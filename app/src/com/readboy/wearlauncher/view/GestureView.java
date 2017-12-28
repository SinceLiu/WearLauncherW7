package com.readboy.wearlauncher.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.readboy.wearlauncher.Launcher;
import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.utils.Utils;

/**
 * Created by Administrator on 2017/7/7.
 */

public class GestureView extends FrameLayout{
    private GestureDetector mGestureDetector;
    private Context mContext;
    private boolean mIsGestureDrag;

    public GestureView(@NonNull Context context) {
        this(context,null);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mGestureDetector = new GestureDetector(context,new DefaultGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            mIsGestureDrag = false;
        }
        if(mIsGestureDrag){
            return true;
        }
        return !mGestureDetector.onTouchEvent(ev) && super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mIsGestureDrag = false;
        }
        return super.onTouchEvent(event);
    }

    public void setIsGestureDrag(boolean drag){
        mIsGestureDrag = drag;
    }

    public MyGestureListener myGestureListener;

    public void setGestureListener(MyGestureListener l){
        myGestureListener = l;
    }

    public interface MyGestureListener {
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
        boolean onSingleTapConfirmed(MotionEvent e);
        void onLongPress(MotionEvent e);
    }

    class DefaultGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if(myGestureListener != null){
               return myGestureListener.onFling(e1,e2,velocityX,velocityY);
            }
            return super.onFling(e1,e2,velocityX,velocityY);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(myGestureListener != null){
                myGestureListener.onLongPress(e);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(myGestureListener != null){
                return myGestureListener.onSingleTapConfirmed(e);
            }
            return super.onSingleTapConfirmed(e);
        }
    }
}
