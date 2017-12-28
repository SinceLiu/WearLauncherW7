package com.readboy.wearlauncher.battery;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.readboy.wearlauncher.R;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback{
	
	private static final String TAG = "Launcher.BatteryLevelTextView";

	//Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private BatteryController mBatteryController;
    private boolean mShow;

    private SettingsObserver mObserver = new SettingsObserver(new Handler());

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }
        protected void observe() {
            getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    STATUS_BAR_SHOW_BATTERY_PERCENT), false, this);
        }
        
        protected void unobserve() {
            getContext().getContentResolver().unregisterContentObserver(this);
        }
        
        public void update() {
            loadShowBatteryTextSetting();
            setVisibility(mShow ? View.VISIBLE : View.GONE);
        }
        
        @Override
        public void onChange(boolean selfChange) {
        	update();
        }
        
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	update();
        }
    };

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadShowBatteryTextSetting();
        setVisibility(mShow ? View.VISIBLE : View.GONE);
    }

    private void loadShowBatteryTextSetting() {
        mShow = 0 != Settings.System.getInt(getContext().getContentResolver(),
                STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        
        mShow = true;
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
//        if(!pluggedIn){
//            setText(getResources().getString(R.string.battery_level_template, level));
//			setTextColor(Color.WHITE);
//		}else{
//            setText(getResources().getString(R.string.battery_level_template, level));
//			setTextColor(Color.GREEN);
//		}
        setText(getResources().getString(R.string.battery_level_template, level));
        setTextColor(getResources().getColor(R.color.battery_level_color,null));
    }

    public void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;
        mBatteryController.addStateChangedCallback(this);
    }

    @Override
    public void onPowerSaveChanged() {
    	//Do Nothing
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mObserver.observe();

        BatteryController controller = new BatteryController(getContext());
        setBatteryController(controller);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mObserver.unobserve();

        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
            mBatteryController.unregisterReceiver();
        }
    }
   
}
