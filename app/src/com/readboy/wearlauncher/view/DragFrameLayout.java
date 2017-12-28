package com.readboy.wearlauncher.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by oubin on 2017/7/17.
 */

public class DragFrameLayout extends FrameLayout {
    private static final String TAG = "DragFrameLayout";

    private Point mOriginPoint = new Point();
    private float maxTranslationOffset = 0;
    private float mWidth = 0;

    private ViewDragHelper mViewDragHelper;
    private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();

    public DragFrameLayout(Context context) {
        this(context, null);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, 1, mCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        maxTranslationOffset = (float) (w * 0.15);
        View view = getChildAt(0);
        if (view != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            mOriginPoint.x = view.getLeft() + layoutParams.leftMargin;
            mOriginPoint.y = view.getTop() + layoutParams.topMargin;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.e(TAG, "tryCaptureView: requestDisallow true");
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int offset = (int) (Math.signum(left)
                    * mInterpolator.getInterpolation(Math.abs(left) / mWidth) * maxTranslationOffset);
            return offset;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            mViewDragHelper.settleCapturedViewAt(mOriginPoint.x, mOriginPoint.y);
            invalidate();
            Log.e(TAG, "onViewReleased: requestDisallow false");
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        @Override
        public int getOrderedChildIndex(int index) {
            return super.getOrderedChildIndex(index);
        }
    };
}
