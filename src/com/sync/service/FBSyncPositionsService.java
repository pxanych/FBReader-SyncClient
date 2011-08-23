package com.sync.service;

import java.util.LinkedList;
import java.util.List;

import com.sync.R;
import com.sync.ServerInterface;
import com.sync.ServerInterface.ServerInterfaceException;
import com.sync.service.FBData.Book;
import com.sync.service.FBSyncPositionsProvider.Position;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class FBSyncPositionsService extends FBSyncBaseService {
	
	private final int SYNCHRONIZED_ENTRIES_COUNT = 20;
	
	protected AbstractThreadedSyncAdapter getSyncAdapter() {
		if (ourSyncAdapter == null) {
			ourSyncAdapter = new FBSyncPositionsAdapter(this);
		}
		return ourSyncAdapter;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}

	private class FBSyncPositionsAdapter extends AbstractThreadedSyncAdapter {
		
		private final Context myContext;
		
		public FBSyncPositionsAdapter(Context context) {
			super(context, true);
			myContext = context;
		}
		
		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// TODO clean debug messages
			Log.i("FBSyncAdapter", this.getClass().getSimpleName() + 
					" \nAccount: " + account.toString() + "\nAuthority:" + authority +
					" \nBundle count: " + String.valueOf(extras.keySet().size()));
			AccountManager am = AccountManager.get(myContext);
			String id = am.getUserData(account, myContext.getString(R.string.settings_id));
			String sig = am.getUserData(account, myContext.getString(R.string.settings_sig));
			positionsToServer(id, sig);
		}
		
		private void positionsToServer(String id, String signature) {
			String[] projection = new String[]{
					Book.HASH,
					Book.POSITION,
					Book.TIMESTAMP
			};
			
			Cursor c = myContext.getContentResolver().query(Book.CONTENT_URI, projection,
					Book.NEEDS_SYNC + " = 1", null, " " + Book.TIMESTAMP + " DESC ");
			
			int hashIndex = c.getColumnIndex(Book.HASH);
			int positionIndex = c.getColumnIndex(Book.POSITION);
			int timestampIndex = c.getColumnIndex(Book.TIMESTAMP);
			List<Position> positions = new LinkedList<Position>();
			
			for (int i = 0; i < Math.min(c.getCount(), SYNCHRONIZED_ENTRIES_COUNT); ++i) {
				String hash = c.getString(hashIndex);
				String position = c.getString(positionIndex);
				int timestamp = c.getInt(timestampIndex);
				Log.i("com.sync", String.valueOf(i) + ": " + hash + " | " + position + 
						" | " + String.valueOf(timestamp));
				Position pos = new Position(hash + "&" + String.valueOf(timestamp) + "&" + position);
				positions.add(pos);
				c.moveToNext();
			}
			
			String[] reply;
			try {
				ServerInterface serverInterface = new ServerInterface(myContext, id, signature);
				reply = serverInterface.set_positions(positions);
			} 
			catch (ServerInterfaceException e) {
				Log.e("com.sync", e.getMessage() + ", caused by: " + 
						e.getCause().getMessage());
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Book.NEEDS_SYNC, 0);
			for (String hash : reply) {
				myContext.getContentResolver().update(Book.CONTENT_URI, values,
						Book.HASH + " = '" + hash + "'", null);
			}
		}
	}

}
