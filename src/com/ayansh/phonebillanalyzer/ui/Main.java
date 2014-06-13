package com.ayansh.phonebillanalyzer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.application.AirtelPostPaidMobileBill;
import com.ayansh.phonebillanalyzer.application.Constants;
import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.application.PhoneBill;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class Main extends Activity implements OnItemClickListener {
	
	private ListView listView;
	private BillListAdapter adapter;
	private List<PhoneBill> billList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		setTitle("Bill List");
		
		PBAApplication.getInstance().setContext(getApplicationContext());
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
		
		// Show Ads
		if (!Constants.isPremiumVersion()) {

			// Show Ad.
			AdRequest adRequest = new AdRequest();
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adRequest.addTestDevice("9BAEE2C71E47F042ABCEDE3FCEF2E9D5");
			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}
		
		billList = new ArrayList<PhoneBill>();
		
		billList.addAll(PBAApplication.getInstance().getPhoneBillList(true));
		
		billList.add(0, new AirtelPostPaidMobileBill("DUMMY"));	// Dummy Entry
		
		listView = (ListView) findViewById(R.id.bill_list);
		
		adapter = new BillListAdapter(this, R.layout.billlistrow, R.id.bill_no, billList);
		
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		listView.setOnItemClickListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()){
		
		case R.id.Help:
			EasyTracker.getTracker().sendView("/Help");
			Intent help = new Intent(Main.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			Main.this.startActivity(help);
			break;
			
		case R.id.About:
			EasyTracker.getTracker().sendView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
			break;
			
		case R.id.ReloadContacts:
			PBAApplication.getInstance().reloadContactsInfo();
			break;
		
		}
		
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		
		if(pos == 0){
						
			// New Phone Bill
			Intent newBill = new Intent(Main.this, NewBill.class);
			Main.this.startActivityForResult(newBill, 100);
			
		}
		else{
			
			Intent analyzeBill = new Intent(Main.this, AnaylzeBill.class);
			analyzeBill.putExtra("Position", pos-1);
			Main.this.startActivity(analyzeBill);
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case 100:
			
			billList.clear();
			billList.addAll(PBAApplication.getInstance().getPhoneBillList(true));
			billList.add(0, new AirtelPostPaidMobileBill("DUMMY"));	// Dummy Entry
			
			adapter.notifyDataSetChanged();
			break;
			
		}
	}

}
