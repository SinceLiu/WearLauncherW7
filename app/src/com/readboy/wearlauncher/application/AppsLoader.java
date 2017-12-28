package com.readboy.wearlauncher.application;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.LauncherSettings;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.compat.LauncherAppsCompat;
import com.readboy.wearlauncher.compat.UserHandleCompat;
import com.readboy.wearlauncher.view.IconCache;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by GuanJiaYin on 17/3/9.
 */

public class AppsLoader extends AsyncTaskLoader<ArrayList<AppInfo>>
        implements LauncherAppsCompat.OnAppsChangedCallbackCompat {
    private final static String TAG = "AppsLoader";

    private final static String APP_READBOY_FLAG = "android.readboy.WATCH.FLAG";
    ArrayList<AppInfo> mInstalledApps;
    private  final PackageManager mPm;
    private Context mContext;
    private LauncherAppsCompat mLauncherApps;
    private IconCache mIconCache;

    HashMap<String,Integer> App_Icons = new HashMap<String,Integer>(){
        {
//            put("com.readboy.watch.speech", R.drawable.app_icon_audio);
//            put("com.readboy.hanzixuexiwatch",R.drawable.app_icon_book);
//            put("com.readboy.watchcamera",R.drawable.app_icon_camera);
//            put("",R.drawable.app_icon_class);
//            put("com.readboy.alarmclock",R.drawable.app_icon_clock);
//            put("com.readboy.findfriend",R.drawable.app_icon_friend);
//            put("com.readboy.heartratemonitor",R.drawable.app_icon_heart);
//            put("com.android.dialer",R.drawable.app_icon_phone);
//            put("com.readboy.qrcode",R.drawable.app_icon_qr);
//            put("com.readboy.running",R.drawable.app_icon_run);
//            put("com.ccb.readboy.timetable",R.drawable.app_icon_schedule);
//            put("com.android.settings",R.drawable.app_icon_setting);
//            put("",R.drawable.app_icon_sos);
//            put("com.readboy.pedometer",R.drawable.app_icon_step);
//            put("com.readboy.wearweather",R.drawable.app_icon_weather);
//            put("com.readboy.wetalk",R.drawable.app_icon_wechat);
//            put("com.readboy.wordstudy",R.drawable.app_icon_wrod);
        }
    };

    //按照这个方式排序
    ArrayList<String> Package_Sort = new ArrayList<String>(){
        {
            add("com.android.dialer");
            add("com.readboy.wetalk");
            add("com.readboy.pedometer");
            add("com.readboy.wordstudy");
            add("com.mediatek.camera");
            add("com.readboy.findfriend");
            add("com.readboy.wear.rbsos");

            add("com.android.settings");
            add("com.readboy.alarmclock");
            add("com.readboy.wearsettings");
            add("com.android.contacts");
            add("com.ccb.readboy.timetable");
            add("com.readboy.hanzixuexiwatch");
            add("com.readboy.heartratemonitor");
            add("com.readboy.qrcode");
            add("com.readboy.running");
            add("com.readboy.watch.speech");
            add("com.readboy.watchcamera");
            add("com.readboy.wearweather");
            add("com.android.deskclock");
        }
    };

    public AppsLoader(Context context) {
        super(context);
        mContext = context;
        mPm = context.getPackageManager();
        mIconCache = ((LauncherApplication)LauncherApplication.getApplication()).getIconCache();
    }

    private boolean isFilter(ResolveInfo resolveInfo){
        if(resolveInfo == null){
            return true;
        }
        return false;
    }

    private void removeExcludedPackages(List<ResolveInfo> pacList) {
        List<String> excludePackageList = Arrays.asList(mContext.getResources().getStringArray(
        R.array.excludePackageList));
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "lacheolauncher.test");
        if(file.exists() && file.isFile()){
            try {
                InputStream in  = new BufferedInputStream(new FileInputStream(file));
                BufferedReader br= new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String tmp;
                while((tmp=br.readLine())!=null){
                    Log.v(TAG, "filePackage " + tmp);
                    excludePackageList.add(tmp) ;
                }
                br.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int index = 0; index < excludePackageList.size(); ++index) {
            for (ResolveInfo packageIter : pacList) {
                if (TextUtils.equals(excludePackageList.get(index),packageIter.activityInfo.packageName)) {
                    Log.v(TAG, "Excluding Package " + packageIter.activityInfo.packageName);
                    pacList.remove(packageIter);
                    break;
                }
            }
        }
    }

    private void removeSelfPackage(List<ResolveInfo> pacList) {
        // iterate through the packages and remove this launcher package from the list
        for (int i = 0; i < pacList.size(); ++i) {
            if (pacList.get(i).activityInfo.packageName.equals(mContext.getPackageName())) {
                pacList.remove(i);
                break;
            }
        }
    }

    @Override
    public ArrayList<AppInfo> loadInBackground() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> mApps = mPm.queryIntentActivities(mainIntent, PackageManager.GET_RESOLVED_FILTER);
        removeSelfPackage(mApps);
        removeExcludedPackages(mApps);
        ArrayList<AppInfo> items = new ArrayList<AppInfo>(mApps.size());
        for(int i = 0; i< mApps.size() ; i++){
            ResolveInfo info = mApps.get(i);
            if(!isFilter(info)){

                Drawable drawable = mIconCache.getFullResIcon(info);
//                if(App_Icons.containsKey(info.activityInfo.packageName)){
//                    drawable = mContext.getResources().getDrawable(App_Icons.get(info.activityInfo.packageName));
//                }else {
//                    drawable = info.loadIcon(mPm);
//                }
                items.add(new AppInfo(info,drawable,info.loadLabel(mPm).toString(),info.activityInfo.packageName,info.activityInfo.name));
            }
        }
        ArrayList<AppInfo> sort_items = new ArrayList<AppInfo>();
        {
            for(int i = 0; i < Package_Sort.size(); i++){
                for (int j = 0; j < items.size(); j++){
                    if(items.get(j).mPackageName.equals(Package_Sort.get(i))){
                        sort_items.add(items.get(j));
                        items.remove(j);
                    }
                }
            }
            sort_items.addAll(items);
        }

        return sort_items;
    }

    @Override
    public void deliverResult(ArrayList<AppInfo> data) {

        mInstalledApps =  data;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {

        if (mInstalledApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mInstalledApps);
        }
        if (mLauncherApps == null){
            mLauncherApps = LauncherAppsCompat.getInstance(mContext);
            mLauncherApps.addOnAppsChangedCallback(this);
        }


        if (takeContentChanged() || mInstalledApps == null ) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        if (mLauncherApps != null){
            mLauncherApps.removeOnAppsChangedCallback(this);
            mLauncherApps = null;
        }

    }
    @Override
    public void onPackageRemoved(String packageName, UserHandleCompat user) {
        this.onContentChanged();
    }

    @Override
    public void onPackageAdded(String packageName, UserHandleCompat user) {
        this.onContentChanged();
    }

    @Override
    public void onPackageChanged(String packageName, UserHandleCompat user) {
        this.onContentChanged();
    }

    @Override
    public void onPackagesAvailable(String[] packageNames, UserHandleCompat user, boolean replacing) {
        this.onContentChanged();
    }

    @Override
    public void onPackagesUnavailable(String[] packageNames, UserHandleCompat user, boolean replacing) {
        this.onContentChanged();
    }
}
