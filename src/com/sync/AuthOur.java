package com.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;

public class AuthOur extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.auth_our);
	}
	
	
    private void performAuth() {
    	
    }
	
	private Boolean addAccount(AccountManager accountManager, String id, String signature) {
		Account account = new Account(
				getString(R.string.account_name), 
				getString(R.string.account_type)
				);
		Bundle userData = new Bundle();
		userData.putString(getString(R.string.settings_id), id);
		userData.putString(getString(R.string.settings_sig), signature);
		ContentResolver.setIsSyncable(account, getString(R.string.authority_positions), 1);
		ContentResolver.setIsSyncable(account, getString(R.string.authority_bookmarks), 1);
		ContentResolver.setIsSyncable(account, getString(R.string.authority_settings), 1);
		ContentResolver.addPeriodicSync(account, getString(R.string.authority_positions), new Bundle(), 1800);
		ContentResolver.addPeriodicSync(account, getString(R.string.authority_bookmarks), new Bundle(), 1800);
		ContentResolver.setSyncAutomatically(account, getString(R.string.authority_positions), true);
		ContentResolver.setSyncAutomatically(account, getString(R.string.authority_bookmarks), true);
		return accountManager.addAccountExplicitly(account, "", userData);
	}
	
	
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
}
