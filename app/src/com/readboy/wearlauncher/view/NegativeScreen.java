package com.readboy.wearlauncher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.Location.LocationControllerImpl;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.alarm.AlarmController;
import com.readboy.wearlauncher.bluetooth.BluetoothController;
import com.readboy.wearlauncher.net.NetworkController;
import com.readboy.wearlauncher.net.SignalClusterView;
import com.readboy.wearlauncher.utils.Utils;
import com.readboy.wearlauncher.utils.WatchController;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by 1 on 2017/5/5.
 */

public class NegativeScreen extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private LauncherApplication mApplication;
    private TextView mRingerModeView;
    private TextView mWeatherModeView;
    ScrollView mScrollView;

    private Uri weatherUri = Uri.parse("content://com.readboy.wearweather.provider/data");

    private ChangeIntentReceiver mReceiver = new ChangeIntentReceiver();

    public NegativeScreen(@NonNull Context context) {
        this(context,null);
    }

    public NegativeScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mApplication = (LauncherApplication) context.getApplicationContext();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        OverScrollDecoratorHelper.setUpOverScroll(mScrollView);
        findViewById(R.id.bell_bid).setOnClickListener(this);
        findViewById(R.id.weather_bid).setOnClickListener(this);
        findViewById(R.id.settings_bid).setOnClickListener(this);
        mRingerModeView = (TextView) findViewById(R.id.ring_mode);
        mWeatherModeView = (TextView) findViewById(R.id.weather_mode);
        //bluetooth
        initBluetoothController();
        //alarm
        initAlarmController();
        //net wifi
        initNetController();

//        initGPSController();

//        initClassDisable();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, filter);
//        mContext.getContentResolver().registerContentObserver(weatherUri,
//                true, sContentObserver);

        updateRingerMode();
//        updateWeatherMode();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mReceiver);
//        mContext.getContentResolver().unregisterContentObserver(sContentObserver);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        switch (vid){
            case R.id.bell_bid:
                setRingerMode();
                //Utils.startActivity(mContext, "com.android.settings","com.android.settings.Settings$RingerModeSettingsActivity");
                break;
            case R.id.weather_bid:
                Utils.startActivity(mContext, "com.readboy.wearweather","com.readboy.wearweather.MainActivity");
                break;
            case R.id.settings_bid:
                Utils.startActivity(mContext,"com.android.settings","com.android.settings.Settings");
                //Utils.startActivity(mContext,"com.qualcomm.qti.watchsettings","com.qualcomm.qti.watchsettings.SettingsActivity");
                break;
        }
    }

    class ChangeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return ;
            }
            //int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,AudioManager.RINGER_MODE_NORMAL);
            updateRingerMode();
        }
    }

    public void moveToTop(){
        if (mScrollView != null){
            mScrollView.scrollTo(0,0);
        }
    }

    private void updateRingerMode(){
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audio.getRingerMode();
        int vibrateAndRinging = Settings.System.getInt(mContext.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING,1);
        if(ringerMode == 2 && vibrateAndRinging != 1){
            ringerMode = 3;
        }
        int[] ringerModes = mContext.getResources().getIntArray(R.array.ringer_mode_integer);
        int index = 0;
        for(;index < ringerModes.length; index++){
            if(ringerMode == ringerModes[index]){
                break;
            }
        }
        String[] ringerText = mContext.getResources().getStringArray(R.array.ringer_mode_text);
        mRingerModeView.setText(ringerText[index]);
        mRingerModeView.setVisibility(View.VISIBLE);
    }

    private void updateWeatherMode(){
        String wea = getWeather();
        if(!Utils.isEmpty(wea)){
            mWeatherModeView.setText(wea);
            mWeatherModeView.setVisibility(View.VISIBLE);
        }else {
            mWeatherModeView.setVisibility(View.GONE);
        }
    }

    private void setRingerMode(){
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audio.getRingerMode();
        int vibrateAndRinging = Settings.System.getInt(mContext.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING,1);
        if(ringerMode == 2 && vibrateAndRinging != 1){
            ringerMode = 3;
        }
        int[] ringerModes = mContext.getResources().getIntArray(R.array.ringer_mode_integer);
        int index = 0;
        for(;index < ringerModes.length; index++){
            if(ringerMode == ringerModes[index]){
                break;
            }
        }
        index = (index+1) % ringerModes.length;
        int settingMode = ringerModes[index];
        if(settingMode == 3){
            Settings.System.putInt(getContext().getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING,0);
            audio.setRingerMode(2);
        }else{
            Settings.System.putInt(getContext().getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING,1);
            audio.setRingerMode(settingMode);
        }
        updateRingerMode();
    }

    /**
     * 蓝牙
     */
    private void  initBluetoothController(){
        ImageView bluetoothIconView = (ImageView) findViewById(R.id.btn_id_bluetooth);
        BluetoothController bluetoothEnabler = mApplication.getBluetoothController();
        bluetoothEnabler.addBluetoothIconView(bluetoothIconView);
        bluetoothEnabler.fireCallbacks();
    }

    /**
     * 闹钟
     */
    private void  initAlarmController(){
        ImageView alarmIconView = (ImageView) findViewById(R.id.btn_id_alarm);
        AlarmController alarmController = mApplication.getAlarmController();
        alarmController.addAlarmIconView(alarmIconView);
        alarmController.fireCallbacks();
    }
    /**
     * 网络 信号和Wi-Fi
     */
    private  void initNetController(){
        NetworkController controller = mApplication.getNetworkController();
        SignalClusterView signalCluster = (SignalClusterView) findViewById(R.id.signal_cluster);
        controller.addSignalCluster(signalCluster);
        controller.addNetworkSignalChangedCallback(signalCluster);
        signalCluster.setNetworkController(controller);
    }

    /**
     * GPS
     */
    private  void initGPSController(){
        ImageView gpsIconView = (ImageView) findViewById(R.id.btn_id_gps);
        LocationControllerImpl controller = mApplication.getLocationControllerImpl();
        controller.addIconView(gpsIconView);
    }

    private void initClassDisable(){
        ImageView iconView = (ImageView) findViewById(R.id.btn_id_classdisable);
        WatchController watchController = mApplication.getWatchController();
        watchController.addClassDisableIconView(iconView);
    }

    private String  getWeather(){
        String weather = "";
        try {
            Cursor c = mContext.getContentResolver().query(weatherUri,null,null,null,null);
            if(c != null && c.moveToLast()){
                //Log.d("getWeather", Arrays.toString(c.getColumnNames()));
                //c.getString(c.getColumnIndex("temperature"));
                //c.getString(c.getColumnIndex("weather"));
                //c.getString(c.getColumnIndex("weathercode"));
                weather = mContext.getResources().getString(R.string.weather_template,
                        c.getString(c.getColumnIndex("temperature")),
                        c.getString(c.getColumnIndex("weather")));
            }
            if(c != null){
                c.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return weather;
    }

    private final ContentObserver sContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            updateWeatherMode();
        }
    };
}
