package com.sync;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

public class SyncLaunch extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);	
		Intent syncMenu = new Intent();
		syncMenu.setClass(getApplicationContext(), SyncMenu.class);
		startActivity(syncMenu);
		finish();
	}
}
