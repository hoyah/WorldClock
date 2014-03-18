package com.tcl.worldclock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class SummerTimeActivity extends ListActivity {
	
	TimezoneDatabaseHelper dbh;
	Cursor cursor;
	
	private HashMap<Integer, Integer> mSummerTimeMap = new HashMap<Integer, Integer>();
	
	Button mOk;
	Button mCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.summer_time_activity);
		findViews();
		setListeners();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSummerTimeMap.clear();
		dbh = new TimezoneDatabaseHelper(SummerTimeActivity.this);
		cursor = dbh.getTimezoneList();
		setListAdapter(new MyListAdapter(SummerTimeActivity.this, cursor, mSummerTimeMap, MyListAdapter.TYPE_SUMMER_TIME));
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		cursor.close();
		dbh.close();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void findViews() {
		mOk = (Button)findViewById(R.id.btn_ok);
		mCancel = (Button)findViewById(R.id.btn_cancel);
	}

	private void setListeners() {
		mOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setSummserTime();
				finish();
			}
		});
		
		mCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void setSummserTime(){
		if(mSummerTimeMap.isEmpty()){
			return;
		}
		Iterator<Entry<Integer, Integer>> iterator = mSummerTimeMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer, Integer> entry = iterator.next();
			int position = entry.getKey();
			int summerTime = entry.getValue();
			Cursor cursor = (Cursor)getListAdapter().getItem(position);
			TimeZoneInfo info = new TimeZoneInfo(this, cursor);
			if(summerTime != info.summerTime){
				dbh.update(info.timeZoneId, summerTime);
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Integer summerTime = mSummerTimeMap.get(position);
		if(summerTime == null){
			Cursor cursor = (Cursor)getListAdapter().getItem(position);
			TimeZoneInfo info = new TimeZoneInfo(this, cursor);
			summerTime = info.summerTime;
		}
		
		if(summerTime == TimeZoneInfo.SUMMERTIME_NONE){
			summerTime = TimeZoneInfo.SUMMERTIME_ONE_HOUR;
		}else if(summerTime == TimeZoneInfo.SUMMERTIME_ONE_HOUR){
			summerTime = TimeZoneInfo.SUMMERTIME_TWO_HOUR;
		}else if(summerTime == TimeZoneInfo.SUMMERTIME_TWO_HOUR){
			summerTime = TimeZoneInfo.SUMMERTIME_NONE;
		}
		
		mSummerTimeMap.put(position, summerTime);
		notifyDataSetChanged();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			notifyDataSetChanged();
		}
		
	};
	
	private void notifyDataSetChanged(){
		mOk.setEnabled(mSummerTimeMap.containsValue(1) || mSummerTimeMap.containsValue(2));
		if(cursor.getCount() > 0){
			((MyListAdapter) getListAdapter()).notifyDataSetChanged();
		}
	}

}
