package org.geometerplus.fbreader.plugin.synchronization.service;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.provider.BaseColumns;

public class FBData {
	
	public static final String DB_NAME = "fbsync_plugin_db";
	private static FBData ourInstanse = null;
	
	public static FBData getInstance(Context context) {
		return (ourInstanse != null) ? ourInstanse : new FBData(context);
	}
	
	private FBData(Context context) {
		myContext = context;
		myDatabaseHelper = new DatabaseHelper(
				myContext, 
				DB_NAME, 
				null, 
				1
				);
	}
	
	private Context myContext;
	public DatabaseHelper myDatabaseHelper;
	
	public class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
							CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String bookQuery = "create table `" + Book.TABLE + "` (" +
					Book.BOOK_ID + " int, " + 
					Book.HASH + " varchar(64), " +
					Book.TITLE + " text, " +
					Book.AUTHOR + " text, " +
					Book.POSITION + " varchar(64), " +
					Book.TIMESTAMP + " int NOT NULL, " +
					Book.NEEDS_SYNC + " int );";
			db.execSQL(bookQuery);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
	
	
	public static class Book implements BaseColumns {
		
		private Book(){};
		
		public static final String TABLE = "book";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" +
				FBSyncPositionsProvider.AUTHORITY + "/" + TABLE);
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.sync.books";
		
		public static final String BOOK_ID = "_id";
		
		public static final String TITLE = "title";
		
		public static final String AUTHOR = "author";
		
		public static final String HASH = "hash";
		
		public static final String POSITION = "position";
		
		public static final String TIMESTAMP = "timestamp";
		
		public static final String NEEDS_SYNC = "needs_sync";
	}
}
