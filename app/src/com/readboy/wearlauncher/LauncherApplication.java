/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.readboy.wearlauncher;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.readboy.wearlauncher.Location.LocationControllerImpl;
import com.readboy.wearlauncher.alarm.AlarmController;
import com.readboy.wearlauncher.bluetooth.BluetoothController;
import com.readboy.wearlauncher.net.NetworkController;
import com.readboy.wearlauncher.utils.WatchController;
import com.readboy.wearlauncher.view.IconCache;

import java.lang.ref.WeakReference;
//import android.util.Slog;

public class LauncherApplication extends Application {
    private IconCache mIconCache;
    private static boolean bIsTouchEnable = true;
    private static long mSetTouchEnableTime = 0;

	private NetworkController mNetworkController;
	private BluetoothController mBluetoothController;
	private AlarmController mAlarmController;
    private LocationControllerImpl mLocationControllerImpl;

    private WatchController mWatchController;

    static LauncherApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        mIconCache = new IconCache(this);

        mNetworkController = new NetworkController(this);
        mBluetoothController = new BluetoothController(this);
        mBluetoothController.resume();
        mAlarmController = new AlarmController(this);
        mAlarmController.resume();
        mLocationControllerImpl = new LocationControllerImpl(this);

        mWatchController = new WatchController(this);
    }

    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        mNetworkController.unregisterReceiver();
        mLocationControllerImpl.unregisterReceiver();
        mBluetoothController.pause();
        mAlarmController.pause();
    }

    public static Application getApplication(){
        return mApplication;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public static void setTouchEnable(boolean enable){
        bIsTouchEnable = enable;
        mSetTouchEnableTime = System.currentTimeMillis();
    }
    public static boolean isTouchEnable() {
        return bIsTouchEnable || (Math.abs(System.currentTimeMillis() - mSetTouchEnableTime) > 1000);
    }
    
    public NetworkController getNetworkController(){
    	return mNetworkController;
    }

    public WatchController getWatchController(){
        return mWatchController;
    }
    
    public BluetoothController getBluetoothController(){
    	return mBluetoothController;
    }
    
    public AlarmController getAlarmController(){
    	return mAlarmController;
    }

    public LocationControllerImpl getLocationControllerImpl(){
        return mLocationControllerImpl;
    }
}
