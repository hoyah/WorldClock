package com.tcl.worldclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;


public class WorldClockActivity extends ListActivity {
	
	TimezoneDatabaseHelper dbh;
	Cursor cursor;
	
	TextView mCity;
	TextView mZone;
	MyAnalogClock mClock;
	TextView mTime;
	TextView mDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		initDefault();
		dbh = new TimezoneDatabaseHelper(WorldClockActivity.this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setDefaultValues();
		cursor = dbh.getTimezoneList();
		setListAdapter(new MyListAdapter(WorldClockActivity.this, cursor));
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

	private void initDefault() {
		View view = getLayoutInflater().inflate(R.layout.worldclock_list_item, null);
		mCity = (TextView)view.findViewById(R.id.wc_city);
		mZone = (TextView)view.findViewById(R.id.wc_timezone);
		mClock = (MyAnalogClock)view.findViewById(R.id.wc_clock);
		mTime = (TextView)view.findViewById(R.id.wc_time);
		mDate = (TextView)view.findViewById(R.id.wc_date);
		getListView().addHeaderView(view, null, false);
	}

	private void setDefaultValues() {
		mCity.setText(TimeZone.getDefault().getDisplayName());
		mZone.setText(AddTimeZoneActivity.getGMT(TimeZone.getDefault().getOffset(System.currentTimeMillis())));
		Calendar calendar = Calendar.getInstance();
		mClock.setTime(calendar);
		SimpleDateFormat sdf = new SimpleDateFormat(((SimpleDateFormat)DateFormat.getDateFormat(this)).toLocalizedPattern() + " EEEE");
		SimpleDateFormat sdf2 = new SimpleDateFormat(android.text.format.DateFormat.is24HourFormat(this) ? MyListAdapter.M24 : MyListAdapter.M12);
		mTime.setText(sdf2.format(new Date(calendar.getTimeInMillis())));
		mDate.setText(sdf.format(new Date(calendar.getTimeInMillis())));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_add:
			startActivity(new Intent(WorldClockActivity.this, AddTimeZoneActivity.class));
			return true;
		case R.id.action_delete:
			startActivity(new Intent(WorldClockActivity.this, DeleteTimeZoneActivity.class));
			return true;
		case R.id.action_summer_time:
			startActivity(new Intent(WorldClockActivity.this, SummerTimeActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.timezone_list_actions, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		//(info.position - 1): we have added a header, which take the first position, for the ListView, so we need to minus 1
		Cursor cursor = (Cursor)getListAdapter().getItem(info.position - 1);
		TimeZoneInfo timeZoneInfo = new TimeZoneInfo(this, cursor);
		switch(item.getItemId()){
		case R.id.action_delete:
			deleteTimeZone(timeZoneInfo);
			return true;
		case R.id.action_summer_time:
			setSummerTime(timeZoneInfo);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void deleteTimeZone(final TimeZoneInfo timeZoneInfo) {
		AlertDialog.Builder builder = new AlertDialog.Builder(WorldClockActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.delete_timezone)
			.setMessage(R.string.delete_timezone_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbh.delete(timeZoneInfo.timeZoneId);
					onStart();
					//notifyDataSetChanged();
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}
	
	private void setSummerTime(final TimeZoneInfo timeZoneInfo) {
		String[] items = new String[]{getString(R.string.worldclock_summertime_off),
				getString(R.string.worldclock_summertime_one),
				getString(R.string.worldclock_summertime_two)};
		AlertDialog.Builder builder = new AlertDialog.Builder(WorldClockActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.worldclock_summertime_title)
			.setSingleChoiceItems(items, timeZoneInfo.summerTime, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == timeZoneInfo.summerTime){
						dialog.dismiss();
						return;
					}
					timeZoneInfo.summerTime = which;
					dbh.update(timeZoneInfo.timeZoneId, timeZoneInfo.summerTime);
					onStart();
					//notifyDataSetChanged();
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			notifyDataSetChanged();
		}
		
	};
	
	private void notifyDataSetChanged(){
		setDefaultValues();
		if(cursor.getCount() > 0){
			((MyListAdapter) getListAdapter()).notifyDataSetChanged();
		}
	}

}
