package com.readboy.wearlauncher.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

/**
 * Created by 1 on 2017/5/5.
 */

public class LauncherViewPager extends ViewPager {

    private Context mContext;

    public LauncherViewPager(Context context) {
        this(context,null);
    }

    public LauncherViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
}
