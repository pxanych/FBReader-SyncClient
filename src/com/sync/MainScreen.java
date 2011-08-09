package com.sync;

import java.net.MalformedURLException;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
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
			authPreference.setTitle(R.string.sync_authenticate_summary);
			authPreference.setOnPreferenceClickListener(new AuthListener());
		}
		forceSyncPreference.setEnabled(authentiacated);
	}
	//
	
	protected void ensureAuthentication() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		if (settings.getBoolean(getString(R.string.settings_auth), false) == false) {
			Dialog authDialog = new SyncAuth(this, new OnSyncAuthDismissListener());
			authDialog.setTitle("auth dialog");
			authDialog.show();
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
	//
	
	private class OnSyncAuthDismissListener implements OnDismissListener {
		public void onDismiss(DialogInterface dialog) {
			SyncAuth authDialog = (SyncAuth)dialog;
			if (authDialog.myAuthOk) {
				SharedPreferences settings = getPreferences(MODE_PRIVATE);
				Editor editor = settings.edit();
				editor.putString(getString(R.string.settings_id), authDialog.myID);
				editor.putString(getString(R.string.settings_sig), authDialog.mySIG);
				try {
					myServerInterface = new ServerInterface(
							getApplicationContext(), 
							authDialog.myID, 
							authDialog.mySIG
							);
					editor.putBoolean(getString(R.string.settings_auth), true);
					// TODO: tag - authenticate
					setGUIState(true);
					//
				}
				catch (MalformedURLException e) {
					Toast.makeText(
							getApplicationContext(),
							"host URL is malformed",
							Toast.LENGTH_SHORT
							).show();
				}
				editor.commit();
			}
		}
	}
}
