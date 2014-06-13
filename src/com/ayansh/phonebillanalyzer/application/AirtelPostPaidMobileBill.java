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
		
		int pageStartIndex = 0;
		int pageEndIndex = fileText.indexOf("page 1 of");
		
		/*
		 * Page 1
		 * Read Phone Number and Bill Number
		 */
		String page1 = fileText.substring(pageStartIndex, pageEndIndex);
		
		int begin = page1.indexOf("airtel number");
		int end = begin + 30;
		
		String substring = page1.substring(begin, end);
		
		String[] lines = substring.split("\n");
		String[] words = lines[0].split(" ");
		phoneNo = words[2];
		
		begin = page1.indexOf("bill number", begin);
		end = begin + 30;
		
		substring = page1.substring(begin, end);
		
		lines = substring.split("\n");
		words = lines[0].split(" ");
		billNo = words[2];
		
		/*
		 * Page 2
		 * Read Bill Date
		 */
		
		pageStartIndex = pageEndIndex;
		pageEndIndex = fileText.indexOf("page 2 of");
		
		String page2 = fileText.substring(pageStartIndex, pageEndIndex);
		
		begin = page2.indexOf("bill number");
		end = page2.indexOf(billNo);
		
		substring = page2.substring(begin, end);
		lines = substring.split("\n");
		billDate = lines[1];
		
		/*
		 * Itemized Bill
		 */
		
		begin = fileText.indexOf("your itemised statement");
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
