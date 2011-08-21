package service;

import com.sync.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class FBData {
	
	public static final String BOOK_TABLE = "book";
	public static final String DB_NAME = "fbsync_plugin_db";
	private static FBData ourInstanse = null;
	
	public static FBData getInstance(Context context) {
		return (ourInstanse != null) ? ourInstanse : new FBData(context);
	}
	
	private FBData(Context context) {
		myContext = context;
		myDB = new DatabaseHelper(
				myContext, 
				DB_NAME, 
				null, 
				1
				).getWritableDatabase();
	}
	
	private Context myContext;
	private SQLiteDatabase myDB;
	
	
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		private Context myContext;
		
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			myContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String bookQuery = "create table `" + DB_NAME +"`.`" + BOOK_TABLE + "` (" +
					"";
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}
	
}
