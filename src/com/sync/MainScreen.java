package com.sync;

import com.sync.service.FBSyncAccountAuthenticatorService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainScreen extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent forward = null;
		if (FBSyncAccountAuthenticatorService.accountExists(this)) {
			forward = new Intent(this, FBSettings.class);
		} else {
			forward = new Intent(this, AuthSelectActivity.class);
		}
		startActivity(forward);
		finish();
	}
}
