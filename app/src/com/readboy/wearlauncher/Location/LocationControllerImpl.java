package com.readboy.wearlauncher.Location;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/6/21.
 */
public class LocationControllerImpl extends BroadcastReceiver implements LocationController {

    public static final String HIGH_POWER_REQUEST_CHANGE_ACTION = "android.location.HIGH_POWER_REQUEST_CHANGE";
    private static final String TAG = "LocationControllerImpl";

    private Context mContext;
    private AppOpsManager mAppOpsManager;
    private boolean mAreActiveLocationRequests;

    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<LocationSettingsChangeCallback> mSettingsChangeCallbacks =
            new ArrayList<LocationSettingsChangeCallback>();
    private final H mHandler = new H();

    private static final int[] mHighPowerRequestAppOpArray
            = new int[] {AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    public LocationControllerImpl(Context context) {
        mContext = context;

        // Register to listen for changes in location settings.
        IntentFilter filter = new IntentFilter();
        filter.addAction(HIGH_POWER_REQUEST_CHANGE_ACTION);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        context.registerReceiver(this,filter);
        mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        // Examine the current location state and initialize the status view.
        updateActiveLocationRequests();
        refreshViews();
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(this);
    }

    /**
     * Add a callback to listen for changes in location settings.
     */
    public void addSettingsChangedCallback(LocationSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.add(cb);
        mHandler.sendEmptyMessage(H.MSG_LOCATION_SETTINGS_CHANGED);
    }

    public void removeSettingsChangedCallback(LocationSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.remove(cb);
    }

    public void addIconView(ImageView v){
        mIconViews.add(v);
        mHandler.sendEmptyMessage(H.MSG_LOCATION_SETTINGS_CHANGED);
    }

    /**
     * Enable or disable location in settings.
     *
     * <p>This will attempt to enable/disable every type of location setting
     * (e.g. high and balanced power).
     *
     * <p>If enabling, a user consent dialog will pop up prompting the user to accept.
     * If the user doesn't accept, network location won't be enabled.
     *
     * @return true if attempt to change setting was successful.
     */
    public boolean setLocationEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        // When enabling location, a user consent dialog will pop up, and the
        // setting won't be fully enabled until the user accepts the agreement.
//        int mode = enabled
//                ? Settings.Secure.LOCATION_MODE_PREVIOUS : Settings.Secure.LOCATION_MODE_OFF;
        int mode = enabled ? -1 : Settings.Secure.LOCATION_MODE_OFF;
        // QuickSettings always runs as the owner, so specifically set the settings
        // for the current foreground user.
        return Settings.Secure.putInt(cr,Settings.Secure.LOCATION_MODE, mode);
//        return Settings.Secure
//                .putIntForUser(cr, Settings.Secure.LOCATION_MODE, mode, currentUserId);
    }

    /**
     * Returns true if location isn't disabled in settings.
     */
    public boolean isLocationEnabled() {
        ContentResolver resolver = mContext.getContentResolver();
        // QuickSettings always runs as the owner, so specifically retrieve the settings
        // for the current foreground user.
//        int mode = Settings.Secure.getIntForUser(resolver, Settings.Secure.LOCATION_MODE,
//                Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
        int mode = Settings.Secure.getInt(resolver,Settings.Secure.LOCATION_MODE,Settings.Secure.LOCATION_MODE_OFF);
        Log.d(TAG,"GPS mode:"+mode);
        return mode != Settings.Secure.LOCATION_MODE_OFF;
    }

    /**
     * Returns true if there currently exist active high power location requests.
     */
    private boolean areActiveHighPowerLocationRequests() {
        List<AppOpsManager.PackageOps> packages
                = mAppOpsManager.getPackagesForOps(mHighPowerRequestAppOpArray);
        // AppOpsManager can return null when there is no requested data.
        if (packages != null) {
            final int numPackages = packages.size();
            for (int packageInd = 0; packageInd < numPackages; packageInd++) {
                AppOpsManager.PackageOps packageOp = packages.get(packageInd);
                List<AppOpsManager.OpEntry> opEntries = packageOp.getOps();
                if (opEntries != null) {
                    final int numOps = opEntries.size();
                    for (int opInd = 0; opInd < numOps; opInd++) {
                        AppOpsManager.OpEntry opEntry = opEntries.get(opInd);
                        // AppOpsManager should only return OP_MONITOR_HIGH_POWER_LOCATION because
                        // of the mHighPowerRequestAppOpArray filter, but checking defensively.
                        if (opEntry.getOp() == AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION) {
                            if (opEntry.isRunning()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // Updates the status view based on the current state of location requests.
    private void refreshViews() {
        if (mAreActiveLocationRequests) {
//            mStatusBarManager.setIcon(mSlotLocation, LOCATION_STATUS_ICON_ID,
//                    0, mContext.getString(R.string.accessibility_location_active));
        } else {
            //mStatusBarManager.removeIcon(mSlotLocation);
        }
        int N = mIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mIconViews.get(i);

            v.setImageResource(R.drawable.stat_sys_location);
            if (!mAreActiveLocationRequests) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
        for (LocationSettingsChangeCallback cb : mSettingsChangeCallbacks) {
            cb.onLocationSettingsChanged(mAreActiveLocationRequests);
        }
    }

    // Reads the active location requests and updates the status view if necessary.
    private void updateActiveLocationRequests() {
        boolean hadActiveLocationRequests = mAreActiveLocationRequests;
        mAreActiveLocationRequests = areActiveHighPowerLocationRequests();
        if (mAreActiveLocationRequests != hadActiveLocationRequests) {
            refreshViews();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (HIGH_POWER_REQUEST_CHANGE_ACTION.equals(action)) {
            updateActiveLocationRequests();
        } else if (LocationManager.MODE_CHANGED_ACTION.equals(action)) {
            mHandler.sendEmptyMessage(H.MSG_LOCATION_SETTINGS_CHANGED);
        }
    }

    private final class H extends Handler {
        private static final int MSG_LOCATION_SETTINGS_CHANGED = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_SETTINGS_CHANGED:
//                    locationSettingsChanged();
                    break;
            }
        }

        private void locationSettingsChanged() {
            boolean isEnabled = isLocationEnabled();
            int N = mIconViews.size();
            for (int i=0; i<N; i++) {
                ImageView v = mIconViews.get(i);

                v.setImageResource(R.drawable.stat_sys_location);
                if (!isEnabled) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                }
            }
            for (LocationSettingsChangeCallback cb : mSettingsChangeCallbacks) {
                cb.onLocationSettingsChanged(isEnabled);
            }
        }
    }
}
