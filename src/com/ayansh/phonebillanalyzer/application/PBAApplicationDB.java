package com.ayansh.phonebillanalyzer.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PBAApplicationDB extends SQLiteOpenHelper{

	private static final String dbName = "PBA";
	private static final int dbVersion = 1;
	
	private static PBAApplicationDB appDB;
	
	private PBAApplication app;
	private SQLiteDatabase db;

	/**
	 * @param context
	 */
	private PBAApplicationDB(Context c){
		
		super(c, dbName, null, dbVersion);
		app = PBAApplication.getInstance();
	}
	
	static PBAApplicationDB getInstance(Context c){
		
		if(appDB == null){
			appDB = new PBAApplicationDB(c);
		}
		
		return appDB;
	}
	
	static PBAApplicationDB getInstance(){
		return appDB;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String createOptionsTable = "CREATE TABLE Options ("
				+ "ParamName VARCHAR(20), " + // Parameter Name
				"ParamValue VARCHAR(20)" + // Parameter Value
				")";
		
		String createContactNamesTable = "CREATE TABLE ContactNames ("
				+ "PhoneNo VARCHAR, " + // Phone No
				"Name VARCHAR" + // Name
				")";
		
		String createContactGroupsTable = "CREATE TABLE ContactGroups ("
				+ "PhoneNo VARCHAR, " + // Phone No
				"GroupName VARCHAR" + // GroupName
				")";
		
		String createBillMetaDataTable = "CREATE TABLE BillMetaData (" + 
				"BillNo VARCHAR , " + // Bill No
				"BillType VARCHAR, "  +
				"PhoneNo VARCHAR, "  +
				"BillDate VARCHAR, "  +
				"FromDate VARCHAR, "  +
				"ToDate VARCHAR, "  +
				"DueDate VARCHAR"  +
				")";
		
		String createBillCallDetailsTable = "CREATE TABLE BillCallDetails (" + 
				"BillNo VARCHAR , " + // Bill No
				"PhoneNo VARCHAR, "  +
				"CallDate VARCHAR, "  +
				"CallTime VARCHAR, "  +
				"CallDuration VARCHAR, "  +
				"Amount VARCHAR, "  +
				"Comments VARCHAR"  +
				")";
		
		// create a new table - if not existing
		try {
			// Create Tables.
			Log.i(PBAApplication.TAG,"Creating Tables for Version:" + String.valueOf(dbVersion));

			db.execSQL(createOptionsTable);
			db.execSQL(createContactNamesTable);
			db.execSQL(createContactGroupsTable);
			db.execSQL(createBillMetaDataTable);
			db.execSQL(createBillCallDetailsTable);

			Log.i(PBAApplication.TAG, "Tables created successfully");

		} catch (SQLException e) {
			Log.e(PBAApplication.TAG, e.getMessage(), e);
		}
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	void openDBForWriting(){
		db = appDB.getWritableDatabase();
	}
	
	synchronized void loadOptions(){
		
		if(!db.isOpen()){
			return;
		}
		
		// Load Parameters
		String name, value;
		Log.v(PBAApplication.TAG, "Loading application Options");
		
		Cursor cursor = db.query("Options", null, null, null, null, null, null);
		
		if(cursor.moveToFirst()){
			name = cursor.getString(cursor.getColumnIndex("ParamName"));
			value = cursor.getString(cursor.getColumnIndex("ParamValue"));
			app.Options.put(name, value);
		}
		
		while(cursor.moveToNext()){
			name = cursor.getString(cursor.getColumnIndex("ParamName"));
			value = cursor.getString(cursor.getColumnIndex("ParamValue"));
			app.Options.put(name, value);
		}
		
		cursor.close();
		
	}

	synchronized boolean executeQueries(List<String> queries) {

		Iterator<String> iterator = queries.listIterator();
		String query;

		try {
			db.beginTransaction();

			while (iterator.hasNext()) {
				query = iterator.next();
				db.execSQL(query);
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			
			return true;
			
		} catch (Exception e) {
			// Do nothing! -- Track the error causing query
			Log.e(PBAApplication.TAG, e.getMessage(), e);
			db.endTransaction();
			return false;
		}
	}
	
	boolean checkBillNumberExists(String billNo) {
		
		String[] columns = {"BillNo"};
		String selection = "BillNo='" + billNo + "'";
		
		Cursor cursor = db.query("BillMetaData", columns, selection, null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		
		if(count > 0){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	ArrayList<PhoneBill> getPhoneBillList() {
		
		ArrayList<PhoneBill> billList = new ArrayList<PhoneBill>();
		
		String sql = "SELECT * FROM BillMetaData";
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			
			do{
				
				PhoneBill bill = null;
				
				String billType = cursor.getString(cursor.getColumnIndex("BillType"));
				
				if(billType.contentEquals(AirtelPostPaidMobileBill.BillType)){
					
					bill = new AirtelPostPaidMobileBill(cursor.getString(cursor.getColumnIndex("BillNo")));
					
				}
				
				bill.setPhoneNumber(cursor.getString(cursor.getColumnIndex("PhoneNo")));
				bill.setBillDate(cursor.getString(cursor.getColumnIndex("BillDate")));
				bill.setFromDate(cursor.getString(cursor.getColumnIndex("FromDate")));
				bill.setToDate(cursor.getString(cursor.getColumnIndex("ToDate")));
				bill.setDueDate(cursor.getString(cursor.getColumnIndex("DueDate")));
				
				billList.add(bill);
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		Collections.sort(billList, PhoneBill.SortByBillDate);
		
		return billList;
		
	}
	
	ArrayList<CallDetailItem> getCallDetails(String billNo) {
		
		ArrayList<CallDetailItem> callDetails = new ArrayList<CallDetailItem>();
		
		String sql = "SELECT * FROM BillCallDetails where BillNo = '" + billNo + "'";
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			
			do{
				
				CallDetailItem callDetail = new CallDetailItem();
				
				callDetail.setPhoneNumber(cursor.getString(cursor.getColumnIndex("PhoneNo")));
				callDetail.setCallDate(cursor.getString(cursor.getColumnIndex("CallDate")));
				callDetail.setCallTime(cursor.getString(cursor.getColumnIndex("CallTime")));
				callDetail.setDuration(cursor.getString(cursor.getColumnIndex("CallDuration")));
				callDetail.setCost(cursor.getFloat(cursor.getColumnIndex("Amount")));
				callDetail.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
				callDetails.add(callDetail);
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return callDetails;
		
	}
	
	ArrayList<String> getDistinctPhoneNumbers(){
		
		String query = "SELECT DISTINCT PhoneNo from BillCallDetails";
		
		Cursor cursor = rawQuery(query);
		
		ArrayList<String> phoneList = new ArrayList<String>();
		
		if(cursor.moveToFirst()){
			
			do{
				
				phoneList.add(cursor.getString(0));
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return phoneList;
		
	}
	
	ArrayList<String> getDistinctPhoneNumbers(String billNo){
		
		String query = "SELECT DISTINCT PhoneNo from BillCallDetails where BillNo = '" + billNo + "'";
		
		Cursor cursor = rawQuery(query);
		
		ArrayList<String> phoneList = new ArrayList<String>();
		
		if(cursor.moveToFirst()){
			
			do{
				
				phoneList.add(cursor.getString(0));
				
			}while(cursor.moveToNext());
			
		}
		
		cursor.close();
		
		return phoneList;
	}
	
	Cursor rawQuery(String query){
		
		return db.rawQuery(query, null);
		
	}
	
}