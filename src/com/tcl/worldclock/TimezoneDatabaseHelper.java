package com.tcl.worldclock;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TimezoneDatabaseHelper {
	
	private TimezoneDatabaseOpenHelper dh;
	
	public TimezoneDatabaseHelper(Context context) {
		dh = new TimezoneDatabaseOpenHelper(context);
	}
	
	public boolean insert(Map map){
		SQLiteDatabase db = dh.getWritableDatabase();
		Cursor cursor = db.query(TimezoneDatabaseOpenHelper.TABLE_NAME, new String[]{"timezone_id"}, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				if(cursor.getString(cursor.getColumnIndexOrThrow("timezone_id")).equals(map.get(AddTimeZoneActivity.KEY_ID))){
					cursor.close();
					db.close();
					return false;
				}
			}while(cursor.moveToNext());
		}
		
		db.execSQL("insert into " + TimezoneDatabaseOpenHelper.TABLE_NAME + "(timezone_id, _index, display_name, gmt, offset, summer_time) values(?, ?, ?, ?, ?, ?)", 
				new Object[]{map.get(AddTimeZoneActivity.KEY_ID), 
					map.get(AddTimeZoneActivity.KEY_INDEX), 
					map.get(AddTimeZoneActivity.KEY_DISPLAYNAME),
					map.get(AddTimeZoneActivity.KEY_GMT),
					map.get(AddTimeZoneActivity.KEY_OFFSET), 0});
		cursor.close();
		db.close();
		return true;
	}
	
	public void update(String timezone_id, int summer_time) {
		SQLiteDatabase db = dh.getWritableDatabase();
		db.execSQL("update " + TimezoneDatabaseOpenHelper.TABLE_NAME + " set summer_time = ? where timezone_id = ?", 
				new Object[]{summer_time, timezone_id});
		db.close();
	}
	
	public void update(String timezone_id, String display_name) {
		SQLiteDatabase db = dh.getWritableDatabase();
		db.execSQL("update " + TimezoneDatabaseOpenHelper.TABLE_NAME + " set display_name = ? where timezone_id = ?", 
				new Object[]{display_name, timezone_id});
		db.close();
	}
	
	public void delete(String timezone_id){
		SQLiteDatabase db = dh.getWritableDatabase();
		db.execSQL("delete from " + TimezoneDatabaseOpenHelper.TABLE_NAME + " where timezone_id = ?", new String[]{timezone_id});
		db.close();
	}
	
	public Cursor getTimezoneList() {
		SQLiteDatabase db = dh.getWritableDatabase();
		Cursor cursor = db.query(TimezoneDatabaseOpenHelper.TABLE_NAME, new String[]{"_id", "timezone_id", "_index", "display_name", "gmt", "offset", "summer_time"}, null, null, null, null, null);
		return cursor;
	}
	
	public void close() {
		dh.close();
	}

}
