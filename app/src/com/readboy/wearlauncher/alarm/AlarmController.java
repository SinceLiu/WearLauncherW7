package com.readboy.wearlauncher.alarm;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import java.util.ArrayList;

public class AlarmController {
	private static final String TAG = "AlarmController";
	
	private final Context mContext;
	private final int mAlarmIconId = R.drawable.stat_sys_alarm;
	private final int mAlarmNotIconId = R.drawable.stat_sys_alarm_not_drawable;
	private ArrayList<ImageView> mAlarmIconViews = new ArrayList<ImageView>();
	
	public AlarmController(Context context){
		mContext = context;
	}

	private final Handler mHandler = new Handler();  
	  
	private final ContentObserver mAlarmObserver = new ContentObserver(mHandler) {  
	    @Override  
	    public void onChange(boolean selfChange) {  
	    	handleStateChanged();
	    }  
	};  
	
	public void resume(){
		mContext.getContentResolver().registerContentObserver( 
				Settings.System.getUriFor(Settings.System.NEXT_ALARM_FORMATTED),  
				false,  
				mAlarmObserver);  
		handleStateChanged();
	}
	
	public void pause(){
		mContext.getContentResolver().unregisterContentObserver(mAlarmObserver);
	}
	
	public void addAlarmIconView(ImageView v){
		mAlarmIconViews.add(v);
    }
	
	public void fireCallbacks() {
		handleStateChanged();
	}
	
	void handleStateChanged() {
		String alarm = Settings.System.getString(mContext.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
    	boolean hasAlarm = !TextUtils.isEmpty(alarm);
    	int N = mAlarmIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mAlarmIconViews.get(i);
			v.setImageResource(mAlarmIconId);
            if (!hasAlarm) {
				//v.setImageResource(mAlarmNotIconId);
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
	}
}
