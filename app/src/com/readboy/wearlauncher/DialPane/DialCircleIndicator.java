package com.readboy.wearlauncher.DialPane;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.readboy.wearlauncher.R;


public class DialCircleIndicator extends RelativeLayout{

    private float DEFAULT_INDICATOR_WIDTH = 15; //默认的 小圆形的 宽 ,而不是半径
    private float DEFAULT_WIDTH = 240;
    private float DEFAULT_SPACE = 15 + DEFAULT_INDICATOR_WIDTH;
    private float DEFAULT_PADDING = 30;
    private double DEFAULT_ANGLE;//弧度

    private float mIndicatorMargin = -1;
    private float mIndicatorWidth = -1;
    private float mIndicatorHeight = -1;
    private int mAnimatorResId = R.anim.scale_with_alpha;
    private int mAnimatorpOutResId = R.anim.scale_with_alpha_out;
    private int mAnimatorReverseResId = 0;
    private int mIndicatorBackgroundResId = R.drawable.white_radius;
    private int mIndicatorUnselectedBackgroundResId = R.drawable.unselect_radius;

    private int mLastPosition = 0;
    private int mSumCount = 0;
    private Context mContext;

    public DialCircleIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public DialCircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
    	mContext = context;
//        DEFAULT_INDICATOR_WIDTH = Utils.dip2px(context,DEFAULT_INDICATOR_WIDTH);
//        DEFAULT_WIDTH = Utils.dip2px(context,DEFAULT_WIDTH);
//        DEFAULT_SPACE = Utils.dip2px(context,DEFAULT_SPACE);
//        DEFAULT_PADDING = Utils.dip2px(context,DEFAULT_PADDING);
        DEFAULT_ANGLE = 2 * Math.asin(DEFAULT_SPACE/(DEFAULT_WIDTH - DEFAULT_PADDING));//得出的是弧度
        handleTypedArray(context, attrs);
        checkIndicatorConfig(context);
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicator);
        mIndicatorWidth =
                typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_width, -1);
        mIndicatorHeight =
                typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_height, -1);
        mIndicatorMargin =
                typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_margin, -1);

        mAnimatorResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator,
                R.anim.scale_with_alpha);
        mAnimatorReverseResId =
                typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator_reverse, 0);
        mIndicatorBackgroundResId =
                typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable,
                        R.drawable.white_radius);
        mIndicatorUnselectedBackgroundResId =
                typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable_unselected,
                        mIndicatorUnselectedBackgroundResId);
        typedArray.recycle();
    }

    /**
     * Create and configure Indicator in Java code.
     */
    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin) {
        configureIndicator(indicatorWidth, indicatorHeight, indicatorMargin,
                R.anim.scale_with_alpha, 0, R.drawable.white_radius, R.drawable.white_radius);
    }

    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin,
            int animatorId, int animatorReverseId,
             int indicatorBackgroundId,
            int indicatorUnselectedBackgroundId) {

        mIndicatorWidth = indicatorWidth;
        mIndicatorHeight = indicatorHeight;
        mIndicatorMargin = indicatorMargin;

        mAnimatorResId = animatorId;
        mAnimatorReverseResId = animatorReverseId;
        mIndicatorBackgroundResId = indicatorBackgroundId;
        mIndicatorUnselectedBackgroundResId = indicatorUnselectedBackgroundId;

        checkIndicatorConfig(getContext());
    }

    private void checkIndicatorConfig(Context context) {
        mIndicatorWidth = (mIndicatorWidth < 0) ? DEFAULT_INDICATOR_WIDTH : mIndicatorWidth;
        mIndicatorHeight =
                (mIndicatorHeight < 0) ? DEFAULT_INDICATOR_WIDTH : mIndicatorHeight;
        mIndicatorMargin =
                (mIndicatorMargin < 0) ? DEFAULT_INDICATOR_WIDTH : mIndicatorMargin;

        mAnimatorResId = (mAnimatorResId == 0) ? R.anim.scale_with_alpha : mAnimatorResId;


        mIndicatorBackgroundResId = (mIndicatorBackgroundResId == 0) ? R.drawable.white_radius
                : mIndicatorBackgroundResId;
        mIndicatorUnselectedBackgroundResId =
                (mIndicatorUnselectedBackgroundResId == 0) ? mIndicatorBackgroundResId
                        : mIndicatorUnselectedBackgroundResId;
    }

    private Animator createAnimatorOut(Context context) {
        return AnimatorInflater.loadAnimator(context, mAnimatorpOutResId);
    }

    private Animator createAnimatorIn(Context context) {
        Animator animatorIn;
        if (mAnimatorReverseResId == 0) {
            //noinspection ResourceType
            animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorResId);
            animatorIn.setInterpolator(new ReverseInterpolator());
        } else {
            animatorIn = AnimatorInflater.loadAnimator(context, mAnimatorReverseResId);
        }
        return animatorIn;
    }

    public void setIndicatorsCounter(int count){
        mSumCount = count;
        createIndicators();
    }

	public void setCurrentPosition(int position){
        Log.d("","lzx mSumCount:"+mSumCount+", position:"+position);
        if (mSumCount <= 0 || mSumCount < position) {
            return;
        }
        if (mLastPosition >= 0) {
            View currentIndicator = getChildAt(mLastPosition);
            currentIndicator.setBackgroundResource(mIndicatorUnselectedBackgroundResId);
        }
        View selectedIndicator = getChildAt(position);
        selectedIndicator.setBackgroundResource(mIndicatorBackgroundResId);

        mLastPosition = position;
    }

    private void createIndicators() {
        removeAllViews();
        int count = mSumCount;
        if (count <= 0) {
            return;
        }
        int currentItem = 0;

        for (int i = 0; i < count; i++) {
            if (currentItem == i) {
                addIndicator(mIndicatorBackgroundResId,i);
            } else {
                addIndicator(mIndicatorUnselectedBackgroundResId,i);
            }
        }
    }

    public void updateIndicators(int count){
        removeAllViews();
        mSumCount = count;
        if (count <= 0) {
            return;
        }
        if (mLastPosition >= count){
            mLastPosition = count -1;
        }
        for (int i = 0; i < count; i++) {
            if (mLastPosition == i) {
                addIndicator(mIndicatorBackgroundResId,i);
            } else {
                addIndicator(mIndicatorUnselectedBackgroundResId,i);
            }
        }
    }
    private void addIndicator( int backgroundDrawableId ,int index) {
      
        int middle = mSumCount / 2 ;
        float roundWidth = (DEFAULT_WIDTH - DEFAULT_PADDING) / 2;
        float R = (DEFAULT_WIDTH - DEFAULT_INDICATOR_WIDTH) / 2;
        View Indicator = new View(getContext());
        Indicator.setBackgroundResource(backgroundDrawableId);
        addView(Indicator, (int)mIndicatorWidth, (int)mIndicatorHeight);
        LayoutParams lp = (LayoutParams) Indicator.getLayoutParams();

        //每个点 DEFAULT_ANGLE 角度
        double angle =  DEFAULT_ANGLE * (middle - index) - Math.PI / 2;
        //计算 top
        float y = (float)(roundWidth * Math.sin(angle));
        //计算 left
        float x = (float)(roundWidth * Math.cos(angle));

        float leftMargin =  R - x;
        float topMargin  =  R + y;

        lp.leftMargin = (int)leftMargin;
        lp.topMargin = (int)topMargin;
        Indicator.setLayoutParams(lp);
    }

    private class ReverseInterpolator implements Interpolator {
        @Override public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }


}
