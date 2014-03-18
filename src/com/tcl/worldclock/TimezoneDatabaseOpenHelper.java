package com.tcl.worldclock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimezoneDatabaseOpenHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "worldclock.db";
	public static final String TABLE_NAME = "timezones";
	private static final int VERSION = 1;
	
	private static final String DB_CREATE = "create table " + TABLE_NAME +
			"(_id integer primary key autoincrement," +
			"timezone_id text not null," +
			"_index integer not null," +
			"display_name text not null," +
			"gmt text not null," +
			"offset long not null," +
			"summer_time integer not null)";

	public TimezoneDatabaseOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_NAME);
		onCreate(db);
	}

}
