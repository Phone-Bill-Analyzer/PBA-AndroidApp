package com.ayansh.phonebillanalyzer.application;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.net.Uri;

public class ReliancePostPaidMobileBill extends PhoneBill {

	public static final String BillType = "ReliancePostPaidMobile";
	
	public ReliancePostPaidMobileBill(String name, Uri uri) {
		super(name, uri);
		billType = ReliancePostPaidMobileBill.BillType;
	}

	public ReliancePostPaidMobileBill(String bNo) {
		super(bNo);
		billType = ReliancePostPaidMobileBill.BillType;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void parseBillText() {
		
		String[] pages = fileText.split("Page ");
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
		
		/*
		 * Page 1
		 */
		String page1 = pages[0];
		
		String[] lines = page1.split("\n");
		String line = "";
		String billYear = "";
		
		int lineCount = lines.length;
		
		for(int i=0; i<lineCount; i++){
			
			line = lines[i];
			
			if(line.contains("Bill Period")){
				
				String[] parts = line.split(":");
				String[] dates = parts[1].split(" ");
				
				try {
					
					Date date = sdf1.parse(dates[0]);
					fromDate = sdf2.format(date);
					
					date = sdf1.parse(dates[2]);
					toDate = sdf2.format(date);
					
				} catch (ParseException e) {
					// ??
				}
				
			}
			
			if(line.contains("Bill Date")){
				
				String[] parts = line.split(":");
				
				billYear = parts[1].split("-")[2];
				
				try {
					
					sdf1 = new SimpleDateFormat("dd-MMM-yy");
					
					Date date = sdf1.parse(parts[1]);
					billDate = sdf2.format(date);
					
				} catch (ParseException e) {
					// ??
				}
			}
			
			if(line.contains("Bill No") && (billNo == null || billNo.contentEquals(""))){
				
				String[] parts = line.split(":");
				billNo = parts[1];
			}
			
			if(line.contains("Your Reliance") && line.contains("Mobile No")){
				
				String[] parts = line.split(" ");
				phoneNo = parts[5];
				
			}
		}
		
		/*
		 * Itemized Bill - Page 3 onwards
		 */
		
		sdf1 = new SimpleDateFormat("dd-MM-yy");
		sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
		
		for(int i=2; i<pages.length; i++ ){
			
			String page = pages[i];
			
			lines = page.split("\n");
			
			lineCount = lines.length;
			
			for(int j=0; j<lineCount; j++){
				
				String[] itemWords = lines[j].split(" ");
				
				if(itemWords.length == 6){
					
					// Check
					if(itemWords[0].contains("-") && itemWords[1].contains(":")){
						
						try {
							
							Date date = sdf1.parse(itemWords[0] + "-" + billYear);
							
							CallDetailItem pbi = new CallDetailItem();
							
							pbi.setCallDate(sdf2.format(date));
							pbi.setCallTime(itemWords[1]);
							
							pbi.setPhoneNumber(itemWords[2]);
							pbi.setDuration(itemWords[3]);
							
							if(itemWords[5].contains("#")){
								pbi.setComments("discounted calls");
							}
							
							// Remove #
							itemWords[5] = itemWords[5].replaceAll("#", "");
							
							pbi.setCost(Float.valueOf(itemWords[5]));
							
							callDetails.add(pbi);
							
						} catch (ParseException e) {
							// Forget it.
						}
						
					}
					
				}
				
			}
			
		}
		
	}

}
