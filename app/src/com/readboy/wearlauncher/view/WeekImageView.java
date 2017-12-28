package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import java.util.Calendar;

public class WeekImageView extends ImageView {

	private Context mContext;
	private DateChangeIntentReceiver mReceiver = new DateChangeIntentReceiver();

	private int[] WEEK_DRAWABLE = new int[]{
			R.drawable.week_sun,
			R.drawable.week_mon,
			R.drawable.week_tue,
			R.drawable.week_wed,
			R.drawable.week_thu,
			R.drawable.week_fri,
			R.drawable.week_sat
	};

	public WeekImageView(Context context) {
		this(context,null);
	}

	public WeekImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public WeekImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDate();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		mContext.registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mContext.unregisterReceiver(mReceiver);
	}
	
	private void setDate(){
		Calendar now = Calendar.getInstance();

		setBackgroundResource(WEEK_DRAWABLE[(now.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_DRAWABLE.length]);
	}

	 class DateChangeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
			if (TextUtils.isEmpty(action)) {
				return ;
			}
			setDate();
        }
    }
}
