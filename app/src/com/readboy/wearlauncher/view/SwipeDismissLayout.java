/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readboy.wearlauncher.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * Special layout that finishes its activity when swiped away.
 */
public class SwipeDismissLayout extends FrameLayout {
    private static final String TAG = "NotifySwipeDismiss";

    private static final float MIN_ALPHA = 0.5F;
    private static final float DISMISS_MIN_DRAG_HEIGHT_RATIO = .33f;
    private boolean mUseDynamicTranslucency = true;
    private int mSwipeFlags = 0;
    private int mHeight = 0;

    /// M: define a slop to deterimne whether the motion events should be ignored or not.
    private static int sSwipeToDismissSlop;

    public interface OnDismissedListener {
        boolean onDismissed(SwipeDismissLayout layout);
    }

    public interface OnSwipeProgressChangedListener {
        /**
         * Called when the layout has been swiped and the position of the window should change.
         *
         * @param progress  A number in [0, 1] representing how far to the
         *                  right the window has been swiped
         * @param translate A number in [0, w], where w is the width of the
         *                  layout. This is equivalent to progress * layout.getWidth().
         */
        void onSwipeProgressChanged(SwipeDismissLayout layout, float progress, float translate);

        void onSwipeCancelled(SwipeDismissLayout layout);
    }

    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;

    // Transient properties
    private int mActiveTouchId;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private boolean mDismissed;
    private boolean mDiscardIntercept;
    private VelocityTracker mVelocityTracker;
    private float mTranslationY;

