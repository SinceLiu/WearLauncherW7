package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.Calendar;

/**
 * TODO: document your custom view class.
 */
public class WatchDialTypeI extends DialBaseLayout{

    private AnalogClock mAnalogClock;

    public WatchDialTypeI(Context context) {
        super(context);
    }

    public WatchDialTypeI(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatchDialTypeI(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    public void addChangedCallback() {
        addDateChangedCallback();
        //mDigitClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable() {

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
}
