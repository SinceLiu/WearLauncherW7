package com.readboy.wearlauncher.battery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

public class BatteryLevelImageView extends ImageView implements BatteryController.BatteryStateChangeCallback{

    private BatteryController mBatteryController;
	private Context mContext;
	private int mLastLevel = -1;
	private int mLevel = -1;
	private int mAnimOffset;
	private boolean mCharging;
	private boolean mPluggedIn;
	private static final int ADD_LEVEL = 10;
	private static final int ANIM_DURATION = 500;
	private static final int FULL = 96;
	private Handler mHandler = new Handler();

	public BatteryLevelImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	private final Runnable mInvalidate = new Runnable() {
		@Override
		public void run() {
			final int level = updateChargingAnimLevel();
			setBackgroundResource(0);
			if(mPluggedIn){
				setImageBitmap(createBatteryChargingImage(level));
			}else {
				setImageBitmap(createBatteryImage(level));
			}
		}
	};

	@Override
	public void onBatteryLevelChanged(int level, boolean pluggedIn,
			boolean charging) {
		// TODO Auto-generated method stub
		mLevel = level;
		mPluggedIn = pluggedIn;
		mCharging =charging;
		mHandler.post(mInvalidate);
//		if(pluggedIn){
//			//anim
//			setBackgroundResource(R.anim.battery_plugged_anim);
//			AnimationDrawable anim = (AnimationDrawable) getBackground();
//			anim.start();
//			setImageResource(R.drawable.battery_plugged_1);
//		}else{
//			setBackgroundResource(0);
//			setImageBitmap(createBatteryImage(level));
//		}
	}

	private int updateChargingAnimLevel() {
		int curLevel = mLevel;
		if (!mCharging) {
			mAnimOffset = 0;
			mHandler.removeCallbacks(mInvalidate);
		} else {
			curLevel += mAnimOffset;
			if (curLevel >= FULL) {
				curLevel = 100;
				mAnimOffset = 0;
			} else {
				mAnimOffset += ADD_LEVEL;
			}

			mHandler.removeCallbacks(mInvalidate);
			mHandler.postDelayed(mInvalidate, ANIM_DURATION);
		}
		return curLevel;
	}

	private Bitmap createBatteryChargingImage(int level){
		Bitmap  empty = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_empty);
		int width = empty.getWidth();
		int high = empty.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(empty.getWidth(), empty.getHeight(),empty.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(empty, new Matrix(), null);
		int left = 0;
		int top = 0;
		int right = (width*22/30) * level / 100 + width*3/30;
		int bottom = high;
		Rect rect = new Rect(left,top,right,bottom);
		Bitmap  full = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_charging_full);
		canvas.drawBitmap(full, rect, rect, null);

		return bitmap;
	}

	private Bitmap createBatteryImage(int level){
		Bitmap  empty = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_empty);
		int width = empty.getWidth();
		int high = empty.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(empty.getWidth(), empty.getHeight(),empty.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(empty, new Matrix(), null);
		int left = 0;
		int top = 0;
		int right = (width*22/30) * level / 100 + width*3/30;
		int bottom = high;
		Rect rect = new Rect(left,top,right,bottom);
		if(level < 20){
			Bitmap  low = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_low);
			canvas.drawBitmap(low, rect, rect, null);
		}else {
			Bitmap  full = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.battery_nor_full);
			canvas.drawBitmap(full, rect, rect, null);
		}

		return bitmap;
	}

	@Override
	public void onPowerSaveChanged() {
		// TODO Auto-generated method stub
		
	}

    private void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;
        mBatteryController.addStateChangedCallback(this);
    }
	@Override
	public  void  onAttachedToWindow(){
		super.onAttachedToWindow();
		BatteryController controller = new BatteryController(getContext());
		setBatteryController(controller);
	}

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
			mBatteryController.unregisterReceiver();
        }

    }


}
