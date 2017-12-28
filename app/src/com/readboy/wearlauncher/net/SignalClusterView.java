/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.readboy.wearlauncher.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.FeatureOptions;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalClusterView extends LinearLayout
        implements NetworkController.SignalCluster, NetworkController.NetworkSignalChangedCallback {

    static final boolean DEBUG = false;
    static final String TAG = "SignalClusterView";

    NetworkController mNC;

    private boolean mWifiVisible = true;
    private int mWifiStrengthId = 0;
    private int mNetworkIcon;
    private boolean mMobileVisible = false;
    private int mMobileStrengthId = 0, mMobileTypeId = 0;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private String mWifiDescription, mMobileDescription, mMobileTypeDescription;
    private boolean mWifiIn, mWifiOut, mMobileIn, mMobileOut;
    private String mNetworkName;
    private boolean isNoSim;
    private int mVolteStatusIcon = 0;

    ViewGroup mWifiGroup, mMobileGroup;
    ImageView mWifi, mWifiInOut, mMobile, mMobileInOut, mMobileType, mAirplane,mNetworkType,mVolteView;
    TextView mNetworkNameView;
//    View mSpacer;

    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNetworkController(NetworkController nc) {
        if (DEBUG) Log.d(TAG, "NetworkController=" + nc);
        mNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiInOut       = (ImageView) findViewById(R.id.wifi_inout);
        mMobileGroup    = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile         = (ImageView) findViewById(R.id.mobile_signal);
        mMobileInOut   = (ImageView) findViewById(R.id.mobile_inout);
        mMobileType     = (ImageView) findViewById(R.id.mobile_type);
        mNetworkType   = (ImageView) findViewById(R.id.network_type);
        mNetworkNameView   = (TextView) findViewById(R.id.network_name);
        mVolteView      = (ImageView) findViewById(R.id.volte_icon);
//        mSpacer         =             findViewById(R.id.spacer);
        mAirplane       = (ImageView) findViewById(R.id.airplane);

        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup      = null;
        mWifi           = null;
        mWifiInOut     = null;
        mMobileGroup    = null;
        mMobile         = null;
        mMobileInOut   = null;
        mMobileType     = null;
        mNetworkNameView = null;
        mVolteView     = null;
//        mSpacer         = null;
        mAirplane       = null;

        super.onDetachedFromWindow();
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiDescription = contentDescription;

        apply();
    }
    @Override
    public void setMobileDataIndicators(boolean visible, int strengthIcon,
            int typeIcon, String contentDescription, String typeContentDescription,
            int noSimIcon) {
        mMobileVisible = visible;
        mMobileStrengthId = strengthIcon;
        mMobileTypeId = typeIcon;
        mMobileDescription = contentDescription;
        mMobileTypeDescription = typeContentDescription;
        isNoSim = noSimIcon != 0;
        mMobileStrengthId = noSimIcon != 0 ? noSimIcon : mMobileStrengthId;

        apply();
    }

    @Override
    public void setVolteStatusIcon(int iconId) {
        mVolteStatusIcon = iconId;
        apply();
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;

        apply();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup != null && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        if (mMobileVisible && mMobileGroup != null && mMobileGroup.getContentDescription() != null)
            event.getText().add(mMobileGroup.getContentDescription());
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
        }

        if (mMobile != null) {
            mMobile.setImageDrawable(null);
        }

        if (mMobileType != null) {
            mMobileType.setImageDrawable(null);
        }

        if (mMobileInOut != null) {
            mMobileInOut.setImageDrawable(null);
        }

        if(mAirplane != null) {
            mAirplane.setImageDrawable(null);
        }

        apply();
    }

    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        //WIIF是否显示
        if (mWifiVisible) {
            mWifi.setImageResource(mWifiStrengthId);
            if(mWifiIn && mWifiOut){
                mWifiInOut.setImageResource(WifiIcons.WIFI_SIGNAL_DATA_INOUT[3]);
            }else if(mWifiOut){
                mWifiInOut.setImageResource(WifiIcons.WIFI_SIGNAL_DATA_INOUT[2]);
            }else if(mWifiIn){
                mWifiInOut.setImageResource(WifiIcons.WIFI_SIGNAL_DATA_INOUT[1]);
            }else {
                mWifiInOut.setImageResource(WifiIcons.WIFI_SIGNAL_DATA_INOUT[0]);
            }
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("wifi: %s sig=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId));
        //显示信号，飞行模式关闭
        if (mMobileVisible && !mIsAirplaneMode) {
            mMobile.setImageResource(mMobileStrengthId);
            mMobileType.setImageResource(mMobileTypeId);
            if(mMobileTypeId == 0){
                mMobileInOut.setImageBitmap(null);
            }else if(mMobileIn && mMobileOut){
                mMobileInOut.setImageResource(WifiIcons.WIFI_SIGNAL_INOUT[3]);
            }else if(mMobileOut){
                mMobileInOut.setImageResource(WifiIcons.WIFI_SIGNAL_INOUT[2]);
            }else if(mMobileIn){
                mMobileInOut.setImageResource(WifiIcons.WIFI_SIGNAL_INOUT[1]);
            }else {
                mMobileInOut.setImageResource(WifiIcons.WIFI_SIGNAL_INOUT[0]);
            }
            if(isNoSim){
                mNetworkNameView.setVisibility(View.GONE);
            }else {
                mNetworkNameView.setVisibility(View.VISIBLE);
                mNetworkNameView.setText(mNetworkName);
            }
            mVolteView.setImageResource(mVolteStatusIcon);

            mMobileGroup.setContentDescription(mMobileTypeDescription + " " + mMobileDescription);
            mMobileGroup.setVisibility(View.VISIBLE);
        } else {
            mMobileGroup.setVisibility(View.GONE);
        }
        //飞行模式打开
        if (mIsAirplaneMode) {
            mAirplane.setImageResource(mAirplaneIconId);
            mAirplane.setVisibility(View.VISIBLE);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("mobile: %s sig=%d typ=%d",
                    (mMobileVisible ? "VISIBLE" : "GONE"),
                    mMobileStrengthId, mMobileTypeId));

        mMobileType.setVisibility(
                !mWifiVisible ? View.VISIBLE : View.GONE);
        mMobileInOut.setVisibility(
                !mWifiVisible ? View.VISIBLE : View.GONE);

        setNetworkIcon();
    }

    private void setNetworkIcon() {
        // Network type is CTA feature, so non CTA project should not set this.
        if (!FeatureOptions.MTK_CTA_SET) {
            return;
        }
        mNetworkIcon =0;//test
        if (mNetworkIcon == 0) {
            mNetworkType.setVisibility(View.GONE);
        } else {
            mNetworkType.setImageResource(mNetworkIcon);
            mNetworkType.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWifiSignalChanged(boolean enabled, int wifiSignalIconId, boolean activityIn, boolean activityOut, String wifiSignalContentDescriptionId, String description) {
        if(mWifiIn != activityIn || mWifiOut != activityOut){
            mWifiIn = activityIn;
            mWifiOut = activityOut;
            apply();
        }
    }

    @Override
    public void onMobileDataSignalChanged(boolean enabled, int mobileSignalIconId, String mobileSignalContentDescriptionId, int networkType, int dataTypeIconId, boolean activityIn, boolean activityOut, String dataTypeContentDescriptionId, String description) {
        if(mMobileIn != activityIn || mMobileOut != activityOut || mNetworkIcon != networkType || !TextUtils.equals(mNetworkName,description)){
            mMobileIn = activityIn;
            mMobileOut = activityOut;
            mNetworkIcon = networkType;
            mNetworkName = description;
            apply();
        }

    }

    @Override
    public void onAirplaneModeChanged(boolean enabled) {

    }
}

