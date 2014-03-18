package com.tcl.worldclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends CursorAdapter {
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_DELETE = 1;
	public static final int TYPE_SUMMER_TIME = 2;
	
	public static final String M24 = "HH:mm";		//21:30
	public static final String M12 = "hh:mm aa";	//09:30 PM
	private SimpleDateFormat sdf;
	private SimpleDateFormat sdf2;
	
	private boolean is24Hour = false;
	
	private HashMap<Integer, ?> map;
	
	private int type = TYPE_NORMAL;
	
	
	public MyListAdapter(Context context, Cursor c) {
		super(context, c);
		is24Hour = android.text.format.DateFormat.is24HourFormat(context);
		sdf = new SimpleDateFormat(((SimpleDateFormat)DateFormat.getDateFormat(context)).toLocalizedPattern() + " EEEE");
		sdf2 = new SimpleDateFormat(is24Hour ? M24 : M12);
	}
	
	public MyListAdapter(Context context, Cursor c, HashMap<Integer, ?> map, int type) {
		this(context, c);
		this.map = map;
		this.type = type;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TimeZoneInfo info = new TimeZoneInfo(context, cursor);
		
		TextView mCity = (TextView)view.findViewById(R.id.wc_city);
		TextView mZone = (TextView)view.findViewById(R.id.wc_timezone);
		TextView mTime = (TextView)view.findViewById(R.id.wc_time);
		TextView mDate = (TextView)view.findViewById(R.id.wc_date);
		MyAnalogClock mClock = (MyAnalogClock)view.findViewById(R.id.wc_clock);
		CheckBox mCheckbox = (CheckBox)view.findViewById(R.id.wc_checkbox);
		ImageView mSummerTime = (ImageView)view.findViewById(R.id.wc_summertime);
		
		Integer summerTime = null;
		
		//DeleteActivity
		if(type == TYPE_DELETE){
			mCheckbox.setVisibility(View.VISIBLE);
			mCheckbox.setChecked((Boolean)map.get(cursor.getPosition()));
		}
		//SummerTimeActivity
		if(type == TYPE_SUMMER_TIME){
			mSummerTime.setVisibility(View.VISIBLE);
			summerTime = (Integer)map.get(cursor.getPosition());
			if(summerTime == null){
				summerTime = info.summerTime;
			}
			switch(summerTime){
			case TimeZoneInfo.SUMMERTIME_NONE:
				mSummerTime.setImageResource(R.drawable.worldtime_summertime_none);
				break;
			case TimeZoneInfo.SUMMERTIME_ONE_HOUR:
				mSummerTime.setImageResource(R.drawable.worldtime_summertime_1);
				break;
			case TimeZoneInfo.SUMMERTIME_TWO_HOUR:
				mSummerTime.setImageResource(R.drawable.worldtime_summertime_2);
				break;
			default:
				mSummerTime.setImageResource(R.drawable.worldtime_summertime_none);
				break;
			}
		}
		
		//city
		mCity.setText(info.displayName);
		
		//zone
		mZone.setText(info.gmt);
		
		//time
		Calendar calendar = Calendar.getInstance();
		long nowMilliseconds = calendar.getTimeInMillis() - TimeZone.getDefault().getOffset(calendar.getTimeInMillis()) + info.offset;
		
		if(summerTime == null){
			summerTime = info.summerTime;
		}
		if(summerTime == TimeZoneInfo.SUMMERTIME_ONE_HOUR){
			nowMilliseconds += 3600000;
		}else if(summerTime == TimeZoneInfo.SUMMERTIME_TWO_HOUR){
			nowMilliseconds += 7200000;
		}
		
		calendar.setTimeInMillis(nowMilliseconds);
		mTime.setText(sdf2.format(new Date(nowMilliseconds)));
		
		//date
		mDate.setText(sdf.format(new Date(nowMilliseconds)));
		
		//AnalogClock
		mClock.setTime(calendar);
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.worldclock_list_item, parent, false);
	}
	
}
