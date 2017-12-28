package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.readboy.wearlauncher.R;

import java.util.Calendar;

public class WeekTextView extends TextView{

	private Context mContext;
	private DateChangeIntentReceiver mReceiver = new DateChangeIntentReceiver();
	private final String[] WEEK_NAME_CN = new String[]{"周日","周一","周二","周三","周四","周五","周六"};
	private final String[] WEEK_NAME_EN = new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};


	private boolean isChinese;
	private boolean isShort;
	public WeekTextView(Context context) {
		this(context,null);
	}

	public WeekTextView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public WeekTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekTextView);

		isChinese = a.getBoolean(R.styleable.WeekTextView_weekCn,false);
		isShort = a.getBoolean(R.styleable.WeekTextView_weekShort,false);

		a.recycle();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDate();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
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

        String weekname;
       	if (isChinese){
			weekname = WEEK_NAME_CN[(now.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_NAME_CN.length];
       	}else{
			weekname = WEEK_NAME_EN[(now.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_NAME_EN.length];
       	}
		setText(weekname);
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
