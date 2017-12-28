package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.Calendar;


public class WatchDialTypeJ extends DialBaseLayout {
    private DigitClock mDigitClock;

    public WatchDialTypeJ(Context context) {
        super(context);
    }

    public WatchDialTypeJ(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatchDialTypeJ(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onPause() {
        mDigitClock.setTimePause();
    }

    @Override
    public void onResume() {
        mDigitClock.setTimeRunning();
    }

    @Override
    public void addChangedCallback() {
        addCallUnreadChangedCallback();
        addDateChangedCallback();
        addWeTalkUnreadChangedCallback();
        //mDigitClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable() {
        if(mDialerBtn != null){
            mDialerBtn.setEnabled(true);
        }
        if(mWetalkBtn != null){
            mWetalkBtn.setEnabled(true);
        }
    }

    @Override
    public void onCallUnreadChanged(int unreadNum) {

    }

    @Override
    public void onWeTalkUnreadChanged(int unreadNum) {

    }

    @Override
    public void onDateChange(int year, int month, int day, int week) {
        setDate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDigitClock = (DigitClock) findViewById(R.id.digit_clock);
        mDigitClock.setCurTime();
        setDate();
    }

    private void setDate(){
        TextView mDateText = (TextView) findViewById(R.id.date_tvid);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WatchController.WEEK_NAME_CN_LONG.length;
        String dateFormat = String.format("%d/%d %s",month,day,WatchController.WEEK_NAME_CN_LONG[week]);
        mDateText.setText(dateFormat);
    }
}
