package com.ayansh.phonebillanalyzer.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ayansh.phonebillanalyzer.R;

public class SettingsActivity extends PreferenceActivity {

	public static final String INC_DISC_CALLS = "include_discounted_calls";
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
	

}
