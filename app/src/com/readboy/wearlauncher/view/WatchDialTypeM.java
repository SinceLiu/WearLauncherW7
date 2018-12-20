package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import java.util.Calendar;

/**
 * TODO: document your custom view class.
 */
public class WatchDialTypeM extends DialBaseLayout{
    private int[] WEEK_DRAWABLE = new int[]{
            R.drawable.week_m_sun,
            R.drawable.week_m_mon,
            R.drawable.week_m_tue,
            R.drawable.week_m_wed,
            R.drawable.week_m_thu,
            R.drawable.week_m_fri,
            R.drawable.week_m_sat
    };
    private int[] MONTH_DRAWABLE = new int[]{
            R.drawable.month_m_jan,
            R.drawable.month_m_feb,
            R.drawable.month_m_mar,
            R.drawable.month_m_apr,
            R.drawable.month_m_may,
            R.drawable.month_m_jun,
            R.drawable.month_m_jul,
            R.drawable.month_m_aug,
            R.drawable.month_m_sep,
            R.drawable.month_m_oct,
            R.drawable.month_m_nov,
            R.drawable.month_m_dec
    };

    public WatchDialTypeM(Context context) {
        super(context);
    }

    public WatchDialTypeM(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchDialTypeM(Context context, AttributeSet attrs, int defStyle) {
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
    public void addChangedCallback(){
        addCallUnreadChangedCallback();
        addDateChangedCallback();
        addWeTalkUnreadChangedCallback();
        //mDigitClock.setTimeRunning();
    }

    @Override
    public void setButtonEnable(){
        if(mDialerBtn != null){
            mDialerBtn.setEnabled(true);
        }
        if(mWetalkBtn != null){
            mWetalkBtn.setEnabled(true);
        }
    }

    @Override
    public void onCallUnreadChanged(int unreadNum) {
        if(mDialerNum != null){
            if (unreadNum > 0){
                mDialerNum.setVisibility(VISIBLE);
                String num = Integer.toString(unreadNum);
                if(unreadNum > getResources().getInteger(R.integer.unread_num_max)){
                    num = getResources().getString(R.string.unread_num_max_display);
                    mDialerNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_m_text_size_min));
                }else {
                    mDialerNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_m_text_size));
                }
                mDialerNum.setText(num);
            }else{
                mDialerNum.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onWeTalkUnreadChanged(int unreadNum) {
        if(mWeTalkNum != null){
            if (unreadNum > 0){
                mWeTalkNum.setVisibility(VISIBLE);
                String num = Integer.toString(unreadNum);
                if(unreadNum > getResources().getInteger(R.integer.unread_num_max)){
                    num = getResources().getString(R.string.unread_num_max_display);
                    mWeTalkNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_m_text_size_min));
                }else {
                    mWeTalkNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_m_text_size));
                }
                mWeTalkNum.setText(num);
            }else{
                mWeTalkNum.setVisibility(GONE);
            }
        }
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
//        mAnalogClock = (AnalogClock) findViewById(R.id.analog_clock);
//        mAnalogClock.setCurTime();
        setDate();
    }

    @Override
    protected void setDate(){
        ImageView monthView = (ImageView) findViewById(R.id.month_ivid);
        ImageView weekView = (ImageView) findViewById(R.id.week_ivid);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) % MONTH_DRAWABLE.length;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_DRAWABLE.length;
        monthView.setImageResource(MONTH_DRAWABLE[month]);
        weekView.setImageResource(WEEK_DRAWABLE[week]);
    }
}

