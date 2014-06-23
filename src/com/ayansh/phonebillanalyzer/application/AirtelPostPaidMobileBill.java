/**
 * 
 */
package com.ayansh.phonebillanalyzer.application;

import android.net.Uri;

/**
 * @author Varun Verma
 *
 */
public class AirtelPostPaidMobileBill extends PhoneBill {

	public static final String BillType = "AirtelPostPaidMobile";
	
	public AirtelPostPaidMobileBill(String bNo) {
		super(bNo);
		billType = AirtelPostPaidMobileBill.BillType;
	}
	
	public AirtelPostPaidMobileBill (String file, Uri uri){
		super(file, uri);
		billType = AirtelPostPaidMobileBill.BillType;
	}
	
	@Override
	public void parseBillText() {
		
		/*
		 * Page 1 and 2
		 * Read Phone Number and Bill Number
		 */
		
		// Take 1st 100 lines
		String[] lines = fileText.split("\n",100);
		
		for(int i=0; i<100; i++){
			
			if(lines[i].contains("airtel number")){
				
				String[] words = lines[i].split(" ");
				if(words.length == 3 && phoneNo == null){
					phoneNo = words[2];
				}
				
			}
			
			if(lines[i].contains("bill number")){
				
				String[] words = lines[i].split(" ");
				if(words.length == 3 && billNo == null){
					billNo = words[2];
				}
				
			}
			
			if(lines[i].contains("your bill plan(as on")){
				
				String[] words = lines[i].split(" ");
				if(words.length > 5 && billDate == null){
					billDate = words[4];
					setBillDate(billDate.substring(0, 11));
				}
				
			}
			
		}
		
		/*
		 * Itemized Bill
		 */
		
		int begin = fileText.indexOf("your itemised statement");
		String itemizedStatement = fileText.substring(begin);
		
		String[] billGroups = itemizedStatement.split("total");
		int size = billGroups.length;
		
		for(int i=0; i<size; i++){
			
			String billGroup = billGroups[i];
			
			String[] itemLines = billGroup.split("\n");
			
			int itemSize = itemLines.length;
			
			for(int j=0; j<itemSize; j++){
				
				String itemLine = itemLines[j];
				
				String[] itemWords = itemLine.split(" ");
				
				if(itemWords.length < 6){
					continue;
				}
				
				try{
					
					@SuppressWarnings("unused")
					int index = Integer.valueOf(itemWords[0]);
					
					CallDetailItem pbi = new CallDetailItem();
					
					pbi.setCallDate(itemWords[1]);
					pbi.setCallTime(itemWords[2]);
					
					if(itemWords[3].contains("airtelgprs")){
						pbi.setPhoneNumber("data");
					}
					else{
						pbi.setPhoneNumber(itemWords[3]);
					}
					
					pbi.setDuration(itemWords[4]);
					
					// Remove **
					itemWords[5] = itemWords[5].replaceAll("\\*", "");
					
					pbi.setCost(Float.valueOf(itemWords[5]));
					
					if(itemWords.length == 7){
						
						if(itemWords[6].contains("*")){
							pbi.setComments("discounted calls");
						}
					}
					
					callDetails.add(pbi);
					
				} catch(NumberFormatException nfe){
					// Skip this line
				}
				
			}
			
		}
				
	}

}
