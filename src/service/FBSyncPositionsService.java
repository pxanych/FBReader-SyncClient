package service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class FBSyncPositionsService extends FBSyncBaseService {
	
	protected AbstractThreadedSyncAdapter getSyncAdapter() {
		if (ourSyncAdapter == null) {
			ourSyncAdapter = new FBSyncPositionsAdapter(this);
		}
		return ourSyncAdapter;
	}

	private class FBSyncPositionsAdapter extends AbstractThreadedSyncAdapter {
		
		//private Context myContext;
		
		public FBSyncPositionsAdapter(Context context) {
			super(context, true);
			//myContext = context;
		}
		
		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			Log.i("FBSyncAdapter", "performSync: " + account.toString());
		}
	}

}
