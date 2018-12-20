package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.Calendar;


public class WatchDialTypeN extends DialBaseLayout {
    private DigitClock mDigitClock;

    public WatchDialTypeN(Context context) {
        super(context);
    }

    public WatchDialTypeN(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WatchDialTypeN(Context context, AttributeSet attrs, int defStyle) {
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
        if(mDialerNum != null){
            if (unreadNum > 0){
                mDialerNum.setVisibility(VISIBLE);
                String num = Integer.toString(unreadNum);
                if(unreadNum > getResources().getInteger(R.integer.unread_num_max)){
                    num = getResources().getString(R.string.unread_num_max_display);
                    mDialerNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_text_size_min));
                }else {
                    mDialerNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_text_size));
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
                            getResources().getDimensionPixelSize(R.dimen.corner_font_text_size_min));
                }else {
                    mWeTalkNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.corner_font_text_size));
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
        setDate();
    }

}
