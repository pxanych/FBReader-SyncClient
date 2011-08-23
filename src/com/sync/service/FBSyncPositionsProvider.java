package com.sync.service;

import java.util.HashMap;

import org.geometerplus.android.fbreader.api.TextPosition;
import com.sync.service.FBData.Book;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class FBSyncPositionsProvider extends FBSyncBaseContentProvider {
	
	public static final String AUTHORITY = "com.sync.fbdata.positions";
	private static final UriMatcher ourUriMatcher;
	private static final HashMap<String, String> ourBookProjectionMap;
	private static final int BOOKS = 1;
	
	
	@Override
	public boolean onCreate() {
		myFBData = FBData.getInstance(getContext());
		return super.onCreate();
	}
	
	private FBData myFBData;
	
	@Override
	public String getType(Uri uri) {
		switch (ourUriMatcher.match(uri)) {
		case BOOKS:
			return Book.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(Book.TABLE);
		queryBuilder.setProjectionMap(ourBookProjectionMap);
		
		SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
		Cursor c = queryBuilder.query(db, projection, selection, 
				selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
		long rowId = db.insert(Book.TABLE, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Book.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		// TODO update on new_timestamp > old_timestamp
		String[] projection = new String[] {
				Book.HASH,
				Book.TIMESTAMP
		};
		
		int count = 0;
		Boolean canPerform = true;
		canPerform &= values.containsKey(Book.HASH);
		canPerform &= values.containsKey(Book.TIMESTAMP);
		canPerform &= values.containsKey(Book.POSITION);

		if (canPerform) {
			Integer newTimestamp = values.getAsInteger(Book.TIMESTAMP);
			String hash = values.getAsString(Book.HASH);
			
			Cursor bookToUpdate = query(Book.CONTENT_URI, projection, 
					Book.HASH + " = " + hash, null, null);
			if (bookToUpdate.getCount() > 0) {
				Integer oldTimestamp = bookToUpdate.getInt(bookToUpdate.getColumnIndex(Book.TIMESTAMP));
				if (newTimestamp > oldTimestamp) {
					SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
					count = db.update(Book.TABLE, values, where, whereArgs);
				}
			}
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		
		SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
		int count = db.delete(Book.TABLE, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}


	public static class Position {
		public final String myHash;
		public final TextPosition myPosition;
		public final int myTimestamp;
		
		public Position(String hash, TextPosition position, int timestamp){
			myHash = hash;
			myTimestamp = timestamp;
			myPosition = position;
		}
		
		public Position(String stringValue) {
			try {
				String[] parts = stringValue.split("&");
				myHash = parts[0];
				myTimestamp = Integer.parseInt(parts[1]);
				myPosition = new TextPosition(
						Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]),
						Integer.parseInt(parts[4])
						);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				throw new IllegalArgumentException("Can't deserialize Position: bad data");
			}
		}
		
		public String toString() {
			String stringValue = myHash + "&";
			stringValue += String.valueOf(myTimestamp) + "&";
			stringValue += myPosition.ParagraphIndex + "&";
			stringValue += myPosition.ElementIndex + "&";
			stringValue += myPosition.CharIndex;
			return stringValue;
		}
	}
	
	static {
		ourUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		ourUriMatcher.addURI(AUTHORITY, Book.TABLE, BOOKS);
		
		ourBookProjectionMap = new HashMap<String, String>();
		ourBookProjectionMap.put(Book.BOOK_ID, Book.BOOK_ID);
		ourBookProjectionMap.put(Book.HASH, Book.HASH);
		ourBookProjectionMap.put(Book.TITLE, Book.TITLE);
		ourBookProjectionMap.put(Book.AUTHOR, Book.AUTHOR);
		ourBookProjectionMap.put(Book.POSITION, Book.POSITION);
		ourBookProjectionMap.put(Book.TIMESTAMP, Book.TIMESTAMP);
	}
}