    private OnDismissedListener mDismissedListener;
    private OnSwipeProgressChangedListener mProgressListener;
    private ViewTreeObserver.OnEnterAnimationCompleteListener mOnEnterAnimationCompleteListener =
            new ViewTreeObserver.OnEnterAnimationCompleteListener() {
                @Override
                public void onEnterAnimationComplete() {
                    // SwipeDismissLayout assumes that the host Activity is translucent
                    // and temporarily disables translucency when it is fully visible.
                    // As soon as the user starts swiping, we will re-enable
                    // translucency.
                    if (mUseDynamicTranslucency && getContext() instanceof Activity) {
                        ((Activity) getContext()).convertFromTranslucent();
                    }
                }
            };
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mDismissed) {
                dismiss();
            } else {
                cancel();
            }
            resetMembers();
        }
    };
    private IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

    private float mLastY;

    public SwipeDismissLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                com.android.internal.R.styleable.Theme);
        mUseDynamicTranslucency = !a.hasValue(
                com.android.internal.R.styleable.Window_windowIsTranslucent);
        a.recycle();
        /// M: Apply MTK swipe to dismiss policy @{
        DisplayMetrics packageMetrics = context.getResources().getDisplayMetrics();
        sSwipeToDismissSlop = packageMetrics.heightPixels / 10;  //packageMetrics.widthPixels / 6;
        /// @}
    }

    public void setOnDismissedListener(OnDismissedListener listener) {
        mDismissedListener = listener;
    }

    public void setOnSwipeProgressChangedListener(OnSwipeProgressChangedListener listener) {
        mProgressListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getContext() instanceof Activity) {
            getViewTreeObserver().addOnEnterAnimationCompleteListener(
                    mOnEnterAnimationCompleteListener);
        }
        getContext().registerReceiver(mScreenOffReceiver, mScreenOffFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mScreenOffReceiver);
        if (getContext() instanceof Activity) {
            getViewTreeObserver().removeOnEnterAnimationCompleteListener(
                    mOnEnterAnimationCompleteListener);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // offset because the view is translated during swipe
        ev.offsetLocation(0, mTranslationY);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                resetMembers();
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();

//                if (mDownY < sSwipeToDismissSlop) {
//                    mDiscardIntercept = true;
//                    break;
//                }

                mActiveTouchId = ev.getPointerId(0);
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                int actionIndex = ev.getActionIndex();
                mActiveTouchId = ev.getPointerId(actionIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                actionIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(actionIndex);
                if (pointerId == mActiveTouchId) {
                    // This was our active pointer going up. Choose a new active pointer.
                    int newActionIndex = actionIndex == 0 ? 1 : 0;
                    mActiveTouchId = ev.getPointerId(newActionIndex);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                resetMembers();
                break;

            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null || mDiscardIntercept) {
                    break;
                }

                int pointerIndex = ev.findPointerIndex(mActiveTouchId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointer index: ignoring.");
                    mDiscardIntercept = true;
                    break;
                }
                float dy = ev.getRawY() - mDownY;
                float x = ev.getX(pointerIndex);
                float y = ev.getY(pointerIndex);
                boolean canScrollY = dy != 0 && canScrollY(this, false, dy, x, y);
                if (canScrollY) {
                    mDiscardIntercept = true;
                    break;
                }
                updateSwiping(ev);
                break;
        }
//        Log.e(TAG, "onInterceptTouchEvent mDiscardIntercept:" + mDiscardIntercept + ",mSwiping:" + mSwiping);
        return !mDiscardIntercept && mSwiping;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //Log.d(TAG, "onTouchEvent ev.getAction():"+ev.getAction());
        if (mVelocityTracker == null) {
            return super.onTouchEvent(ev);
        }
        // offset because the view is translated during swipe
        ev.offsetLocation(0, mTranslationY);

        if (mDiscardIntercept) {
            return false;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                updateDismiss(ev);
                boolean dm = false;
                if (mDismissed) {
                    dm = dismiss();
                } else if (mSwiping) {
                    cancel();
                }
                if (!dm) resetMembers();
                break;

            case MotionEvent.ACTION_CANCEL:
                cancel();
                resetMembers();
                break;

            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);
                mLastY = ev.getRawY();
                updateSwiping(ev);
                if (mSwiping) {
                    if (mUseDynamicTranslucency && getContext() instanceof Activity) {
                        ((Activity) getContext()).convertToTranslucent(null, null);
                    }
                    setProgress(ev.getRawY() - mDownY);
                    break;
                }
        }
        return true;
    }

    private void setProgress(float deltaY) {
        mTranslationY = deltaY;
        if (mProgressListener != null && deltaY >= 0) {
            mProgressListener.onSwipeProgressChanged(this, deltaY / getHeight(), deltaY);
        }
        float translation = mTranslationY <= 0 ? mTranslationY : 0;
        SwipeDismissLayout.this.setTranslationY(translation);
        float alpha = Math.abs(deltaY) * (MIN_ALPHA - 1) / mHeight + 1;
        this.setAlpha(alpha);
    }

    private boolean dismiss() {
        if (mDismissedListener != null) {
            return mDismissedListener.onDismissed(this);
        }
        return false;
    }

    protected void cancel() {
        if (mUseDynamicTranslucency && getContext() instanceof Activity) {
            ((Activity) getContext()).convertFromTranslucent();
        }
        if (mProgressListener != null) {
            mProgressListener.onSwipeCancelled(this);
        }
    }

    /**
     * Resets internal members when canceling.
     */
    private void resetMembers() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = null;
        mTranslationY = 0;
        mDownX = 0;
        mDownY = 0;
        mSwiping = false;
        mDismissed = false;
        mDiscardIntercept = false;
        SwipeDismissLayout.this.setTranslationY(mTranslationY);
        setAlpha(1);
    }

    private void updateSwiping(MotionEvent ev) {
        if (!mSwiping) {
            float deltaX = ev.getRawX() - mDownX;
            float deltaY = ev.getRawY() - mDownY;
            if ((deltaX * deltaX) + (deltaY * deltaY) > mSlop * mSlop) {
                mSwiping = deltaY < 0 /*&& Math.abs(deltaY) > mSlop * 2*/ && Math.abs(deltaX) < Math.abs(deltaY);
            } else {
                mSwiping = false;
            }
        }
    }

    private void updateDismiss(MotionEvent ev) {
        float deltaY = ev.getRawY() - mDownY;
        mVelocityTracker.addMovement(ev);
        mVelocityTracker.computeCurrentVelocity(1000);
        if (!mDismissed) {
            if (Math.abs(deltaY) > (getHeight() * DISMISS_MIN_DRAG_HEIGHT_RATIO) &&
                    mVelocityTracker.getYVelocity() < -mMinFlingVelocity &&
                    ev.getRawY() <= mLastY) {
                mDismissed = true;
            }
        }
        // Check if the user tried to undo this.
        if (mDismissed && mSwiping) {
            // Check if the user's finger is actually back
            if (Math.abs(deltaY) < (getHeight() * DISMISS_MIN_DRAG_HEIGHT_RATIO) ||
                    // or user is flinging back left
                    mVelocityTracker.getYVelocity() > mMinFlingVelocity) {
                mDismissed = false;
            }
        }
    }

    /**
     * Tests scrollability within child views of v in the direction of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     *               or just its children (false).
     * @param dx     Delta scrolled in pixels. Only the sign of this is used.
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected boolean canScroll(View v, boolean checkV, float dx, float x, float y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && v.canScrollHorizontally((int) -dx);
    }

    /**
     * @return true if
     */
    protected boolean canScrollY(View v, boolean checkV, float dy, float x, float y) {
        boolean canScroll = false;
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                Rect rect = new Rect();
                child.getBoundsOnScreen(rect);
                if (!rect.contains((int) x, (int) y)) {
                    continue;
                }
                if (child instanceof AbsListView) {
                    final AbsListView absListView = (AbsListView) child;
                    int c = absListView.getChildCount();
                    final int lastItemPosition = absListView.getCount() - 1;
                    int lastVisiblePosition = absListView.getLastVisiblePosition();
                    if (c == 0) {
                        canScroll = true;
                    } else if (lastVisiblePosition >= lastItemPosition) {
                        final int childIndex = lastItemPosition - absListView.getFirstVisiblePosition();
                        final View lastVisibleChild = absListView.getChildAt(childIndex);
                        if (lastVisibleChild != null) {
                            canScroll = lastVisibleChild.getBottom() > absListView.getBottom() - absListView.getTop();
                        }
                    } else {
                        canScroll = true;
                    }
                    return canScroll;
                } else if (child instanceof RecyclerView) {
                    final RecyclerView recyclerView = (RecyclerView) child;
                    int rvCount = recyclerView.getChildCount();
                    if (rvCount == 0) {
                        return false;
                    } else if (recyclerView.canScrollVertically(1)) {
                        //证明没有滑动到recyclerView最低端
                        return true;
                    } else {
                        return false;
                    }
                }
//                else if (canScrollY(child, true, dy, x + scrollX - child.getLeft(),
//                        y + scrollY - child.getTop())) {
//                    return true;
//                }
            }
        }

        return canScroll;
        //dy 负数表示检测上滑，正数表示下滑
        //return checkV && v.canScrollVertically((int) dy);
    }

    public void addFlags(int flag) {
        mSwipeFlags = mSwipeFlags | flag;
    }

    public void setFlags(int flags) {
        mSwipeFlags = flags;
    }
}
