package org.geometerplus.fbreader.plugin.synchronization.service;

import java.util.HashMap;

import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.fbreader.plugin.synchronization.service.FBData.Book;

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
	public Uri insert(Uri uri, ContentValues values) {
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		if (values == null) {
			return null;
		}
		SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
		long rowId = db.insert(Book.TABLE, null, values);
		
		if (rowId == -1) {
			String hash = values.getAsString(Book.HASH);
			int count = db.update(Book.TABLE, values, Book.HASH + " = '" + hash + "'", null);
			if (count == 0) {
				return null;
			}
		}

		Uri noteUri = ContentUris.withAppendedId(Book.CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(noteUri, null);
		return noteUri;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		if (ourUriMatcher.match(uri) == -1) {
			throw new IllegalArgumentException("Unknown uri:" + uri);	
		}
		String[] projection = new String[] {
				Book.HASH,
				Book.TIMESTAMP,
				Book.POSITION
		};
		
		int count = 0;
		Boolean canPerform = true;
		canPerform &= values.containsKey(Book.HASH);
		canPerform &= values.containsKey(Book.TIMESTAMP);
		canPerform &= values.containsKey(Book.POSITION);

		if (canPerform) {
			long newTimestamp = values.getAsLong(Book.TIMESTAMP);
			String hash = values.getAsString(Book.HASH);
			Position newPosition = 
				new Position(hash, values.getAsString(Book.POSITION), newTimestamp);
			
			Cursor cur = query(Book.CONTENT_URI, projection, 
					Book.HASH + " = '" + hash + "'", null, null);
			if (cur.moveToFirst()) {
				String position = cur.getString(cur.getColumnIndex(Book.POSITION));
				String oldHash = cur.getString(cur.getColumnIndex(Book.HASH));
				long oldTimestamp = cur.getLong(cur.getColumnIndex(Book.TIMESTAMP));
				Position oldPosition = new Position(oldHash, position, oldTimestamp);
				if (newPosition.compare(oldPosition) > 0) {
					SQLiteDatabase db = myFBData.myDatabaseHelper.getWritableDatabase();
					count = db.update(Book.TABLE, values, where, whereArgs);
					getContext().getContentResolver().notifyChange(uri, null);
				}
			}
		}
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
		public final long myTimestamp;
		
		public Position(String hash, TextPosition position, long timestamp){
			myHash = hash;
			myTimestamp = timestamp;
			myPosition = position;
		}
		
		public Position(String hash, String position, long timestamp){
			myHash = hash;
			myTimestamp = timestamp;
			myPosition = stringToTextPosition(position);
		}
		
		public static String textPositionToString(TextPosition textPosition) {
			String ret = String.valueOf(textPosition.ParagraphIndex);
			ret += '&' + String.valueOf(textPosition.ElementIndex);
			ret += '&' + String.valueOf(textPosition.CharIndex);
			return ret;
		}
		
		public static TextPosition stringToTextPosition(String str) {
			String[] parts = str.split("&");
			if (parts.length < 3) {
				return null;
			}
			try {
				return new TextPosition(
						Integer.parseInt(parts[0]), 
						Integer.parseInt(parts[1]), 
						Integer.parseInt(parts[2])
						);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		
		public Position(String stringValue) {
			try {
				String[] parts = stringValue.split("&");
				myHash = parts[0];
				myTimestamp = Long.parseLong(parts[1]);
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
		
		/**
		 * Represents Position as hash&timestamp&paragraphIndex&elementIndex&charIndex
		 * @return string representation of Position
		 */
		public String toString() {
			String stringValue = myHash + "&";
			stringValue += String.valueOf(myTimestamp) + "&";
			stringValue += myPosition.ParagraphIndex + "&";
			stringValue += myPosition.ElementIndex + "&";
			stringValue += myPosition.CharIndex;
			return stringValue;
		}
		
		public int compare(Position cmp) {
			if (cmp == null) {
				return 1;
			}
			if (myPosition.ParagraphIndex > cmp.myPosition.ParagraphIndex) {
				return 1;
			} else if (myPosition.ElementIndex > cmp.myPosition.ElementIndex) {
				return 1;
			} else if (myPosition.CharIndex > cmp.myPosition.CharIndex) {
				return 1;
			} else if (myTimestamp > cmp.myTimestamp) {
				return 1;
			} else if (myTimestamp == cmp.myTimestamp) {
				return 0;
			} else {
				return -1;
			}
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
