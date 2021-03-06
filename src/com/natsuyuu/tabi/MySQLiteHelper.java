package com.natsuyuu.tabi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class MySQLiteHelper extends SQLiteOpenHelper {

	  public static final String TABLE_NAME = "user_data";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_KEY = "key";
	  public static final String COLUMN_VALUE = "value";
	
	  private static final String DATABASE_NAME = "user_data.db";
	  private static final int DATABASE_VERSION = 1;
	
	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_NAME + "(" + COLUMN_ID
	      + " integer primary key autoincrement, " + COLUMN_KEY
	      + " text not null," + COLUMN_VALUE + " text not null);";
	
	  public MySQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	
	  @Override
	  public void onCreate(SQLiteDatabase database) {
		  Log.d("TABI", "SQL EXEC:"+DATABASE_CREATE);
	    database.execSQL(DATABASE_CREATE);
	  }
	
	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(MySQLiteHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	  }

} 
