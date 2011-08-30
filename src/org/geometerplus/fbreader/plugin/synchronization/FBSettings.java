package org.geometerplus.fbreader.plugin.synchronization;

import java.io.IOException;

import org.geometerplus.fbreader.plugin.synchronization.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class FBSettings extends PreferenceActivity {
	
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.settings);
		Account account = AccountManager.get(this).
							getAccountsByType(getString(R.string.account_type))[0];
		
		CheckBoxPreference pref = 
			(CheckBoxPreference)findPreference(getString(R.string.checkbox_positions));
		String authority = getString(R.string.authority_positions);
		pref.setOnPreferenceClickListener(new PreferenceClickListener(authority, this));
		pref.setChecked(ContentResolver.getSyncAutomatically(account, authority));
		
		pref = (CheckBoxPreference)findPreference(getString(R.string.checkbox_bookmarks));
		authority = getString(R.string.authority_bookmarks);
		pref.setOnPreferenceClickListener(new PreferenceClickListener(authority, this));
		pref.setChecked(ContentResolver.getSyncAutomatically(account, authority));
		
		pref = (CheckBoxPreference)findPreference(getString(R.string.checkbox_settings));
		authority = getString(R.string.authority_settings);
		pref.setOnPreferenceClickListener(new PreferenceClickListener(authority, this));
		pref.setChecked(ContentResolver.getSyncAutomatically(account, authority));
		
		Preference logoutPreference = findPreference(getString(R.string.preference_logout));
		logoutPreference.setOnPreferenceClickListener(new LogoutClickListener());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	private class PreferenceClickListener implements OnPreferenceClickListener {
		
		private final String myAuthority;
		private final Context myContext;
		
		public PreferenceClickListener(String authority, Context context){
			myAuthority = authority;
			myContext = context;
		}
		
		public boolean onPreferenceClick(Preference preference) {
			CheckBoxPreference pref = (CheckBoxPreference) preference;
			String accountType = myContext.getString(R.string.account_type);
			int syncable = (pref.isChecked()) ? 1 : 0;
			Account account = AccountManager.get(myContext).getAccountsByType(accountType)[0];
			ContentResolver.addPeriodicSync(account, myAuthority, new Bundle(), 1800);
			ContentResolver.setSyncAutomatically(account, myAuthority, syncable == 1);
			return true;
		}
	}
	
	private class LogoutClickListener implements OnPreferenceClickListener {
		public boolean onPreferenceClick(Preference preference) {
			AccountManager accountManager = AccountManager.get(getApplicationContext());
			String accountType = getApplicationContext().getString(R.string.account_type);
			Account account = accountManager.getAccountsByType(accountType)[0];
			AccountManagerFuture<Boolean> result = 
				accountManager.removeAccount(account, null, null);
			try {
				if (result.getResult()){
					startActivity(new Intent(FBSettings.this, AuthSelectActivity.class));
					finish();
				} else {
					Toast.makeText(FBSettings.this, "Can't delete", Toast.LENGTH_LONG).show();
				}
			}
			catch (AuthenticatorException e){
				Toast.makeText(FBSettings.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			catch (IOException e){
				Toast.makeText(FBSettings.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			catch (OperationCanceledException e){
				Toast.makeText(FBSettings.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return true;
			
		}
	}
}
