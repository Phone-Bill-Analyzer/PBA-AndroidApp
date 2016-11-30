package com.ayansh.phonebillanalyzer.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ayansh.phonebillanalyzer.R;

public class SettingsActivity extends AppCompatActivity {

	public static final String INC_DISC_CALLS = "include_discounted_calls";
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}

	}

	public static class MyPreferenceFragment extends PreferenceFragment {

		@Override
		public void onCreate(final Bundle savedInstanceState){

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}

	}
	
}