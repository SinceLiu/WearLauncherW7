package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;

import com.readboy.wearlauncher.R;

/**
 * TODO: document your custom view class.
 */
public class WatchDialTypeL extends DialBaseLayout {

    public WatchDialTypeL(Context context) {
        super(context);
    }

    public WatchDialTypeL(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatchDialTypeL(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onPause() {
        mAnalogClock.setTimePause();
    }

    @Override
    public void onResume() {
        mAnalogClock.setTimeRunning();
    }

    @Override
    public void addChangedCallback() {
        //mAnalogClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable() {

    }

    @Override
    public void onStepChange(int step) {
    }

    @Override
    public void onCallUnreadChanged(int unreadNum) {

    }

    @Override
    public void onWeTalkUnreadChanged(int unreadNum) {

    }

    @Override
    public void onDateChange(int year, int month, int day, int week) {
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
        mAnalogClock = (AnalogClock) findViewById(R.id.analog_clock);
        mAnalogClock.setCurTime();
    }

}
