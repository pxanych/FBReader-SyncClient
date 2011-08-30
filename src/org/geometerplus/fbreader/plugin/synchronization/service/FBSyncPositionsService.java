package org.geometerplus.fbreader.plugin.synchronization.service;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.plugin.synchronization.ServerInterface;
import org.geometerplus.fbreader.plugin.synchronization.SyncConstants;
import org.geometerplus.fbreader.plugin.synchronization.ServerInterface.ServerInterfaceException;
import org.geometerplus.fbreader.plugin.synchronization.service.FBData.Book;
import org.geometerplus.fbreader.plugin.synchronization.service.FBSyncPositionsProvider.Position;

import org.geometerplus.fbreader.plugin.synchronization.R;

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
		private ServerInterface myServerInterface;
		
		public FBSyncPositionsAdapter(Context context) {
			super(context, true);
			myContext = context;
			String accountType = myContext.getString(R.string.account_type);
			AccountManager accountManager = AccountManager.get(myContext);
			Account account = accountManager.getAccountsByType(accountType)[0];
			String id = accountManager.getUserData(account, SyncConstants.SETTINGS_ID);
			String sig = accountManager.getUserData(account, SyncConstants.SETTINGS_SIG);
			try {
				myServerInterface = new ServerInterface(id, sig);
			} 
			catch (ServerInterfaceException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// TODO clean debug messages
			Log.i("FBSyncAdapter", this.getClass().getSimpleName() + 
					" \nAccount: " + account.toString() + "\nAuthority:" + authority +
					" \nBundle count: " + String.valueOf(extras.keySet().size()));
			positionsFromServer();
			positionsToServer();
		}
		
		private void positionsToServer() {
			String[] projection = new String[]{
					Book.HASH,
					Book.POSITION,
					Book.TIMESTAMP
			};
			
			Cursor booksToSync = myContext.getContentResolver().query(Book.CONTENT_URI, projection,
					Book.NEEDS_SYNC + " = 1", null, " " + Book.TIMESTAMP + " DESC ");
			
			int hashIndex = booksToSync.getColumnIndex(Book.HASH);
			int positionIndex = booksToSync.getColumnIndex(Book.POSITION);
			int timestampIndex = booksToSync.getColumnIndex(Book.TIMESTAMP);
			List<Position> positions = new LinkedList<Position>();
			
			for (int i = 0; i < Math.min(booksToSync.getCount(), SYNCHRONIZED_ENTRIES_COUNT); ++i) {
				String hash = booksToSync.getString(hashIndex);
				String position = booksToSync.getString(positionIndex);
				int timestamp = booksToSync.getInt(timestampIndex);
				Log.i("com.sync", String.valueOf(i) + ": " + hash + " | " + position + 
						" | " + String.valueOf(timestamp));
				Position pos = new Position(hash + "&" + String.valueOf(timestamp) + "&" + position);
				positions.add(pos);
				booksToSync.moveToNext();
			}
			
			String[] reply;
			try {
				reply = myServerInterface.setPositions(positions);
			} 
			catch (ServerInterfaceException e) {
				Log.e("com.sync", e.getMessage() + ", caused by: " + 
						e.getCause().getClass().getSimpleName() + " : " + 
						e.getCause().getMessage() + " in pts");
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Book.NEEDS_SYNC, 0);
			for (String hash : reply) {
				myContext.getContentResolver().update(Book.CONTENT_URI, values,
						Book.HASH + " = '" + hash + "'", null);
			}
		}
		
		private void positionsFromServer(){
			String[] projection = new String[] {
					Book.BOOK_ID,
					Book.HASH,
					Book.TIMESTAMP
			};
			
			Cursor c = myContext.getContentResolver().query(Book.CONTENT_URI, projection, 
					null, null, Book.TIMESTAMP + " DESC ");
			int hashColumnIndex = c.getColumnIndex(Book.HASH);
			LinkedList<String> books = new LinkedList<String>();
			for (int i = 0; i < c.getCount(); ++i) {
				books.push(c.getString(hashColumnIndex));
				c.moveToNext();
			}
			
			try {
				Position[] positions = myServerInterface.getPositions(books);
				for (Position position : positions) {
					ContentValues values = new ContentValues();
					String[] positionParts = position.toString().split("&", 3);
					values.put(Book.TIMESTAMP, Integer.parseInt(positionParts[1]));
					values.put(Book.POSITION, positionParts[2]);
					myContext.getContentResolver().update(Book.CONTENT_URI, values, 
							Book.HASH + " = " + positionParts[0], null);
				}
			} 
			catch(ServerInterfaceException e) {
				Log.e("com.sync", e.getMessage() + ", caused by: " + 
						e.getCause().getClass().getSimpleName() + " : " + 
						e.getCause().getMessage() + " in pfs");
				return;
			}
			
			
		}
	}

}
