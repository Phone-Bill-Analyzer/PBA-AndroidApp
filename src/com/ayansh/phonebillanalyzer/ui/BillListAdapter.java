package com.ayansh.phonebillanalyzer.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.application.PhoneBill;

public class BillListAdapter extends ArrayAdapter<PhoneBill> {

	List<PhoneBill> billList;
	
	public BillListAdapter(Context context, int resource, int textViewResourceId, List<PhoneBill> billList) {
		
		super(context, resource, textViewResourceId, billList);
		
		this.billList = billList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView = null;
		
		PhoneBill bill = billList.get(position);
		
		if (convertView == null) {
			rowView = LayoutInflater.from(getContext()).inflate(R.layout.billlistrow, parent, false);
			rowView.setTag(bill.getBillNo());
		}

		else {
			if(convertView.getTag().toString().contentEquals(bill.getBillNo())){
				rowView = convertView;
			}
			else{
				rowView = LayoutInflater.from(getContext()).inflate(R.layout.billlistrow, parent, false);
				rowView.setTag(bill.getBillNo());
			}
		}
		
		TextView phone_no = (TextView) rowView.findViewById(R.id.phone_no);
		TextView bill_text = (TextView) rowView.findViewById(R.id.bill_text);
		
		TextView billDate = (TextView) rowView.findViewById(R.id.bill_date);
		TextView billMonth = (TextView) rowView.findViewById(R.id.bill_month);
		
		if(bill.getBillNo().contentEquals("DUMMY")){
			
			LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.bldate);
			ll.setBackgroundResource(R.drawable.new_bill);
			
			billDate.setVisibility(View.GONE);
			billMonth.setVisibility(View.GONE);
			
			phone_no.setText("New Bill");
			bill_text.setText("Click to upload a new Bill");
			
		}
		else{
			
			billDate.setText(bill.getBillDate());
			billMonth.setText(bill.getBillMonth());
			
			phone_no.setText(bill.getPhoneNumber());
			String bt = "";
			
			if(bill.getBillType().contains("Airtel") || bill.getBillType().contains("APPM")){
				bt = "Airtel Bill No: " + bill.getBillNo();
			}
			else if(bill.getBillType().contains("Vodafone") || bill.getBillType().contains("VPPM")){
				bt = "Vodafone Bill No: " + bill.getBillNo();
			}
			else if(bill.getBillType().contains("Reliance") || bill.getBillType().contains("RPPM")){
				bt = "Reliance Bill No: " + bill.getBillNo();
			}
			
			bill_text.setText(bt);
		}
		
		return rowView;
		
	}
	
}