package com.sync;

import com.android.settings.IconPreferenceScreen;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class AuthSelectActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.auth_selection);
		
		String ourAccKey = getString(R.string.auth_our_acc_key);
		String googleAccKey = getString(R.string.auth_google_key);
		String fbAccKey = getString(R.string.auth_facebook_key);

		Preference ourAcc = 
			(Preference)findPreference(ourAccKey);
		IconPreferenceScreen googleAcc = 
			(IconPreferenceScreen)findPreference(googleAccKey);
		IconPreferenceScreen fbAcc = 
			(IconPreferenceScreen)findPreference(fbAccKey);
		
		Drawable googleIcon = getResources().getDrawable(R.drawable.google_icon);
		Drawable fbIcon = getResources().getDrawable(R.drawable.fb_icon);
		
		googleAcc.setIcon(googleIcon);
		fbAcc.setIcon(fbIcon);
		
		ourAcc.setOnPreferenceClickListener(new OurAccOnClickListener());
		googleAcc.setOnPreferenceClickListener(new GoogleOnClickListener());
	}
	
	
	private class GoogleOnClickListener implements OnPreferenceClickListener {

		public boolean onPreferenceClick(Preference preference) {
			//Intent forward = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
			Intent forward = new Intent(getApplicationContext(), SyncAuth.class);
			
			String[] authorities = new String[]{
					getString(R.string.authority_positions), 
					getString(R.string.authority_bookmarks), 
					getString(R.string.authority_settings)
					};
			
			forward.putExtra(android.provider.Settings.EXTRA_AUTHORITIES, authorities);
			startActivity(forward);
			
			finish();
			return true;
		}
	}
	
	private class OurAccOnClickListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			startActivity(new Intent(getApplicationContext(), AuthOur.class));
			finish();
			return true;
		}
	}	
}
