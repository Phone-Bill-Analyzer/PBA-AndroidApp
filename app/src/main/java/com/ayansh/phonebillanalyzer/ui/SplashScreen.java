package com.ayansh.phonebillanalyzer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.application.Constants;
import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.billingutil.IabHelper;
import com.ayansh.phonebillanalyzer.billingutil.IabHelper.OnIabSetupFinishedListener;
import com.ayansh.phonebillanalyzer.billingutil.IabHelper.QueryInventoryFinishedListener;
import com.ayansh.phonebillanalyzer.billingutil.IabResult;
import com.ayansh.phonebillanalyzer.billingutil.Inventory;
import com.ayansh.phonebillanalyzer.billingutil.Purchase;

public class SplashScreen extends Activity implements
		OnIabSetupFinishedListener, QueryInventoryFinishedListener {

	private IabHelper billingHelper;
	private TextView statusView;
	private boolean appStarted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		
		statusView = (TextView) findViewById(R.id.status);
		statusView.setText("Initializing");
			
        // Get Application Instance.
		PBAApplication app = PBAApplication.getInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());

		// Accept my Terms
        if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, Eula.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			SplashScreen.this.startActivityForResult(eula, 100);
			
		} else {
			// Start the Main Activity
			startSplashScreenActivity();
		}
		
	}
	
	private void startSplashScreenActivity() {
		
		// Register application.
		//PBAApplication.getInstance().registerAppForGCM();
        
		// Instantiate billing helper class
		billingHelper = IabHelper.getInstance(this, Constants.getPublicKey());
		
		if(billingHelper.isSetupComplete()){
			// Set up is already done... so Initialize app.
			startApp();
		}
		else{
			// Set up
			try{
				billingHelper.startSetup(this);
			}
			catch(Exception e){
				// Oh Fuck !
				Log.w(PBAApplication.TAG, e.getMessage(), e);
				billingHelper.dispose();
				finish();
			}
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		PBAApplication app = PBAApplication.getInstance();
		
		switch (requestCode) {

		case 100:
			if (!app.isEULAAccepted()) {
				finish();
			} else {
				// Start Main Activity
				startSplashScreenActivity();
			}
			break;
			
		case 900:
			app.addParameter("FirstLaunch", "Completed");
			startApp();
			break;
			
		case 901:
			startApp();
			break;
		}
	}
	
	private void startApp() {
		
		PBAApplication app = PBAApplication.getInstance();
		
		if(appStarted){
			return;
		}
		
		// Show help for the 1st launch
		if(app.getOptions().get("FirstLaunch") == null){
			// This is first launch !
			showHelp();
			return;
		}
		
		// Check if version is updated.
		int oldAppVersion = app.getOldAppVersion();
		int newAppVersion;
		try {
			newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			newAppVersion = 0;
			Log.e(PBAApplication.TAG, e.getMessage(), e);
		}
		
		if(newAppVersion > oldAppVersion ){
			// Update App Version
			app.updateVersion();
			
			showWhatsNew();
			return;
		}
		
		appStarted = true;
		
		// Start the Main
		Log.i(PBAApplication.TAG, "Start Main");
		Intent start = new Intent(SplashScreen.this, Main.class);
		SplashScreen.this.startActivity(start);
		
		// Kill this activity.
		Log.i(PBAApplication.TAG, "Kill Splash screen");
		SplashScreen.this.finish();
	}

	private void showWhatsNew() {
		
		Intent newFeatures = new Intent(SplashScreen.this, DisplayFile.class);
		newFeatures.putExtra("File", "NewFeatures.html");
		newFeatures.putExtra("Title", "New Features: ");
		SplashScreen.this.startActivityForResult(newFeatures, 901);
	}

	private void showHelp() {
		
		Intent help = new Intent(SplashScreen.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		SplashScreen.this.startActivityForResult(help, 900);
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		
		if (!result.isSuccess()) {
			
			// Log error ! Now I don't know what to do
			Log.w(PBAApplication.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
			// Initialize the app
			startApp();
			
			
		} else {
			
			// Check if the user has purchased premium service			
			// Query for Product Details
			
			List<String> productList = new ArrayList<String>();
			productList.add(Constants.getProductKey());
			
			try{
				billingHelper.queryInventoryAsync(true, productList, this);
			}
			catch(Exception e){
				Log.w(PBAApplication.TAG, e.getMessage(), e);
			}
			
		}
		
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		
		if (result.isFailure()) {
			
			// Log error ! Now I don't know what to do
			Log.w(PBAApplication.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
		} else {
			
			String productKey = Constants.getProductKey();
			
			Purchase item = inv.getPurchase(productKey);
			
			if (item != null) {
				// Has user purchased this premium service ???
				Constants.setPremiumVersion(inv.hasPurchase(productKey));
				
			}
			else{
				Constants.setPremiumVersion(false);
			}
			
			if(inv.getSkuDetails(productKey) != null){
				
				Constants.setProductTitle(inv.getSkuDetails(productKey).getTitle());
				Constants.setProductDescription(inv.getSkuDetails(productKey).getDescription());
				Constants.setProductPrice(inv.getSkuDetails(productKey).getPrice());
			}
			
		}
		
		// Initialize the app
		startApp();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
	}
	
	@Override
	protected void onDestroy(){
		
		if(Constants.isPremiumVersion()){
			try{
				IabHelper.getInstance().dispose();
			}
			catch(Exception e){
				Log.w(PBAApplication.TAG, e.getMessage(), e);
			}
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
    		return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
}