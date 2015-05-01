package com.ayansh.phonebillanalyzer.ui;

import android.webkit.JavascriptInterface;

import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.application.PhoneBill;

public class AppJavaScriptInterface {

	private PhoneBill bill;
	
	public AppJavaScriptInterface(PhoneBill bill) {
		this.bill = bill;
	}

	@JavascriptInterface
	public String getTop5ContactsByAmount(){
		
		return bill.getTop5ContactsByAmount().toString();
		
	}
	
	@JavascriptInterface
	public String getSummaryByContactGroups(){
		
		return bill.getSummaryByContactGroups().toString();
		
	}
	
	@JavascriptInterface
	public String getSummaryByContactNames(){
		
		return bill.getSummaryByContactNames().toString();
		
	}

    @JavascriptInterface
    public String getContactsWithoutNames(){

        return bill.getContactsWithoutNames().toString();

    }

    @JavascriptInterface
    public String getContactsWithoutGroups(){

        return bill.getContactsWithoutGroups().toString();

    }

    @JavascriptInterface
    public String getAllBillDetails(){

        return bill.getAllBillDetails().toString();

    }

    @JavascriptInterface
    public String compareMonthlyUsage(){

        return PBAApplication.getInstance().getMonthlyComparision().toString();

    }
	
}