package com.ayansh.phonebillanalyzer.ui;

import java.util.Iterator;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.phonebillanalyzer.R;
import com.ayansh.phonebillanalyzer.application.Constants;
import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.application.PhoneBill;
import com.ayansh.phonebillanalyzer.application.ReadPDFFileCommand;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

public class NewBill extends Activity implements OnClickListener, Invoker {
	
	private EditText fileName, password;
	private Uri fileURI;
	private ProgressDialog pd;
	private Spinner billType;
	private TextView helpText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.new_bill);
		
		// Show Ads
		if (!Constants.isPremiumVersion()) {

			// Show Ad.
			AdRequest adRequest = new AdRequest.Builder()
			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
			.addTestDevice("9BAEE2C71E47F042ABCEDE3FCEF2E9D5").build();

			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}
		
		fileName = (EditText) findViewById(R.id.file_name);
		fileName.setOnClickListener(this);
		
		password = (EditText) findViewById(R.id.pwd);
		
		Button uploadButton = (Button) findViewById(R.id.upload);
		uploadButton.setOnClickListener(this);
		
		billType = (Spinner) findViewById(R.id.bill_type);
		
		helpText = (TextView) findViewById(R.id.help_text);
		
		showHelp();
	}

	@Override
	protected void onStart(){
		
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	protected void onStop(){
		
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	private void showHelp() {
		
		String help = "Note : Internet connectivity is required to analyze the bill "
				+ "because this app depends on Google Charts API"
				+ "\n\n";
		
		help = help + "Note : You will need a file browser app to browse files on "
				+ "device and upload.\n\n";
		
		if (!Constants.isPremiumVersion()) {
		
			help = help + "Note : You are using the free version of the app.\n"
					+ "In this version, you can anlyze only 1 bill at a time.\n"
					+ "Old bill will be deleted.";
		}
		
		helpText.setText(help);
		
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
			
		case R.id.file_name:
			selectFile();
			break;
			
		case R.id.upload:
			deleteOldBillDetails();
			uploadFile();
			break;
			
		}
		
	}

	private void deleteOldBillDetails() {
		
		if (Constants.isPremiumVersion()) {
			// Premium Version
			return;
		}
		
		PBAApplication app = PBAApplication.getInstance();
		
		Iterator<PhoneBill> i = app.getPhoneBillList(false).iterator();
		
		while(i.hasNext()){
			
			app.deleteBill(i.next().getBillNo());
			
		}
		
	}

	private void selectFile() {
		
		Intent fileSelect = new Intent(Intent.ACTION_GET_CONTENT); 
		fileSelect.setType("*/*"); 
		fileSelect.addCategory(Intent.CATEGORY_OPENABLE);

	    try {
	    	
	        startActivityForResult(Intent.createChooser(fileSelect, "Select a File to Upload"),100);
	        
	    } catch (android.content.ActivityNotFoundException ex) {
	        // Potentially direct the user to the Market with a Dialog
	        Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
	    }
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    
		switch (requestCode) {
		
	        case 100:
	        if (resultCode == RESULT_OK) {
	        	
	            // Get the Uri of the selected file 
	            fileURI = data.getData();
	            
	            // Get the path
	            Cursor returnCursor = getContentResolver().query(fileURI, null, null, null, null);
	            
	            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
	            returnCursor.moveToFirst();
	            
	            String fname = returnCursor.getString(nameIndex);
	            
	            fileName.setText(fname);
	            
	        }
	        
	        break;
	    }
		
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void uploadFile(){
		
		int bt = billType.getSelectedItemPosition();
		String pwd = password.getEditableText().toString();
		
		String filename = fileName.getEditableText().toString();
		
		CommandExecuter ce = new CommandExecuter();
		
		ReadPDFFileCommand command = new ReadPDFFileCommand(this, bt, filename, fileURI);
		
		command.setPassword(pwd);
		
		pd = ProgressDialog.show(this, "Reading PDF File", "Please wait while we read the PDF File");
		pd.setMax(100);
		
		ce.execute(command);
		
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess()){
			
			this.setResult(RESULT_OK);
			finish();
			
		}
		else{
			
			Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
			Log.e(PBAApplication.TAG, result.getErrorMessage(), result.getException());
		}
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo pi) {
		
		pd.setProgress(pi.getProgressPercentage());
		pd.setMessage(pi.getProgressMessage());
		
	}
	
}