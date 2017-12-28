package com.readboy.wearlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import me.dreamheart.autoscalinglayout.ASRelativeLayout;

/**
 * Created by 1 on 2017/5/6.
 */

public class DaialParentLayout extends ASRelativeLayout {
    private Context mContext;

    public DaialParentLayout(Context context) {
        this(context,null);
    }

    public DaialParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }
}
