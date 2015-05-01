package com.ayansh.phonebillanalyzer.application;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PhoneBill {

	protected String phoneNo, billNo, dueDate, fromDate, toDate, billDate;
	protected List<CallDetailItem> callDetails;
	protected Date bill_date;
	
	protected String billType;
	protected String fileName, password;
	protected Uri fileURI;
	protected int pages;
	
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
	
	public String getBillType(){
		return billType;
	}
	
	public void setBillType(String type){
		billType = type;
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
		
		if(billDate == null){
			return "";
		}
		
		String[] date = billDate.split("-");
		return date[0];
	}

	public String getBillMonth(){
		
		if(billDate == null){
			return "";
		}
		
		String[] date = billDate.split("-");
		if(date.length < 2){
			return "null";
		}
		return date[1];
	}
	
	@SuppressWarnings("deprecation")
	public void readPDFFile() throws Exception{
		
		HttpClient httpClient = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost("http://apps.ayansh.com/Phone-Bill-Analyzer/parse_bill.php");
	    
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    
	    InputStream is = PBAApplication.getInstance().getContext().getContentResolver().openInputStream(fileURI);
	    InputStreamBody isb = new InputStreamBody(is, fileName);
	    
	    //builder.addTextBody("password", password);
	    builder.addPart("type", new StringBody(billType));
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
		
		try{
			
			JSONObject result = new JSONObject(sbuilder.toString());
			
			int status = result.getInt("ErrorCode");
			String message = result.getString("Message");
			pages = result.getInt("PageCount");

			JSONObject billDetails = result.getJSONObject("BillDetails");
			
			setPhoneNumber(billDetails.getString("PhoneNumber"));
			setBillDate(billDetails.getString("BillDate"));
			billNo = billDetails.getString("BillNo");
			setFromDate(billDetails.getString("FromDate"));
			setToDate(billDetails.getString("ToDate"));
			setDueDate(billDetails.getString("DueDate"));
			
			JSONArray call_details = result.getJSONArray("CallDetails");
			
			CallDetailItem cd = null;
			
			for(int i=0; i<call_details.length(); i++){
			
				cd = new CallDetailItem(call_details.getJSONObject(i));
				callDetails.add(cd);
			}
			
			if(status > 0){
				// Error
				throw new Exception(message);
			}
			
		} catch(JSONException e){
			
			throw new Exception("Cannot read the PDF bill. Please report this error to the developer");
			
		}
		
	}
	
	public void saveToDB(){
		
		List<String> queries = new ArrayList<String>();
		
		PBAApplicationDB appDB = PBAApplicationDB.getInstance();
		
		// If Exists... then delete and save again.
		boolean billExists = appDB.checkBillNumberExists(billNo);
		
		if(billExists){
			
			PBAApplication.getInstance().deleteBill(billNo);
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
					"'" + cdi.getComments() + "'," +
					"'" + cdi.getFreeCall() + "'," +
					"'" + cdi.getRoamingCall() + "'," +
					"'" + cdi.getSmsCall() + "'," +
					"'" + cdi.getStdCall() + "'," +
					"" + cdi.getPulse() + "" +
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
		
		String query = "";
		
		if(PBAApplication.getInstance().includeDiscountedCalls()){
			
			query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
					+ "where cd.BillNo = '" + billNo + "' "
					+ "group by n order by Amount desc limit 5";
		}
		else{
			
			query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
					+ "where cd.BillNo = '" + billNo + "' "
					+ "and cd.IsFreeCall <> 'X' "
					+ "group by n order by Amount desc limit 5";
		}
		
		Cursor cursor = appDB.rawQuery(query);
		
		JSONArray resultData = new JSONArray();
		
		if(cursor.moveToFirst()){
			
			do{
				
				JSONObject data = new JSONObject(); 
				
				try {
					
					data.put("contact", cursor.getString(0));
					data.put("amount", cursor.getFloat(1));
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

		String query = "";
		
		if(PBAApplication.getInstance().includeDiscountedCalls()){
			
			query = "select case when cg.GroupName is null then 'Others' else cg.GroupName end as GroupN, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join (select distinct PhoneNo, GroupName from ContactGroups) as cg "
					+ "on cd.PhoneNo = cg.PhoneNo where cd.BillNo = '" + billNo + "' "
					+ "group by GroupN order by Amount desc";
		}
		else{
			
			query = "select case when cg.GroupName is null then 'Others' else cg.GroupName end as GroupN, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join (select distinct PhoneNo, GroupName from ContactGroups) as cg "
					+ "on cd.PhoneNo = cg.PhoneNo where cd.BillNo = '" + billNo + "' "
					+ "and cd.IsFreeCall <> 'X' group by GroupN order by Amount desc";
		}
		
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

	public JSONArray getSummaryByContactNames() {

		PBAApplicationDB appDB = PBAApplicationDB.getInstance();
		
		String query = "";
		
		if(PBAApplication.getInstance().includeDiscountedCalls()){
			
			query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
					+ "where cd.BillNo = '" + billNo + "' "
					+ "group by n order by Amount desc";
		}
		else{
			
			query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
					+ "sum(cd.Amount) as Amount from BillCallDetails as cd "
					+ "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
					+ "where cd.BillNo = '" + billNo + "' "
					+ "and cd.IsFreeCall <> 'X' "
					+ "group by n order by Amount desc";
		}
		
		Cursor cursor = appDB.rawQuery(query);
		
		JSONArray resultData = new JSONArray();
		
		if(cursor.moveToFirst()){
			
			do{
				
				JSONObject data = new JSONObject(); 
				
				try {

					double amt = cursor.getDouble(1);
					float amount = (float) (Math.round(amt * 100.00) / 100.00);
					data.put("name", cursor.getString(0));
					data.put("amount", amount);
					resultData.put(data);
					
				} catch (JSONException e) {
					// Ignore.
				}
				
			} while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return resultData;
	}

    public JSONArray getAllBillDetails() {

        PBAApplicationDB appDB = PBAApplicationDB.getInstance();

        String query = "";

        if(PBAApplication.getInstance().includeDiscountedCalls()){

            query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
                    + "cd.CallDate, cd.CallTime, cd.Amount as Amount from BillCallDetails as cd "
                    + "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
                    + "where cd.BillNo = '" + billNo + "' "
                    + "order by n";
        }
        else{

            query = "select case when cn.Name is null then cd.PhoneNo else cn.Name end as n, "
                    + "cd.CallDate, cd.CallTime, cd.Amount as Amount from BillCallDetails as cd "
                    + "left outer join ContactNames as cn on cd.PhoneNo = cn.PhoneNo "
                    + "where cd.BillNo = '" + billNo + "' "
                    + "and cd.IsFreeCall <> 'X' "
                    + "order by n";
        }

        Cursor cursor = appDB.rawQuery(query);

        JSONArray resultData = new JSONArray();
        ArrayList<JSONObject> resultList = new ArrayList<JSONObject>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM HH:mm");
        String date_time;
        Date date;

        if(cursor.moveToFirst()){

            do{

                JSONObject data = new JSONObject();

                try {

                    double amt = cursor.getDouble(3);
                    float amount = (float) (Math.round(amt * 100.00) / 100.00);
                    data.put("name", cursor.getString(0));

                    date = sdf.parse(cursor.getString(1) + " " +  cursor.getString(2));
                    date_time = sdf1.format(date);

                    data.put("date_time", date_time);
                    data.put("timestamp", cursor.getString(1) + " " +  cursor.getString(2));
                    data.put("amount", amount);
                    resultList.add(data);

                } catch (Exception e) {
                    // Ignore.
                }

            } while(cursor.moveToNext());

        }

        cursor.close();

        // Sort
        Collections.sort(resultList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
                Date lhsDate, rhsDate;

                try {
                    lhsDate = sdf.parse(lhs.getString("timestamp"));
                    rhsDate = sdf.parse(rhs.getString("timestamp"));
                    return lhsDate.compareTo(rhsDate);
                } catch (Exception e) {
                    // Ignore
                    return 0;
                }
            }
        });

        Iterator<JSONObject> i = resultList.iterator();
        while(i.hasNext()){
            resultData.put(i.next());
        }

        return resultData;
    }

    public JSONArray getContactsWithoutNames(){

        PBAApplicationDB appDB = PBAApplicationDB.getInstance();
        String query = "";

        if(PBAApplication.getInstance().includeDiscountedCalls()){
            query = "SELECT distinct cd.PhoneNo, names.Name FROM BillCallDetails as cd " +
                    "left outer join ContactNames as names on cd.PhoneNo = names.PhoneNo " +
                    "where cd.PhoneNo <> 'data' and names.Name is null order by cd.PhoneNo";
        }
        else{
            query = "SELECT distinct cd.PhoneNo, names.Name FROM BillCallDetails as cd " +
                    "left outer join ContactNames as names on cd.PhoneNo = names.PhoneNo " +
                    "where cd.PhoneNo <> 'data' and cd.IsFreeCall <> 'X' and names.Name is null " +
                    "order by cd.PhoneNo";
        }

        Cursor cursor = appDB.rawQuery(query);

        JSONArray resultData = new JSONArray();

        if(cursor.moveToFirst()){

            do{

                JSONObject data = new JSONObject();

                try {

                    data.put("name", cursor.getString(0));
                    resultData.put(data);

                } catch (JSONException e) {
                    // Ignore.
                }

            } while(cursor.moveToNext());

        }

        cursor.close();

        return resultData;
    }

    public JSONArray getContactsWithoutGroups(){

        PBAApplicationDB appDB = PBAApplicationDB.getInstance();
        String query = "";

        if(PBAApplication.getInstance().includeDiscountedCalls()){
            query = "SELECT distinct cd.PhoneNo, names.Name, groups.GroupName FROM BillCallDetails as cd " +
                    "left outer join ContactNames as names on cd.PhoneNo = names.PhoneNo " +
                    "left outer join ContactGroups as groups on cd.PhoneNo = groups.PhoneNo " +
                    "where cd.PhoneNo <> 'data' and groups.GroupName is null order by cd.PhoneNo";
        }
        else{
            query = "SELECT distinct cd.PhoneNo, names.Name, groups.GroupName FROM BillCallDetails as cd " +
                    "left outer join ContactNames as names on cd.PhoneNo = names.PhoneNo " +
                    "left outer join ContactGroups as groups on cd.PhoneNo = groups.PhoneNo " +
                    "where cd.PhoneNo <> 'data' and cd.IsFreeCall <> 'X' and groups.GroupName is null " +
                    "order by cd.PhoneNo";
        }

        Cursor cursor = appDB.rawQuery(query);

        JSONArray resultData = new JSONArray();

        if(cursor.moveToFirst()){

            do{

                JSONObject data = new JSONObject();

                try {

                    data.put("phone", cursor.getString(0));
                    data.put("name", cursor.getString(1));
                    resultData.put(data);

                } catch (JSONException e) {
                    // Ignore.
                }

            } while(cursor.moveToNext());

        }

        cursor.close();

        return resultData;
    }
	
}