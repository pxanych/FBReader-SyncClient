package com.sync;

import service.FBSyncAccountAuthenticatorService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent forward = null;
		if (FBSyncAccountAuthenticatorService.accountExists(this)) {
			forward = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
		} else {
			forward = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
		}
		String[] authorities = new String[]{
				getString(R.string.authority_positions), 
				getString(R.string.authority_bookmarks), 
				getString(R.string.authority_settings)
				};
		forward.putExtra(android.provider.Settings.EXTRA_AUTHORITIES, authorities);
		startActivity(forward);
		finish();
	}
}
