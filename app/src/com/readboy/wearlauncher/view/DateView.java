package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.readboy.wearlauncher.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateView extends TextView{
	
	private Context mContext;
	private DateChangeIntentReceiver mReceiver = new DateChangeIntentReceiver();
	private final String[] WEEK_NAME_SHORT = new String[]{"周日","周一","周二","周三","周四","周五","周六"};
	private final String[] WEEK_NAME_EN = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	private boolean weekShort = false;
	private boolean weeknot = false;
	private String template;
	public DateView(Context context) {
		this(context,null);
	}

	public DateView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateView);
		  
		template = a.getString(R.styleable.DateView_dateTemplate);
		weekShort = a.getBoolean(R.styleable.DateView_weekShort, false);
		weeknot = a.getBoolean(R.styleable.DateView_weeknot, false);
		
		a.recycle();
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

		
		if (template == null || template.isEmpty()){
			template = "yyyy年MM月dd日";
		}
		Log.e(VIEW_LOG_TAG, "template = "+template);
		SimpleDateFormat df = new SimpleDateFormat(template,Locale.getDefault());
        String formattedDate = df.format(now.getTime());
        String weekname;

		weekname = WEEK_NAME_SHORT[(now.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_NAME_SHORT.length];

       	if (weeknot){
       		setText(formattedDate);
       	}else{
       		setText(formattedDate + " " + weekname);
       	}
        
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
