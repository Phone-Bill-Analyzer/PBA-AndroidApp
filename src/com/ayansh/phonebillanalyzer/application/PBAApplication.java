package com.ayansh.phonebillanalyzer.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.ui.SettingsActivity;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class PBAApplication {

	private static PBAApplication application;
	public final static String SenderId = "492119277184";
	public static final String TAG = "PBA";
	
	private PBAApplicationDB appDB;
	private Context context;
	private Tracker tracker;
	
	HashMap<String, String> Options;
	private ArrayList<PhoneBill> phoneBillList;
	
	public static PBAApplication getInstance(){
		
		if(application == null){
			application = new PBAApplication();
		}
		
		return application;
		
	}
	
	private PBAApplication(){
		Options = new HashMap<String, String>();
	}
	
	public void setContext(Context c) {

		if (context == null) {

			context = c;

			// Initialize the DB.
			appDB = PBAApplicationDB.getInstance(context);
			appDB.openDBForWriting();
			appDB.loadOptions();
		}
	}
	
	public Context getContext() {
		return context;
	}
	
	public synchronized Tracker getTracker(){
		
		if(tracker == null){
			
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
			
			tracker = analytics.newTracker(R.xml.global_tracker);
			
		}
		
		return tracker;
		
	}
	
	// Get all Options
	public HashMap<String, String> getOptions() {
		return Options;
	}

	public boolean isEULAAccepted() {

		String eula = Options.get("EULA");
		if (eula == null || eula.contentEquals("")) {
			eula = "false";
		}
		return Boolean.valueOf(Options.get("EULA"));
	}

	public void setEULAResult(boolean result) {
		// Save EULA Result
		addParameter("EULA", String.valueOf(result));
	}

	public void close() {
		appDB.close();
		context = null;
	}

	// Add parameter
	public boolean addParameter(String paramName, String paramValue) {

		List<String> queries = new ArrayList<String>();
		String query = "";

		if (Options.containsKey(paramName)) {
			// Already exists. Update it.
			query = "UPDATE Options SET ParamValue = '" + paramValue
					+ "' WHERE ParamName = '" + paramName + "'";
		} else {
			// New entry. Create it
			query = "INSERT INTO Options (ParamName, ParamValue) VALUES ('"
					+ paramName + "','" + paramValue + "')";
		}

		queries.add(query);
		boolean success = appDB.executeQueries(queries);

		if (success) {
			Options.put(paramName, paramValue);
		}

		return success;

	}

	public boolean removeParameter(String paramName) {

		List<String> queries = new ArrayList<String>();

		String query = "DELETE FROM Options WHERE ParamName = '" + paramName
				+ "'";
		queries.add(query);
		boolean success = appDB.executeQueries(queries);

		if (success) {
			Options.remove(paramName);
		}

		return success;
	}
	
	public int getOldAppVersion() {
		String versionCode = Options.get("AppVersionCode");
		if (versionCode == null || versionCode.contentEquals("")) {
			versionCode = "0";
		}
		return Integer.valueOf(versionCode);
	}

	public void updateVersion() {
		// Update Version

		int version;
		try {
			version = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			version = 0;
			Log.e(TAG, e.getMessage(), e);
		}

		addParameter("AppVersionCode", String.valueOf(version));
	}
	
	public void registerAppForGCM() {

		// Register this app
		String regId1 = Options.get("RegistrationId");
		String regId2 = GCMRegistrar.getRegistrationId(context);

		if (regId1 == null || regId1.contentEquals("") || regId2 == null
				|| regId2.contentEquals("")) {
			// Application is not registered
			Log.v(TAG, "Registering app with GCM");

			// Remove parameters
			removeParameter("RegistrationId");
			removeParameter("RegistrationStatus");

			// Register
			GCMRegistrar.register(context, SenderId);
		}

	}
	
	boolean includeDiscountedCalls(){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean include = sharedPref.getBoolean(SettingsActivity.INC_DISC_CALLS, true);
		
		return include;
		
	}

	public ArrayList<PhoneBill> getPhoneBillList(boolean reload) {
		
		if(reload){
			phoneBillList = appDB.getPhoneBillList();
		}
		
		return phoneBillList;
		
	}

	public void reloadContactsInfo() {
		
		// Get Distinct Phone Numbers
		ArrayList<String> phoneList = PBAApplicationDB.getInstance().getDistinctPhoneNumbers();
		
		reloadContactsInfo(phoneList);
	}
	
	void reloadContactsInfo(ArrayList<String> phoneList){
		
		ArrayList<String> queries = new ArrayList<String>();
		String query = "";
		
		Iterator<String> iterator = phoneList.iterator();
		
		while(iterator.hasNext()){
			
			String pNo = iterator.next();
			
			// Get Contact Name and Group Names
			try {
				
				JSONObject contactData = getContactDetails(pNo);
				
				if (contactData != null) {

					String cName = contactData.getString("Name");
					
					query = "DELETE FROM ContactNames WHERE PhoneNo = '" + pNo + "'";
					queries.add(query);
					
					query = "DELETE FROM ContactGroups WHERE PhoneNo = '" + pNo + "'";
					queries.add(query);
					
					query = "INSERT INTO ContactNames VALUES(" +
							"'" + pNo + "'," +
							"'" + cName + "'" +
							")";
					
					queries.add(query);
					
					JSONArray cGroups = contactData.getJSONArray("Groups");

					for (int i = 0; i < cGroups.length(); i++) {

						String gName = cGroups.getString(i);
						
						query = "INSERT INTO ContactGroups VALUES(" +
								"'" + pNo + "'," +
								"'" + gName + "'" +
								")";
						
						queries.add(query);

					}

				}
				
			} catch (Exception e) {
				// Forget it
				Log.e(PBAApplication.TAG, e.getMessage(), e);
			}
		}
		
		// Save to DB
		PBAApplicationDB.getInstance().executeQueries(queries);
	}
	
	// Get Contact Details from Phone Number
	JSONObject getContactDetails(String pNo) throws Exception {
		
		JSONObject contactData = new JSONObject();
		
		ContentResolver resolver = getContext().getContentResolver();
		
		String[] dataProjection = {PhoneLookup._ID,PhoneLookup.DISPLAY_NAME};
		
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pNo));
		Cursor resultCursor = resolver.query(uri, dataProjection, null, null, null);
		
		if(resultCursor.moveToFirst()){
			
			// Get Name
			contactData.put("Name", resultCursor.getString(1));
			long cID = resultCursor.getLong(0);
			
			resultCursor.close();
			
			// Get Group IDs
			ArrayList<String> groupIDList = new ArrayList<String>();
			
			String where = ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.CONTACT_ID + "=?";
			String[] filter = {ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,Long.toString(cID)};
			
			dataProjection = new String[]{ContactsContract.Data.CONTACT_ID,ContactsContract.Data.DATA1};
			
			Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI,dataProjection, where, filter, null );
			
			if(dataCursor.moveToFirst()){
				
				groupIDList = new ArrayList<String>();
				
				do{
					
					cID = dataCursor.getLong(0);
					groupIDList.add(dataCursor.getString(1));
					
				}while(dataCursor.moveToNext());
			}
			
			dataCursor.close();
			
			if(!groupIDList.isEmpty()){
				
				// Query Group Name
				filter = new String[]{};
				where = ContactsContract.Groups._ID + " IN (?,?,?,?,?,?,?,?,?,?)";
				filter = groupIDList.toArray(filter);
				
				dataProjection = new String[]{ContactsContract.Groups._ID,ContactsContract.Groups.TITLE};
				
				Cursor groupCursor = resolver.query(ContactsContract.Groups.CONTENT_URI,dataProjection, where, filter, null);
				
				if(groupCursor.moveToFirst()){
					
					JSONArray groups = new JSONArray();
					do{
						groups.put(groupCursor.getString(1));
					}while(groupCursor.moveToNext());
					
					contactData.put("Groups", groups);
					
				}
				
				groupCursor.close();
				
			}
			else{
				// Put Empty Group
				contactData.put("Groups", new JSONArray());
			}
			
			return contactData;
			
		}
		else{
			return null;
		}

	}

	public void downloaDBData() {
		
		/*
		 * Add Permission also
		 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
		 */
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		
		String currentDBPath = "/data/" + "com.ayansh.phonebillanalyzer"
				+ "/databases/" + "PBA";
		String backupDBPath = "PBA";
		File currentDB = new File(data, currentDBPath);
		File backupDB = new File(sd, backupDBPath);
		try {
			source = new FileInputStream(currentDB).getChannel();
			destination = new FileOutputStream(backupDB).getChannel();
			destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void deleteBill(String billNo) {
		
		List<String> queries = new ArrayList<String>();
		
		String query = "DELETE FROM BillMetaData WHERE BillNo = '" + billNo + "'";
		queries.add(query);
		
		query = "DELETE FROM BillCallDetails WHERE BillNo = '" + billNo + "'";
		queries.add(query);
		
		appDB.executeQueries(queries);
		
	}
}