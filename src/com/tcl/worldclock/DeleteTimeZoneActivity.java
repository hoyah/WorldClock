package com.tcl.worldclock;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

public class DeleteTimeZoneActivity extends ListActivity {
	
	TimezoneDatabaseHelper dbh;
	Cursor cursor;
	
	private HashMap<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();
	
	View mSelectAllView;
	CheckBox mSelectAllCheckBox;
	Button mDelete;
	Button mCancel;
	boolean isSelectAll = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.delete_timezone_activity);
		findViews();
		setListeners();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mSelectedMap.clear();
		dbh = new TimezoneDatabaseHelper(DeleteTimeZoneActivity.this);
		cursor = dbh.getTimezoneList();
		setListAdapter(new MyListAdapter(DeleteTimeZoneActivity.this, cursor, mSelectedMap, MyListAdapter.TYPE_DELETE));
		for(int i=0; i<getListView().getCount(); i++){
			mSelectedMap.put(i, false);
		}
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
		mSelectAllView = (View)findViewById(R.id.worldtime_select_all);
		mSelectAllCheckBox = (CheckBox)findViewById(R.id.worldtime_select_all_checkbox);
		mDelete = (Button)findViewById(R.id.clock_delete);
		mCancel = (Button)findViewById(R.id.clock_cancel);
	}

	private void setListeners() {
		mSelectAllView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isSelectAll = !isSelectAll;
				mSelectAllCheckBox.setChecked(isSelectAll);
				for(int i=0; i<getListView().getCount(); i++){
					mSelectedMap.put(i, isSelectAll);
				}
				notifyDataSetChanged();
			}
		});
		
		mDelete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				deleteSelected();
			}
		});
		
		mCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Boolean isSelected = mSelectedMap.get(position);
		if(isSelected == null){
			return;
		}
		isSelected = !isSelected;
		if(isSelected == false && isSelectAll == true){
			isSelectAll = false;
			mSelectAllCheckBox.setChecked(isSelectAll);
		}
		mSelectedMap.put(position, isSelected);
		notifyDataSetChanged();
	}

	private void deleteSelected() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.delete_timezone)
			.setMessage(R.string.delete_timezone_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int position = getListView().getCount() - 1;
					while(position >= 0){
						if(mSelectedMap.get(position)){
							Cursor cursor = (Cursor)getListAdapter().getItem(position);
							TimeZoneInfo  info = new TimeZoneInfo(DeleteTimeZoneActivity.this, cursor);
							dbh.delete(info.timeZoneId);
						}
						position--;
					}
					dialog.dismiss();
					finish();
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			notifyDataSetChanged();
		}
		
	};
	
	private void notifyDataSetChanged(){
		mDelete.setEnabled(mSelectedMap.containsValue(true));
		if(cursor.getCount() > 0){
			((MyListAdapter) getListAdapter()).notifyDataSetChanged();
		}
	}

}
