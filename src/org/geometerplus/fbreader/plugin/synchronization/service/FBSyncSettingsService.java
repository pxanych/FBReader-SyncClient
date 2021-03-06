package org.geometerplus.fbreader.plugin.synchronization.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class FBSyncSettingsService extends FBSyncBaseService {
	
	protected AbstractThreadedSyncAdapter getSyncAdapter() {
		if (ourSyncAdapter == null) {
			ourSyncAdapter = new FBSyncSettingsAdapter(this);
		}
		return ourSyncAdapter;
	}

	private class FBSyncSettingsAdapter extends AbstractThreadedSyncAdapter {
		
		//private Context myContext;
		
		public FBSyncSettingsAdapter(Context context) {
			super(context, true);
			//myContext = context;
		}
		
		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			Log.i("FBSyncAdapter", this.getClass().getSimpleName() + 
					" \nAccount: " + account.toString() + "\nAuthority:" + authority +
					" \nBundle count: " + String.valueOf(extras.keySet().size()));
		}
	}

}
