package org.geometerplus.fbreader.plugin.synchronization.service;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;
import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.android.fbreader.api.ApiClientImplementation.ConnectionListener;
import org.geometerplus.fbreader.plugin.synchronization.ServerInterface;
import org.geometerplus.fbreader.plugin.synchronization.SyncAuth;
import org.geometerplus.fbreader.plugin.synchronization.SyncConstants;
import org.geometerplus.fbreader.plugin.synchronization.ServerInterface.ServerInterfaceException;
import org.geometerplus.fbreader.plugin.synchronization.service.FBData.Book;
import org.geometerplus.fbreader.plugin.synchronization.service.FBSyncPositionsProvider.Position;

import org.geometerplus.fbreader.plugin.synchronization.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class FBSyncPositionsService extends FBSyncBaseService implements ConnectionListener{
	
	private final int SYNCHRONIZED_ENTRIES_COUNT = 20;
	public static final String FBREADER_ACTION = "action";
	public static final int FBREADER_PULL_POSITION = 1;
	public static final int FBREADER_PUSH_POSITION = 2;
	public static final int FBREADER_STARTED = 3;
	private static ApiClientImplementation ourFBReaderApiClient;
	
	
	protected AbstractThreadedSyncAdapter getSyncAdapter() {
		if (ourSyncAdapter == null) {
			ourSyncAdapter = new FBSyncPositionsAdapter(this);
		}
		return ourSyncAdapter;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Service", "starting");
		if (intent.getIntExtra(FBREADER_ACTION, -1) == FBREADER_STARTED) {
			if (ourFBReaderApiClient == null) {
				ourFBReaderApiClient = new ApiClientImplementation(this, this);
			}
		} else {
			if (SyncAuth.hasAccount(this)) {
				new ServiceStarter(intent, ourFBReaderApiClient).start();
			}
		}
		Log.i("Service", "ok");
		return START_STICKY;
	}
	
	public void onConnected() {
		ourFBReaderApiClient.addListener(FBReaderApiListener.getInstance(this));
	}
	
	private class ServiceStarter extends Thread {
		
		private Intent myIntent;
		private ApiClientImplementation myApiClient;
		public ServiceStarter(Intent intent, ApiClientImplementation apiClient) {
			myIntent = intent;
			myApiClient = apiClient;
		}
		
		@Override
		public void run() {
			try {
				int code = myIntent.getIntExtra(FBREADER_ACTION, -1);
				// TODO debug code
				long eventTimestamp = myIntent.getLongExtra("timestamp", -1);
				Log.i(SyncConstants.TAG, "ServiceStarter.run() with code: " + code + " and timestamp:" + eventTimestamp);
				//
				FBSyncPositionsAdapter positionsAdapter = 
					new FBSyncPositionsAdapter(FBSyncPositionsService.this);
				TextPosition position = myApiClient.getPageStart();
				String hash = myApiClient.getBookHash();
				switch (myIntent.getIntExtra(FBREADER_ACTION, 0)) {
					case FBREADER_PUSH_POSITION:
						position = myApiClient.getPageStart();
						positionsAdapter.pushPosition(
								position, 
								System.currentTimeMillis(), 
								hash
								);
					case FBREADER_PULL_POSITION:
						FBSyncPositionsAdapter adapter = (FBSyncPositionsAdapter)ourSyncAdapter;
						Position cachedPosition = adapter.getPositionByHash(hash);
						FBSyncPositionsAdapter.PositionDownloader downloader = 
							adapter.getPositionDownloader(cachedPosition, hash);
						Position serverPosition = downloader.getResult(true, 2000);
						Position positionToSet = null;
						if (downloader.isDone()) {
							
							if (cachedPosition != null) {
								positionToSet = (serverPosition.compare(cachedPosition) > 0) ?
										serverPosition : cachedPosition;
								
							} else {
								positionToSet = serverPosition;
							}
							if (positionToSet != null) {
								myApiClient.setPageStart(positionToSet.myPosition);
							}
						} else {
							serverPosition = downloader.getResult(true, 10000);
							if (cachedPosition != null) {
								positionToSet = (serverPosition.compare(cachedPosition) > 0) ?
										serverPosition : cachedPosition;
								
							} else {
								positionToSet = serverPosition;
							}
							if (positionToSet != null) {
								myApiClient.setPageStart(positionToSet.myPosition);
							}
						}
				}
			}
			catch (ApiException e) {
				Log.e(SyncConstants.TAG, "ApiException in ServiceStarter.run()", e);
			}
		}
	}
	
	
	private class FBSyncPositionsAdapter extends AbstractThreadedSyncAdapter {
		
		private ServerInterface myServerInterface;
		private ContentResolver myContentResolver;
		
		public FBSyncPositionsAdapter(Context context) {
			super(context, true);
			String accountType = getString(R.string.account_type);
			AccountManager accountManager = AccountManager.get(context);
			Account account = accountManager.getAccountsByType(accountType)[0];
			String id = accountManager.getUserData(account, SyncConstants.SETTINGS_ID);
			String sig = accountManager.getUserData(account, SyncConstants.SETTINGS_SIG);
			myServerInterface = new ServerInterface(id, sig);
			myContentResolver = getContentResolver();
		}
		
		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// TODO debug code
			Log.i(SyncConstants.TAG, this.getClass().getSimpleName() + ".onPerformSync()");
			//
			positionsFromServer();
			positionsToServer();
		}
		
		private void positionsToServer() {
			String[] projection = new String[]{
					Book.HASH,
					Book.POSITION,
					Book.TIMESTAMP
			};
			Cursor booksToSync = myContentResolver.query(
					Book.CONTENT_URI, 
					projection, 
					Book.NEEDS_SYNC + " = 1", 
					null, 
					Book.TIMESTAMP + " DESC"
					);
			if (!booksToSync.moveToFirst()) {
				return;
			}
			
			int hashIndex = booksToSync.getColumnIndex(Book.HASH);
			int positionIndex = booksToSync.getColumnIndex(Book.POSITION);
			int timestampIndex = booksToSync.getColumnIndex(Book.TIMESTAMP);
			List<Position> positions = new LinkedList<Position>();
			
			for (int i = 0; i < Math.min(booksToSync.getCount(), SYNCHRONIZED_ENTRIES_COUNT); ++i) {
				String hash = booksToSync.getString(hashIndex);
				String position = booksToSync.getString(positionIndex);
				long timestamp = booksToSync.getLong(timestampIndex);
				Position pos = new Position(hash + "&" + String.valueOf(timestamp) + "&" + position);
				// TODO debug code
				Log.i(SyncConstants.TAG, "Sending to server: " + pos);
				//
				positions.add(pos);
				booksToSync.moveToNext();
			}
			
			String[] reply;
			try {
				reply = myServerInterface.setPositions(positions);
				ContentValues values = new ContentValues();
				values.put(Book.NEEDS_SYNC, 0);
				for (String hash : reply) {
					myContentResolver.update(Book.CONTENT_URI, values,
							Book.HASH + " = '" + hash + "'", null);
				}
			} 
			catch (ServerInterfaceException e) {
				Log.e(SyncConstants.TAG, "", e);
			}
			catch (NullPointerException e) {
				Log.e(SyncConstants.TAG, "", e);
			}
		}
		
		
		private void pushPosition(TextPosition textPosition, long timestamp, String hash) {
			Position oldPosition = getPositionByHash(hash);
			if (Position.compareTextPositions(textPosition, oldPosition.myPosition) > 0) {
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Book.HASH, hash);
			values.put(Book.NEEDS_SYNC, 1);
			values.put(Book.TIMESTAMP, timestamp);
			values.put(Book.POSITION, Position.textPositionToString(textPosition));
			myContentResolver.insert(Book.CONTENT_URI, values);
			positionsToServer();
		}
		
		
//		private TextPosition pullPosition(String hash) {
//			LinkedList<String> books = new LinkedList<String>();
//			books.add(hash);
//			
//			Position[] positions;
//			try {
//				positions = myServerInterface.getPositions(books);
//			} catch (ServerInterfaceException e) {
//				Log.e(SyncConstants.TAG, "", e);
//				return null;
//			}
//			if (positions.length != 0) {
//				Position position = positions[0];
//				Position localPosition = getPositionByHash(hash);
//				if (position.compare(localPosition) <= 0) {
//					return null;
//				}
//				return position.myPosition;
//			}
//			return null;
//		}
		
		
		private void positionsFromServer(){
			String[] projection = new String[] {
					Book.BOOK_ID,
					Book.HASH,
					Book.TIMESTAMP
			};
			
			Cursor c = myContentResolver.query(Book.CONTENT_URI, projection, 
					null, null, Book.TIMESTAMP + " DESC ");
			int hashColumnIndex = c.getColumnIndex(Book.HASH);
			LinkedList<String> books = new LinkedList<String>();
			c.moveToFirst();
			for (int i = 0; i < Math.min(c.getCount(), SYNCHRONIZED_ENTRIES_COUNT); ++i) {
				books.add(c.getString(hashColumnIndex));
				c.moveToNext();
			}
			
			try {
				Position[] positions = myServerInterface.getPositions(books);
				for (Position position : positions) {
					ContentValues values = new ContentValues();
					values.put(Book.TIMESTAMP, position.myTimestamp);
					values.put(Book.POSITION, Position.textPositionToString(position.myPosition));
					myContentResolver.update(Book.CONTENT_URI, values, 
							Book.HASH + " = '" + position.myHash + "'", null);
				}
			} 
			catch(ServerInterfaceException e) {
				Log.e(SyncConstants.TAG, "", e);
				return;
			}
		}
		
		private Position getPositionByHash(String hash) {
			String[] projection = new String[] {
					Book.HASH,
					Book.POSITION,
					Book.TIMESTAMP
			};
			Cursor c = myContentResolver.query(
					Book.CONTENT_URI, 
					projection, 
					Book.HASH + " = '" + hash + "'", null, null);
			
			if(c.moveToFirst()) {
				Position ret = new Position(
						c.getString(c.getColumnIndex(Book.HASH)),
						c.getString(c.getColumnIndex(Book.POSITION)),
						c.getLong(c.getColumnIndex(Book.TIMESTAMP))
						);
				return ret;
			} else {
				return null;
			}
		}
		
		public PositionDownloader getPositionDownloader(Position defaultValue, String hash) {
			return new PositionDownloader(defaultValue, hash);
		}
		
		private class PositionDownloader extends Thread {

			private Boolean myCompleted = false;
			private Position myResult;
			private String myBookHash;
			
			public PositionDownloader(Position defaultValue, String hash) {
				super();
				myResult = defaultValue;
				myBookHash = hash;
			}
			
			public boolean isDone() {
				synchronized (this) {
					return myCompleted;
				}
			}
			
			public Position getResult(boolean waitForServer, int maxMillis) {
				if (isDone()) {
					return myResult;
				}
				synchronized (this) {
					while(maxMillis > 0 && waitForServer) {
						try {
							wait(200);
						}
						catch (InterruptedException e) {
							myCompleted = true;
						}
						if (myCompleted) {
							return myResult;
						}
						maxMillis -= 200;
					}
					return myResult;
				}
			}
			
			@Override
			public void run() {
				LinkedList<String> books = new LinkedList<String>();
				books.add(myBookHash);
				
				Position[] positions = null;
				try {
					positions = myServerInterface.getPositions(books);
				} catch (ServerInterfaceException e) {
					Log.e(SyncConstants.TAG, "", e);
				}
				if (positions != null && positions.length != 0) {
					Position position = positions[0];
					synchronized (this) {
						myResult = position;
						myCompleted = true;
						this.notifyAll();
					}
				} else {
					synchronized (this) {
						myCompleted = true;
						this.notifyAll();
					}
				}
			}
		}
	}
}
