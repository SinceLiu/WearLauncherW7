package com.readboy.wearlauncher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GuanJiaYin on 16/11/21.
 */

public class LauncherSharedPrefs {
    private static final String TAG = "Launcher.LauncherSharedPrefs";
    private static String SHARED_NAME = "settings";
    private static final String WATCHTYPE = "watchtype";

    public static int getWatchType(Context context){
        return getInt(context,WATCHTYPE);
    }

    public static void setWatchtype(Context context,int watchtype){
        putInt(context,WATCHTYPE,watchtype);
    }

    public static long getLong(Context context, String string) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, 0);
        long value = preferences.getLong(string, 0);
        return value;
    }

    public static void putLong(Context context, String string, long value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(string, value);
        editor.commit();
    }

    public static int getInt(Context context, String string) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        int value = preferences.getInt(string, 0);
        return value;
    }

    public static void putInt(Context context, String string, int value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(string, value);
        editor.commit();
    }

    public static String getString(Context context, String string) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        String value = preferences.getString(string, null);
        return value;
    }

    public static void putString(Context context, String string, String value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(string, value);
        editor.commit();
    }
}
