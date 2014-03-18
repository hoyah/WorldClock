package com.tcl.worldclock;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

public class TimeZoneInfo {
	public static class Columns implements BaseColumns {
		public static final String ID = "_id";
		public static final String TIME_ZONE_ID = "timezone_id";
		public static final String INDEX = "index";
		public static final String DISPLAY_NAME = "display_name";
		public static final String GMT = "gmt";
		public static final String OFF_SET = "off_set";
		public static final String SUMMER_TIME = "summer_time";
		
		public static final int ID_INDEX = 0;
		public static final int TIME_ZONE_ID_INDEX = 1;
		public static final int INDEX_INDEX = 2;
		public static final int DISPLAY_NAME_INEDX = 3;
		public static final int GMT_INEDX = 4;
		public static final int OFF_SET_INEDX = 5;
		public static final int SUMMER_TIME_INDEX = 6;
	}

	public int _id;
	public String timeZoneId;
	public int index;
	public String displayName;
	public String gmt;
	public int offset;
	public int summerTime;
	
	public static final int SUMMERTIME_NONE = 0;
	public static final int SUMMERTIME_ONE_HOUR = 1;
	public static final int SUMMERTIME_TWO_HOUR = 2;

	public TimeZoneInfo() {
		_id = -1;
		summerTime = SUMMERTIME_NONE;
	}

	public TimeZoneInfo(Context context, Cursor c) {
		_id = c.getInt(Columns.ID_INDEX);
		timeZoneId = c.getString(Columns.TIME_ZONE_ID_INDEX);
		index = c.getInt(Columns.INDEX_INDEX);
		displayName = c.getString(Columns.DISPLAY_NAME_INEDX);
		gmt = c.getString(Columns.GMT_INEDX);
		offset = c.getInt(Columns.OFF_SET_INEDX);
		summerTime = c.getInt(Columns.SUMMER_TIME_INDEX);
	}
}