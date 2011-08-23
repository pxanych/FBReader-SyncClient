package com.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

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
}
