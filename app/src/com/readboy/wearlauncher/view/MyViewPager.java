package com.readboy.wearlauncher.view;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.support.v4.view.ViewPager;

public class MyViewPager extends ViewPager {
    private float mLastX;
    private float mLastY;
    private float dirX;
    private float dirY;
    private boolean isSpi;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isSpi = Settings.System.getInt(context.getContentResolver(), "is_spi", 0)==1;
    }

    public MyViewPager(Context context) {
        this(context, null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                mLastY = ev.getY();
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果是SPI屏，取消切屏动画
        if(!isSpi){
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                dirX = ev.getX() - mLastX;
                if (getCurrentItem() == 1) {
                    dirY = ev.getY() - mLastY;
                    if (Math.abs(dirY) > Math.abs(dirX)) {
                        break;
                    }
                }
                if (dirX < -10) {
                    setCurrentItem(Math.min(getCurrentItem() + 1, getAdapter().getCount() - 1), false);
                } else if (dirX > 10) {
                    setCurrentItem(Math.max(0, getCurrentItem() - 1), false);
                }
                break;
            default:
                break;
        }
        return true;
    }
}