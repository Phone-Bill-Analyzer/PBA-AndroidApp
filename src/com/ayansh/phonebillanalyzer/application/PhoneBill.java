package com.ayansh.phonebillanalyzer.application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;

public abstract class PhoneBill {

	protected String phoneNo, billNo, dueDate, fromDate, toDate, billDate;
	protected List<CallDetailItem> callDetails;
	protected Date bill_date;
	
	protected String billType;
	protected String fileName, password;
	protected Uri fileURI;
	protected String fileText;
	protected int pages;
	
	public abstract void parseBillText();
	
	public PhoneBill (String name, Uri uri){
		fileURI = uri;
		fileName = name;
		callDetails = new ArrayList<CallDetailItem>();
		
		dueDate = fromDate = toDate = "";
		
	}
	
	public PhoneBill(String bNo){
		billNo = bNo;
		callDetails = new ArrayList<CallDetailItem>();
		dueDate = fromDate = toDate = "";
	}
	
	public void setPassword(String pwd){
		password = pwd;
	}
	
	public void setPhoneNumber(String no){
		phoneNo = no;
	}
	
	public String getPhoneNumber(){
		return phoneNo;
	}
	
	public String getBillNo(){
		return billNo;
	}
	
	public void setDueDate(String date){
		dueDate = date;
	}
	
	public String getDueDate(){
		return dueDate;
	}
	
	public void setFromDate(String date){
		fromDate = date;
	}
	
	public String getFromDate(){
		return fromDate;
	}
	
	public void setToDate(String date){
		toDate = date;
	}
	
	public String getToDate(){
		return toDate;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void setBillDate(String date){
		
		billDate = date;
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			bill_date = sdf.parse(billDate);
		} catch (ParseException e) {
			bill_date = new Date();
		}
		
	}
	
	public String getBillDate(){
		
		String[] date = billDate.split("-");
		return date[0];
	}

	public String getBillMonth(){
		
		String[] date = billDate.split("-");
		return date[1];
	}
	
	@SuppressWarnings("deprecation")
	public void readPDFFile() throws Exception{
		
		HttpClient httpClient = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost("http://apps.ayansh.com/Phone-Bill-Analyzer/read_pdf.php");
	    
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    
	    InputStream is = PBAApplication.getInstance().getContext().getContentResolver().openInputStream(fileURI);
	    InputStreamBody isb = new InputStreamBody(is, fileName);
	    
	    builder.addTextBody("password", password);
	    builder.addPart("password", new StringBody(password));
	    builder.addPart("file", isb);
	    
	    httpPost.setEntity(builder.build());
	    
	    // Execute HTTP Request
	    HttpResponse response = httpClient.execute(httpPost);
	    
	    InputStream ris = response.getEntity().getContent();
		InputStreamReader isr = new InputStreamReader(ris);
		BufferedReader reader = new BufferedReader(isr);
		
		StringBuilder sbuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sbuilder.append(line);
		}
		
		JSONObject result = new JSONObject(sbuilder.toString());
		
		int status = result.getInt("ErrorCode");
		String message = result.getString("Message");
		pages = result.getInt("PageCount");
		fileText = result.getString("Text");
		
		
		if(status > 0){
			// Error
			throw new Exception(message);
		}
		
	}
	
	public void saveToDB(){
		
		List<String> queries = new ArrayList<String>();
		
		PBAApplicationDB appDB = PBAApplicationDB.getInstance();
		
		// If Exists... then delete and save again.
		boolean billExists = appDB.checkBillNumberExists(billNo);
		
		if(billExists){
			
			String query = "DELETE FROM BillMetaData WHERE BillNo = '" + billNo + "'";
			queries.add(query);
			
			query = "DELETE FROM BillCallDetails WHERE BillNo = '" + billNo + "'";
			queries.add(query);
			
			appDB.executeQueries(queries);
		}
		
		queries.clear();
		
		// Save Meta Data
		String query = "INSERT INTO BillMetaData VALUES(" +
				"'" + billNo + "'," +
				"'" + billType + "'," +
				"'" + phoneNo + "'," +
				"'" + billDate + "'," +
				"'" + fromDate + "'," +
				"'" + toDate + "'," +
				"'" + dueDate + "'" +
				")";
		
		queries.add(query);
		
		// Save Bill Items
		Iterator<CallDetailItem> i = callDetails.iterator();
		
		while(i.hasNext()){
			
			CallDetailItem cdi = i.next();
			
			query = "INSERT INTO BillCallDetails VALUES(" +
					"'" + billNo + "'," +
					"'" + cdi.getPhoneNumber() + "'," +
					"'" + cdi.getCallDate() + "'," +
					"'" + cdi.getCallTime() + "'," +
					"'" + cdi.getDuration() + "'," +
					"" + cdi.getCost() + "," +
					"'" + cdi.getComments() + "'" +
					")";
			
			queries.add(query);
		}
		
		appDB.executeQueries(queries);
		
	}
	
	public static Comparator<PhoneBill> SortByBillDate = new Comparator<PhoneBill>() {
		
		@Override
		public int compare(PhoneBill lhs, PhoneBill rhs) {
			return lhs.bill_date.compareTo(rhs.bill_date);
		}
	};

	public JSONArray getTop5ContactsByAmount() {
		
		PBAApplicationDB appDB = PBAApplicationDB.getInstance();
		
		String query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
				+ "cd.Amount as Amount from BillCallDetails as cd "
				+ "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
				+ "where cd.Comments <> 'discounted calls' group by n order by Amount desc limit 5";
		
		Cursor cursor = appDB.rawQuery(query);
		
		JSONArray resultData = new JSONArray();
		
		if(cursor.moveToFirst()){
			
			do{
				
				JSONObject data = new JSONObject(); 
				
				try {
					
					data.put("contact", cursor.getString(0));
					data.put("amount", cursor.getLong(1));
					resultData.put(data);
					
				} catch (JSONException e) {
					// Ignore.
				}
				
			} while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return resultData;
		
	}

	public JSONArray getSummaryByContactGroups() {

		PBAApplicationDB appDB = PBAApplicationDB.getInstance();
		
		String query = "select case when cg.GroupName is null then 'Others' else cg.GroupName end as GroupN, "
				+ "sum(cd.Amount) as Amount "
				+ "from BillCallDetails as cd left outer join ContactGroups as cg on cd.PhoneNo = cg.PhoneNo group by GroupN";
		
		Cursor cursor = appDB.rawQuery(query);
		
		JSONArray resultData = new JSONArray();
		
		if(cursor.moveToFirst()){
			
			do{
				
				JSONObject data = new JSONObject();
				
				try{
					
					data.put("group", cursor.getString(0));
					data.put("amount", cursor.getFloat(1));
					
					resultData.put(data);
					
				}catch (JSONException e){
					// Ignore
				}
				
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return resultData;
	}
	
}