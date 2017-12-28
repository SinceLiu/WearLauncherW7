package com.readboy.wearlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author add by lzx
 * <p> It is a Corner LinearLayout
 */
public class CornerRelativeLayout extends RelativeLayout {

	public CornerRelativeLayout(Context context) {
		this(context,null);
	}

	public CornerRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CornerRelativeLayout(Context context, AttributeSet attrs,
                                int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private final Path mPath = new Path();
	private float mRectAdiusX = 12;
	private float mRectAdiusY = 12;
	
	private void init(Context context) {
		float density = getResources().getDisplayMetrics().density;
		mRectAdiusX = mRectAdiusX * density;
		mRectAdiusY = mRectAdiusY * density;
	}

	public void setRectAdius(float rx, float ry){
		mRectAdiusX = rx;
		mRectAdiusY = ry;
		requestLayout();
	}
	/*
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mPath.reset();
		mPath.addRoundRect(new RectF(getPaddingLeft(), getPaddingTop(), w
				- getPaddingRight(), h - getPaddingBottom()), mRectAdiusX,
				mRectAdiusY, Direction.CW);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if(mRectAdiusX != 0 || mRectAdiusY != 0){
			canvas.clipPath(mPath);
		}
		super.dispatchDraw(canvas);
	}
	
	@Override
	public void draw(Canvas canvas) {
		if(mRectAdiusX != 0 || mRectAdiusY != 0){
			canvas.clipPath(mPath);
		}
		super.draw(canvas);
	}*/

}