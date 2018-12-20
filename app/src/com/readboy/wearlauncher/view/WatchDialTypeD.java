package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TODO: document your custom view class.
 */
public class WatchDialTypeD extends DialBaseLayout {

    private ChangeIntentReceiver mReceiver = new ChangeIntentReceiver();

    public WatchDialTypeD(Context context) {
        super(context);
    }

    public WatchDialTypeD(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatchDialTypeD(Context context, AttributeSet attrs, int defStyle) {
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
        addDateChangedCallback();
        //mDigitClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable() {
        if(mDialerBtn != null){
            mDialerBtn.setEnabled(true);
        }
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
        setDate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDigitClock = (DigitClock) findViewById(R.id.digit_clock);
        setDate();
        setTime();
    }

    class ChangeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return ;
            }
            setTime();
        }
    }


    private void setTime(){
        mDigitClock.setCurTime();
//        TextView hourView = (TextView) findViewById(R.id.hour_tvid);
//        TextView nimuteView = (TextView) findViewById(R.id.nimute_tvid);
//        GregorianCalendar time = new GregorianCalendar();
//        time.setTime(new Date());
//        int hour = time.get(Calendar.HOUR_OF_DAY);//Calendar.HOUR
//        int minute = time.get(Calendar.MINUTE);
////        int second = time.get(Calendar.SECOND);
////        int am_pm = time.get(Calendar.AM_PM);
//        String hourFormat = String.format("%02d",hour);
//        String nimuteFormat = String.format("%02d",minute);
//        hourView.setText(hourFormat);
//        nimuteView.setText(nimuteFormat);
    }

}
