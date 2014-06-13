package com.ayansh.phonebillanalyzer.application;

import java.util.ArrayList;

import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.net.Uri;

public class ReadPDFFileCommand extends Command {

	private Uri fileUri;
	private String password, fileName;
	private int billType;
	private PhoneBill bill;
	
	public ReadPDFFileCommand(Invoker caller, int bt, String file, Uri uri) {
		
		super(caller);
		fileUri = uri;
		fileName = file;
		billType = bt;
	}

	public void setPassword(String pwd){
		password = pwd;
	}
	
	@Override
	protected void execute(ResultObject result) throws Exception {
				
		switch(billType){
		
		case 0:
			bill = new AirtelPostPaidMobileBill(fileName, fileUri);
			break;
		
		case 1:
			break;
			
		}
		
		bill.setPassword(password);
		
		ProgressInfo pi;
		
		// Read PDF File
		bill.readPDFFile();
		
		// Parse the Text and read bill details
		pi = new ProgressInfo(40, "Reading bill details");
		publishProgress(pi);
		bill.parseBillText();
		
		// Save Bill
		pi = new ProgressInfo(80, "Saving bill details for analysis");
		publishProgress(pi);
		bill.saveToDB();
		
		// Map Contacts and Groups
		mapContactData();
		
	}

	private void mapContactData() {
		
		// Get Distinct Phone Numbers
		ArrayList<String> phoneList = PBAApplicationDB.getInstance().getDistinctPhoneNumbers(bill.getBillNo());
		
		PBAApplication.getInstance().reloadContactsInfo(phoneList);
		
	}

}
