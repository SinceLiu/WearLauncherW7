package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.readboy.wearlauncher.R;

import java.util.Calendar;
import java.util.Date;

public class AnalogDate extends View{
	private Context mContext;
    private Drawable mMonthDrawable;
    private Drawable mWeekDrawable;
    private int mMonth;
    private int mWeek;
    
	private DateChangeIntentReceiver mReceiver = new DateChangeIntentReceiver();
	public AnalogDate(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		
	     TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogDate);
	     mWeekDrawable = a.getDrawable(R.styleable.AnalogDate_analogWeek);
	     mMonthDrawable = a.getDrawable(R.styleable.AnalogDate_analogMonth);
	     
	     if(mMonthDrawable == null){
	    	 mMonthDrawable = context.getResources().getDrawable(R.drawable.watch_type_black_d_analogmonth);    	 
	     }
	     if(mWeekDrawable == null){
	    	 mWeekDrawable = context.getResources().getDrawable(R.drawable.watch_type_black_d_analogweek);    	 
	     }
	     
	     a.recycle();
	     
	     setDate();
	}
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDate();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		mContext.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mContext.unregisterReceiver(mReceiver);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
	    int availableWidth = getWidth();
	    int availableHeight = getHeight();

	    int x = availableWidth / 2;
	    int y = availableHeight / 2;
	    
	    drawHand(canvas, mWeekDrawable, x, y, mWeek / 7.0f * 240);
	    drawHand(canvas, mMonthDrawable, x, y, mMonth / 12.0f * 240);
		
	}
	private class DateChangeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
			if (TextUtils.isEmpty(action)) {
				return;
			}
			setDate();
        }
    }
	private void setDate(){
		Calendar now = Calendar.getInstance();
		Date date = now.getTime();
		mMonth = date.getMonth();
		mWeek = (now.get(Calendar.DAY_OF_WEEK) - 1) % 7;
		
		invalidate();
	
	}
    private void drawHand(Canvas canvas, Drawable hand, int x, int y, float angle) {
        canvas.save();
        canvas.rotate(angle, x, y);
        hand.setBounds(0, 0,  getWidth(), getHeight());
        hand.draw(canvas);
        canvas.restore();
      }

}
