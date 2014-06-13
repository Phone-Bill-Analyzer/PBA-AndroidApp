package com.ayansh.phonebillanalyzer.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.application.Constants;
import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.application.PhoneBill;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("SetJavaScriptEnabled")
public class AnaylzeBill extends Activity implements OnItemSelectedListener {

	private PhoneBill bill;
	private WebView webView;
	private String htmlText;
	private Spinner analysisType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill_analysis);
		
		setTitle("Analyze Bill");
		
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
		
		int pos = getIntent().getIntExtra("Position", -1);
		if(pos < 0){
			return;
		}
		
		analysisType = (Spinner) findViewById(R.id.analysis_type);
		analysisType.setOnItemSelectedListener(this);
		
		bill = PBAApplication.getInstance().getPhoneBillList(false).get(pos);
		
		webView = (WebView) findViewById(R.id.webview);
		
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		webView.addJavascriptInterface(new AppJavaScriptInterface(bill), "App");
		
	}
	
	private void showFromRawSource() {
		
		//webView.clearCache(true);
		
		webView.loadData(htmlText, "text/html", "utf-8");
		//webView.loadDataWithBaseURL( "file:///android_asset/", htmlText, "text/html", "utf-8", null );
		//webView.loadDataWithBaseURL( "file:///android_asset/", htmlText, "text/html", "utf-8", "");

	}
	
	private void getHTMLFromFile(String fileName) {
		// Get HTML File from RAW Resource
		Resources res = getResources();
        InputStream is;
        
		try {
			
			is = res.getAssets().open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        htmlText = "";
	        String line = "";
	        
	        while((line = reader.readLine()) != null){
				htmlText = htmlText + "\n" + line;
			}
	        
		} catch (IOException e) {
			Log.e(PBAApplication.TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		
		switch(pos){
			
		case 0:
			
			getHTMLFromFile("top_5_pie_chart.html");
			break;
			
		case 1:
			getHTMLFromFile("contact_group_summary.html");
			break;
		
		}
		
		showFromRawSource();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
		getHTMLFromFile("top_5_pie_chart.html");
		showFromRawSource();
	}
	
}