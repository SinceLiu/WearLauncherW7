package com.readboy.wearlauncher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.readboy.wearlauncher.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DigitClock extends LinearLayout{

	private Context mContext;
	private ImageView hourImage0;
	private ImageView hourImage1;
	private ImageView dotImage;
	private ImageView minImage0;
	private ImageView minImage1;

	private int mMilliSeconds;
	private int mMinutes;
	private int mHour;
	private int dialType = 0;
	private boolean hasDot = true;

	private int[] clockDrawable_normal = new int[]{
		R.drawable.num_clock_0,
		R.drawable.num_clock_1,
		R.drawable.num_clock_2,
		R.drawable.num_clock_3,
		R.drawable.num_clock_4,
		R.drawable.num_clock_5,
		R.drawable.num_clock_6,
		R.drawable.num_clock_7,
		R.drawable.num_clock_8,
		R.drawable.num_clock_9
	};

	private int[] clockDrawable_type_a = new int[]{
			R.drawable.num_clock_a_0,
			R.drawable.num_clock_a_1,
			R.drawable.num_clock_a_2,
			R.drawable.num_clock_a_3,
			R.drawable.num_clock_a_4,
			R.drawable.num_clock_a_5,
			R.drawable.num_clock_a_6,
			R.drawable.num_clock_a_7,
			R.drawable.num_clock_a_8,
			R.drawable.num_clock_a_9
	};

	private int[] clockDrawable_type_d = new int[]{
			R.drawable.num_clock_d_0,
			R.drawable.num_clock_d_1,
			R.drawable.num_clock_d_2,
			R.drawable.num_clock_d_3,
			R.drawable.num_clock_d_4,
			R.drawable.num_clock_d_5,
			R.drawable.num_clock_d_6,
			R.drawable.num_clock_d_7,
			R.drawable.num_clock_d_8,
			R.drawable.num_clock_d_9

	};

	private int[] clockDrawable_type_g = new int[]{
			R.drawable.num_clock_g_0,
			R.drawable.num_clock_g_1,
			R.drawable.num_clock_g_2,
			R.drawable.num_clock_g_3,
			R.drawable.num_clock_g_4,
			R.drawable.num_clock_g_5,
			R.drawable.num_clock_g_6,
			R.drawable.num_clock_g_7,
			R.drawable.num_clock_g_8,
			R.drawable.num_clock_g_9
	};

	private int[] clockDrawable_type_h = new int[]{
			R.drawable.num_clock_h_0,
			R.drawable.num_clock_h_1,
			R.drawable.num_clock_h_2,
			R.drawable.num_clock_h_3,
			R.drawable.num_clock_h_4,
			R.drawable.num_clock_h_5,
			R.drawable.num_clock_h_6,
			R.drawable.num_clock_h_7,
			R.drawable.num_clock_h_8,
			R.drawable.num_clock_h_9

	};

	//type j
	private int[] clockDrawable_type_j = new int[]{
			R.drawable.num_clock_j_0,
			R.drawable.num_clock_j_1,
			R.drawable.num_clock_j_2,
			R.drawable.num_clock_j_3,
			R.drawable.num_clock_j_4,
			R.drawable.num_clock_j_5,
			R.drawable.num_clock_j_6,
			R.drawable.num_clock_j_7,
			R.drawable.num_clock_j_8,
			R.drawable.num_clock_j_9
	};

	//type k
	private int[] clockDrawable_type_k = new int[]{
			R.drawable.num_clock_k_0,
			R.drawable.num_clock_k_1,
			R.drawable.num_clock_k_2,
			R.drawable.num_clock_k_3,
			R.drawable.num_clock_k_4,
			R.drawable.num_clock_k_5,
			R.drawable.num_clock_k_6,
			R.drawable.num_clock_k_7,
			R.drawable.num_clock_k_8,
			R.drawable.num_clock_k_9
	};
	//type m
	private int[] clockDrawable_type_m = new int[]{
			R.drawable.num_clock_m_0,
			R.drawable.num_clock_m_1,
			R.drawable.num_clock_m_2,
			R.drawable.num_clock_m_3,
			R.drawable.num_clock_m_4,
			R.drawable.num_clock_m_5,
			R.drawable.num_clock_m_6,
			R.drawable.num_clock_m_7,
			R.drawable.num_clock_m_8,
			R.drawable.num_clock_m_9
	};

	//type n
	private int[] clockDrawable_type_n = new int[]{
			R.drawable.num_clock_n_0,
			R.drawable.num_clock_n_1,
			R.drawable.num_clock_n_2,
			R.drawable.num_clock_n_3,
			R.drawable.num_clock_n_4,
			R.drawable.num_clock_n_5,
			R.drawable.num_clock_n_6,
			R.drawable.num_clock_n_7,
			R.drawable.num_clock_n_8,
			R.drawable.num_clock_n_9
	};

	//type n
	private int[] clockDrawable_type_o = new int[]{
			R.drawable.num_clock_o_0,
			R.drawable.num_clock_o_1,
			R.drawable.num_clock_o_2,
			R.drawable.num_clock_o_3,
			R.drawable.num_clock_o_4,
			R.drawable.num_clock_o_5,
			R.drawable.num_clock_o_6,
			R.drawable.num_clock_o_7,
			R.drawable.num_clock_o_8,
			R.drawable.num_clock_o_9
	};

	public DigitClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs,0);
	}
	private void init(Context context,AttributeSet attrs, int defStyle) {
		mContext = context;


		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DigitClock);

		dialType = a.getInteger(R.styleable.DigitClock_clockType,0);
		hasDot = a.getBoolean(R.styleable.DigitClock_dot,true);
		a.recycle();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks(mClockTick);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		try {
			hourImage0 = (ImageView) findViewById(R.id.hour0);
			hourImage1 = (ImageView) findViewById(R.id.hour1);
			dotImage = (ImageView) findViewById(R.id.dot);
			minImage0 = (ImageView) findViewById(R.id.min0);
			minImage1 = (ImageView) findViewById(R.id.min1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void updateClock(){
		int TYPE_A = mContext.getResources().getInteger(R.integer.dial_type_a);
		int TYPE_D = mContext.getResources().getInteger(R.integer.dial_type_d);
		int TYPE_G = mContext.getResources().getInteger(R.integer.dial_type_g);
		int TYPE_H = mContext.getResources().getInteger(R.integer.dial_type_h);
		int TYPE_J = mContext.getResources().getInteger(R.integer.dial_type_j);
		int TYPE_K = mContext.getResources().getInteger(R.integer.dial_type_k);
		int TYPE_M = mContext.getResources().getInteger(R.integer.dial_type_m);
		int TYPE_N = mContext.getResources().getInteger(R.integer.dial_type_n);
		int TYPE_O = mContext.getResources().getInteger(R.integer.dial_type_o);

    	int currentHour = mHour;
    	int currentMinute = mMinutes;
    	int currentMillisecond = mMilliSeconds;
           
        int hour0 = currentHour / 10;
        int hour1 = currentHour % 10;
		int min0 = currentMinute / 10;
		int min1 = currentMinute % 10;
		if (dialType == TYPE_A){
			hourImage0.setBackgroundResource(clockDrawable_type_a[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_a[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_a[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_a[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_a_dot);
		}else if(dialType == TYPE_D){
			hourImage0.setBackgroundResource(clockDrawable_type_d[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_d[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_d[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_d[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_d_dot);
		}else if (dialType == TYPE_G){
			hourImage0.setBackgroundResource(clockDrawable_type_g[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_g[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_g[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_g[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_g_dot);
		} else if(dialType == TYPE_H){
			hourImage0.setBackgroundResource(clockDrawable_type_h[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_h[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_h[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_h[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_h_dot);
		}else if(dialType == TYPE_J){
			hourImage0.setBackgroundResource(clockDrawable_type_j[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_j[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_j[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_j[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_j_dot);
		}else if(dialType == TYPE_K){
			hourImage0.setBackgroundResource(clockDrawable_type_k[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_k[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_k[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_k[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_k_dot);
		}else if(dialType == TYPE_M){
			hourImage0.setBackgroundResource(clockDrawable_type_m[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_m[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_m[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_m[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_m_dot);
		}else if(dialType == TYPE_N){
			hourImage0.setBackgroundResource(clockDrawable_type_n[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_n[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_n[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_n[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_n_dot);
		}else if(dialType == TYPE_O){
			hourImage0.setBackgroundResource(clockDrawable_type_o[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_type_o[hour1]);
			minImage0.setBackgroundResource(clockDrawable_type_o[min0]);
			minImage1.setBackgroundResource(clockDrawable_type_o[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_o_dot);
		}else{
			hourImage0.setBackgroundResource(clockDrawable_normal[hour0]);
			hourImage1.setBackgroundResource(clockDrawable_normal[hour1]);
			minImage0.setBackgroundResource(clockDrawable_normal[min0]);
			minImage1.setBackgroundResource(clockDrawable_normal[min1]);
			dotImage.setBackgroundResource(R.drawable.num_clock_dot);
		}

		if(hasDot){
			if (currentMillisecond < 500){
				dotImage.setVisibility(VISIBLE);
			}else{
				dotImage.setVisibility(INVISIBLE);
			}
		}
	}

	public void setCurTime(){
		Calendar calendar = Calendar.getInstance();
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMinutes = calendar.get(Calendar.MINUTE);
		mMilliSeconds = calendar.get(Calendar.MILLISECOND);
		mMilliSeconds = 0;
		updateClock();
	}

	public void setTimePause(){
		removeCallbacks(mClockTick);
	}

	public void setTimeRunning(){
		post(mClockTick);
	}

	private final Runnable mClockTick = new Runnable () {

		@Override
		public void run() {
			Calendar calendar = Calendar.getInstance();
			mHour = calendar.get(Calendar.HOUR_OF_DAY);
			mMinutes = calendar.get(Calendar.MINUTE);
			mMilliSeconds = calendar.get(Calendar.MILLISECOND);
			int currentMillisecond = calendar.get(Calendar.MILLISECOND);

			updateClock();

			DigitClock.this.postDelayed(mClockTick, 1000 - currentMillisecond);
		}
	};
}
