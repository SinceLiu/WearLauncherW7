/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.readboy.wearlauncher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.Utils;

import java.util.ArrayList;

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
public final class BluetoothController {
    private final Context mContext;
    private boolean mValidListener;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;
    
    private int[] mBluetoothIconIds = new int[]{R.drawable.stat_sys_data_bluetooth,
    		R.drawable.stat_sys_data_bluetooth_connected};
    private ArrayList<ImageView> mBluetoothIconViews = new ArrayList<ImageView>();
    
    ArrayList<BluetoothChangedCallback> mBluetoothChangedCallbacks =
            new ArrayList<BluetoothChangedCallback>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast receiver is always running on the UI thread here,
            // so we don't need consider thread synchronization.
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            handleStateChanged(state);
        }
    };
    
    public interface BluetoothChangedCallback {
        void onBluetoothChanged(boolean enable, int state);
    }
    
    public void addBluetoothChangedCallback(BluetoothChangedCallback cb) {
    	mBluetoothChangedCallbacks.add(cb);
    }

    public void removeBluetoothChangedCallback(BluetoothChangedCallback cb) {
    	mBluetoothChangedCallbacks.remove(cb);
    }

    public BluetoothController(Context context) {
        mContext = context;
        mValidListener = false;

        LocalBluetoothAdapter adapter = LocalBluetoothAdapter.getInstance();
        if (adapter == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
        } else {
            mLocalAdapter = adapter;
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void resume() {
        if (mLocalAdapter == null) {
            return;
        }

        // Bluetooth state is not sticky, so set it manually
        handleStateChanged(mLocalAdapter.getBluetoothState());

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mValidListener = true;
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }

        mContext.unregisterReceiver(mReceiver);
        mValidListener = false;
    }
    
    public void fireCallbacks() {
    	if (mLocalAdapter == null) {
            return;
        }
        handleStateChanged(mLocalAdapter.getBluetoothState());
    }

    public int getBluetoothState() {
        int bluetoothState = BluetoothAdapter.STATE_OFF;
        if (mLocalAdapter != null) bluetoothState = mLocalAdapter.getBluetoothState();
        boolean isOn = bluetoothState == BluetoothAdapter.STATE_ON;
        boolean isOff = bluetoothState == BluetoothAdapter.STATE_OFF;
        
        return bluetoothState;
    }

    public void addBluetoothIconView(ImageView v){
    	mBluetoothIconViews.add(v);
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode
        if (isChecked &&
                !Utils.isRadioAllowed(mContext, Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, "飞行模式", Toast.LENGTH_SHORT).show();
            // Reset switch to off
            buttonView.setChecked(false);
        }

        if (mLocalAdapter != null) {
            mLocalAdapter.setBluetoothEnabled(isChecked);
        }
    }

    void handleStateChanged(int state) {
    	int combinedSignalIconId = 0;
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                break;
            case BluetoothAdapter.STATE_ON:
                combinedSignalIconId = mBluetoothIconIds[0];
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_OFF:
                break;
            default:
        }
        
        for (BluetoothChangedCallback cb : mBluetoothChangedCallbacks) {
            cb.onBluetoothChanged((state == BluetoothAdapter.STATE_ON), state);
        }
        
        int N = mBluetoothIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mBluetoothIconViews.get(i);
            v.setImageResource(combinedSignalIconId);
            if (combinedSignalIconId == 0) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
}
