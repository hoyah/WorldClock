package com.tcl.worldclock;

import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)){
			
			List<HashMap> list = AddTimeZoneActivity.getZones(context);
			
			TimezoneDatabaseHelper dbh = new TimezoneDatabaseHelper(context);
			Cursor cursor = dbh.getTimezoneList();
			if(cursor.moveToFirst()){
				do{
					dbh.update(cursor.getString(TimeZoneInfo.Columns.TIME_ZONE_ID_INDEX), 
							(String)list.get(cursor.getInt(TimeZoneInfo.Columns.INDEX_INDEX)).get(AddTimeZoneActivity.KEY_DISPLAYNAME));
				}while(cursor.moveToNext());
			}
			cursor.close();
			dbh.close();
		}
	}

}
