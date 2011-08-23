package com.sync.service;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Intent;
import android.os.IBinder;

public abstract class FBSyncBaseService extends Service {
	
	protected static AbstractThreadedSyncAdapter ourSyncAdapter = null;

	@Override
	public IBinder onBind(Intent intent) {
		return getSyncAdapter().getSyncAdapterBinder();
	}
	
	protected abstract AbstractThreadedSyncAdapter getSyncAdapter();
	
}
