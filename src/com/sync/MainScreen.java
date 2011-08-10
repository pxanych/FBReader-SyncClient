package com.sync;

import java.net.MalformedURLException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class MainScreen extends PreferenceActivity {
	
	private ServerInterface myServerInterface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.sync_main_view);
		// TODO: tag - authenticate
		setGUIState(
				getPreferences(MODE_PRIVATE).
				getBoolean(getString(R.string.settings_auth), false)
				);
		//
	}
	
	// TODO: tag - authenticate
	protected void setGUIState(Boolean authentiacated) {
		Preference authPreference = (Preference)findPreference("auth_preference");
		Preference forceSyncPreference = (Preference)findPreference("force_sync");
		if (authentiacated) {
			authPreference.setTitle(R.string.sync_logout);
			authPreference.setSummary(R.string.sync_logout_summary);
			authPreference.setOnPreferenceClickListener(new LogoutListener());
		} else {
			authPreference.setTitle(R.string.sync_authenticate);
			authPreference.setSummary(R.string.sync_authenticate_summary);
			authPreference.setOnPreferenceClickListener(new AuthListener());
		}
		forceSyncPreference.setEnabled(authentiacated);
	}
	//
	
	protected void ensureAuthentication() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		if (settings.getBoolean(getString(R.string.settings_auth), false) == false) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.addCategory(Intent.CATEGORY_BROWSABLE);
			i.addCategory(Intent.CATEGORY_DEFAULT);
			startActivityForResult(i, 1);
		} else if (myServerInterface == null) {
			try {
				String id = settings.getString(getString(R.string.settings_id), null);
				String sig = settings.getString(getString(R.string.settings_sig), null);
				myServerInterface = new ServerInterface(this, id, sig);
				// TODO: tag - authenticate
				setGUIState(true);
				//
			}
			catch (MalformedURLException e) {
				Toast.makeText(this, "host URL is malformed", Toast.LENGTH_SHORT).show();
			}								
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Editor editor = getPreferences(MODE_PRIVATE).edit();
				Boolean auth_ok = data.getBooleanExtra(
									getString(R.string.settings_auth), 
									false
									);
				if(auth_ok) {
					editor.putString(
							getString(R.string.settings_id), 
							data.getStringExtra(getString(R.string.settings_id))
							);
					editor.putString(
							getString(R.string.settings_sig), 
							data.getStringExtra(getString(R.string.settings_sig))
							);
				}
				editor.putBoolean(getString(R.string.settings_auth), auth_ok);
				setGUIState(auth_ok);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// TODO: tag - authenticate
	private class AuthListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			ensureAuthentication();			
			return true;
		}		
	}
	
	private class LogoutListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			myServerInterface = null;
			setGUIState(false);
			Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putBoolean(getString(R.string.settings_auth), false);
			editor.commit();
			return true;
		}
	}
}
