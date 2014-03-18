package com.tcl.worldclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class AddTimeZoneActivity extends ListActivity {
	
	private static final String TAG = "AddTimeZoneActivity";
    public static final String KEY_ID = "id";
    public static final String KEY_DISPLAYNAME = "name";
    public static final String KEY_INDEX = "index";
    public static final String KEY_GMT = "gmt";
    public static final String KEY_OFFSET = "offset";
    public static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;
    private static final int HOURS_24 = 24 * HOURS_1;
    private static final int HOURS_HALF = HOURS_1 / 2;
    
    private static final int MENU_TIMEZONE = Menu.FIRST+1;
    private static final int MENU_ALPHABETICAL = Menu.FIRST;
    
    // Initial focus position
    private static int mDefault;
    
    private boolean mSortedByTimezone;

    private SimpleAdapter mTimezoneSortedAdapter;
    private SimpleAdapter mAlphabeticalAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		String[] from = new String[]{KEY_DISPLAYNAME, KEY_GMT};
		int[] to = new int[]{android.R.id.text1, android.R.id.text2};
		
		MyComparator comparator = new MyComparator(KEY_OFFSET);
		
		List<HashMap> timezoneSortedList = getZones(AddTimeZoneActivity.this);
		Collections.sort(timezoneSortedList, comparator);
		mTimezoneSortedAdapter = new SimpleAdapter(this, (List)timezoneSortedList, android.R.layout.simple_list_item_2, from, to);
		
		List<HashMap> alphabeticalList = new ArrayList<HashMap>(timezoneSortedList);
		comparator.setSortingKey(KEY_DISPLAYNAME);
		Collections.sort(alphabeticalList, comparator);
		mAlphabeticalAdapter = new SimpleAdapter(this, (List)alphabeticalList, android.R.layout.simple_list_item_2, from, to);
		
		//sets the adapter
		setSorting(true);
		
		getListView().setSelection(mDefault);
		
		setResult(RESULT_CANCELED);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ALPHABETICAL, 0, R.string.action_sort_alphabetically);
		menu.add(0, MENU_TIMEZONE, 0, R.string.action_sort_by_timezone);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mSortedByTimezone){
			menu.findItem(MENU_TIMEZONE).setVisible(false);
			menu.findItem(MENU_ALPHABETICAL).setVisible(true);
		}else{
			menu.findItem(MENU_TIMEZONE).setVisible(true);
			menu.findItem(MENU_ALPHABETICAL).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case MENU_TIMEZONE:
			setSorting(true);
			return true;
		case MENU_ALPHABETICAL:
			setSorting(false);
			return true;
		default:
			return false;
		}
	}

	private void setSorting(boolean timezone) {
		getListView().setAdapter(timezone ? mTimezoneSortedAdapter : mAlphabeticalAdapter);
        mSortedByTimezone = timezone;
    }
	
	static List<HashMap> getZones(Context context) {
        List<HashMap> myData = new ArrayList<HashMap>();
        long date = Calendar.getInstance().getTimeInMillis();
        int index = 0;
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(myData, id, displayName, date, index++);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted timezones.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read timezones.xml file");
        }

        return myData;
    }
	
	protected static void addItem(List<HashMap> myData, String id, String displayName, 
            long date, int index) {
        HashMap map = new HashMap();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        map.put(KEY_INDEX, index);
        TimeZone tz = TimeZone.getTimeZone(id);
        int offset = tz.getOffset(date);
        
        map.put(KEY_GMT, getGMT(offset));
        map.put(KEY_OFFSET, offset);
        
        if (id.equals(TimeZone.getDefault().getID())) {
            mDefault = myData.size();
        }
        
        myData.add(map);
    }
	
	public static String getGMT(int offset){
		int p = Math.abs(offset);
		StringBuilder sb = new StringBuilder();
		sb.append("GMT");
		if(offset < 0) {
			sb.append("-");
		}else{
			sb.append("+");
		}
		sb.append(p/HOURS_1);
		sb.append(":");
		int min = p/60000;
		min %= 60;
		
		if(min < 10){
			sb.append("0");
		}
		sb.append(min);
		
		return sb.toString();
	}
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Map map = (Map) l.getItemAtPosition(position);
		TimezoneDatabaseHelper dbh = new TimezoneDatabaseHelper(AddTimeZoneActivity.this);
		boolean insertSuccess = dbh.insert(map);
		dbh.close();
		if(insertSuccess){
			finish();
		}else{
			Toast.makeText(AddTimeZoneActivity.this, R.string.worldclock_already_added, Toast.LENGTH_SHORT).show();
		}
	}


	private static class MyComparator implements Comparator<HashMap> {
        private String mSortingKey; 
        
        public MyComparator(String sortingKey) {
            mSortingKey = sortingKey;
        }
        
        public void setSortingKey(String sortingKey) {
            mSortingKey = sortingKey;
        }
        
        public int compare(HashMap map1, HashMap map2) {
            Object value1 = map1.get(mSortingKey);
            Object value2 = map2.get(mSortingKey);

            /* 
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            if (!isComparable(value1)) {
                return isComparable(value2) ? 1 : 0;
            } else if (!isComparable(value2)) {
                return -1;
            }
            
            return ((Comparable) value1).compareTo(value2);
        }
        
        private boolean isComparable(Object value) {
            return (value != null) && (value instanceof Comparable); 
        }
    }

}
