package com.readboy.wearlauncher.Location;

/**
 * Created by Administrator on 2017/6/21.
 */


public interface LocationController {
    boolean isLocationEnabled();
    boolean setLocationEnabled(boolean enabled);
    void addSettingsChangedCallback(LocationSettingsChangeCallback cb);
    void removeSettingsChangedCallback(LocationSettingsChangeCallback cb);

    /**
     * A callback for change in location settings (the user has enabled/disabled location).
     */
    public interface LocationSettingsChangeCallback {
        /**
         * Called whenever location settings change.
         *
         * @param locationEnabled A value of true indicates that at least one type of location
         *                        is enabled in settings.
         */
        void onLocationSettingsChanged(boolean locationEnabled);
    }
}
