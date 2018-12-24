package com.readboy.wearlauncher.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 时间、日期、天气（警报）、电话/未接提示泡、微聊/未读微聊信息、计步
 * Created by Administrator on 2017/6/13.
 */

public class WatchController extends BroadcastReceiver {
    public static final String TAG = "WatchController";
    //class disable
    public static final String TAG_CLASS_DISABLED = "class_disabled";
    public static final String TAG_CLASS_DISABLED_TIME = "class_disable_time";
    public static final String READBOY_ACTION_CLASS_DISABLE_CHANGED = "readboy.acion.CLASS_DISABLE_CHANGED";
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
    //Weather
    public static final String ACTION_WEATHER_RESULT = "com.readboy.wearlauncher.weather.WEATHER_RESULT";
    public static final String ACTION_WEATHER_GET = "com.readboy.wearlauncher.weather.GET_WEATHER";
    //Step
    public static final String ACTION_STEP_ADD = "com.readboy.action.StepCountService.stepAdd";

    //call
    private final static Uri MISSCALL_CONTENT_URI = CallLog.Calls.CONTENT_URI;
    //wetalk
    public static final Uri WETALK_CONTENT_URI = ContactsContract.Data.CONTENT_URI;
    //step
    public static final Uri STEPS_CONTENT_URI = Uri.parse("content://com.readboy.pedometer.contentProvider/pedometer");

    private static final Object LOCK = new Object();
    private final static String MISSCALL_WHERE = "type = 3 and new = 1";
    private static final int CALL_MSG_WHAT = 0x10;
    private static final int WETALK_MSG_WHAT = 0x11;

    public static final String[] WEEK_NAME_CN_LONG = new String[]{"星期天","星期一","星期二","星期三","星期四","星期五","星期六"};
    public static final String[] WEEK_NAME_CN_SHORT = new String[]{"周日","周一","周二","周三","周四","周五","周六"};
    public static final String[] WEEK_NAME_EN_SHORT = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    public static final String MONTHS_NAME_EN_SHORT[] = {
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"};

    public static final String[] WEEK_NAME_EN_LONG = new String[]{
            "Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    public static final String MONTHS_NAME_EN_LONG[] = {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"};

    private ArrayList<ImageView> mClassDisableIconViews = new ArrayList<ImageView>();

    Context mContext;
    private String mClassDisableData;
    int mStepCount;
    int mMissCallCount;
    int mMissWetalkCount;

    public interface DateChangedCallback {
        void onDateChange(int year, int month, int day, int week);
    }
    private ArrayList<DateChangedCallback> mDateChangedCallback = new ArrayList<>();
    public void addDateChangedCallback(DateChangedCallback cb){
        if(mDateChangedCallback.contains(cb)){
            mDateChangedCallback.remove(cb);
        }
        mDateChangedCallback.add(cb);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_NAME_CN_SHORT.length;
        cb.onDateChange(year,month,day,week);
    }
    public void removeDateChangedCallback(DateChangedCallback cb){
        mDateChangedCallback.remove(cb);
    }

    public interface StepChangedCallback {
        void onStepChange(int step);
    }
    private ArrayList<StepChangedCallback> mStepChangedCallback = new ArrayList<>();
    public void addStepChangedCallback(StepChangedCallback cb){
        mStepChangedCallback.add(cb);
        cb.onStepChange(mStepCount);
    }
    public void removeStepChangedCallback(StepChangedCallback cb){
        mStepChangedCallback.remove(cb);
    }
    private void fireStepChanged(){
        for (StepChangedCallback cb : mStepChangedCallback){
            cb.onStepChange(mStepCount);
        }
    }

    public interface CallUnreadChangedCallback {
        void onCallUnreadChanged(int count);
    }
    private ArrayList<CallUnreadChangedCallback> mCallUnreadChangedCallback = new ArrayList<>();
    public void addCallUnreadChangedCallback(CallUnreadChangedCallback cb){
        if (!mCallUnreadChangedCallback.contains(cb)){
            mCallUnreadChangedCallback.add(cb);
        }
        cb.onCallUnreadChanged(mMissCallCount);
    }
    public void removeCallUnreadChangedCallback(CallUnreadChangedCallback cb){
        mCallUnreadChangedCallback.remove(cb);
    }

    public interface WeTalkUnreadChangedCallback {
        void onWeTalkUnreadChanged(int count);
    }
    private ArrayList<WeTalkUnreadChangedCallback> mWeTalkUnreadChangedCallback = new ArrayList<>();
    public void addWeTalkUnreadChangedCallback(WeTalkUnreadChangedCallback cb){
        mWeTalkUnreadChangedCallback.add(cb);
        cb.onWeTalkUnreadChanged(mMissWetalkCount);
    }
    public void removeWeTalkUnreadChangedCallback(WeTalkUnreadChangedCallback cb){
        mWeTalkUnreadChangedCallback.remove(cb);
    }

    public void addClassDisableIconView(ImageView v){
        mClassDisableIconViews.add(v);
        classDisableChanged();
    }
    public interface ClassDisableChangedCallback {
        void onClassDisableChange(boolean show);
    }
    private ArrayList<ClassDisableChangedCallback> mClassDisableChangedCallback = new ArrayList<>();
    public void addClassDisableChangedCallback(ClassDisableChangedCallback cb){
        mClassDisableChangedCallback.add(cb);
        boolean show = !TextUtils.isEmpty(mClassDisableData) && isNowEnable();
        cb.onClassDisableChange(show);
    }
    public void removeClassDisableChangedCallback(ClassDisableChangedCallback cb){
        mClassDisableChangedCallback.remove(cb);
    }

    public ScreenOff mScreenOffListener;
    public void setScreenOffListener(ScreenOff l){
        mScreenOffListener = l;
    }
    public interface ScreenOff{
        void onScreenOff();
    }

    void classDisableChanged() {
        boolean show = !TextUtils.isEmpty(mClassDisableData) && isNowEnable();
        for(ClassDisableChangedCallback callback : mClassDisableChangedCallback) {
            callback.onClassDisableChange(show);
        }
        int N = mClassDisableIconViews.size();
        for (int i=0; i<N; i++) {
            ImageView v = mClassDisableIconViews.get(i);
            v.setImageResource(R.drawable.stat_sys_classdisable);
            if (!show) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }

    public WatchController(){

    }

    public WatchController(Context context){
        mContext = context;
        // broadcasts
        IntentFilter filter = new IntentFilter();
        //class disable
        filter.addAction(READBOY_ACTION_CLASS_DISABLE_CHANGED);
        //date
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        //time
        filter.addAction(Intent.ACTION_TIME_TICK);
        //weather
        filter.addAction(ACTION_WEATHER_RESULT);
        //step
        filter.addAction(ACTION_STEP_ADD);

        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(this, filter);

        mContext.getContentResolver().registerContentObserver(MISSCALL_CONTENT_URI,
                true, sMissCallObserver);
        mContext.getContentResolver().registerContentObserver(WETALK_CONTENT_URI,
                true, sMissWeTalkObserver);

        getMissCallCount();
        getAllContactsUnreadCount(mContext);
        mStepCount = getSteps();
        mClassDisableData = getClassdisabledData(mContext);
    }

    public int getSteps(){
        int count = 0;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(STEPS_CONTENT_URI,null,"date="+Utils.getTodayStartTime(),null,null);
            if(c != null && c.moveToFirst()){
                count = c.getInt(c.getColumnIndex("steps"));
            }
            if(c != null){
                c.close();
                c = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(c != null){
                c.close();
                c = null;
            }
        }

        return count;
    }

    private int getContentCount(Uri uri, String where){
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = mContext.getContentResolver().query(uri, null, where, null,null);
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor){
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    public int getMissCallCount(){
        mMissCallCount = getContentCount(MISSCALL_CONTENT_URI, MISSCALL_WHERE);
        return mMissCallCount;
    }

    public int getMissCallCountImmediately(){
        return mMissCallCount;
    }

    public int getAllContactsUnreadCount(Context context){
        int count = 0;
        Cursor c = null;
        try{
            c = context.getContentResolver().query(WETALK_CONTENT_URI,
                    new String[]{"data6", ContactsContract.Data.RAW_CONTACT_ID},
                    ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.CommonDataKinds.StructuredPostal.TYPE +"=?",
                    new String[]{ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK+""},
                    null);

            if(c!=null && c.moveToFirst()){
                do {
                    int num = c.getInt(0);
                    count += num;
                } while (c.moveToNext());
                c.close();
                c = null;
            }else if(c!=null){
                c.close();
                c = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(c!=null){
                c.close();
                c = null;
            }
        }

        mMissWetalkCount = count;
        return count;
    }

    public boolean isNowEnable(){
        long time = System.currentTimeMillis();
        return isTimeEnable(mClassDisableData,time);
    }

    public boolean isTimeEnable(String data, long time){
        boolean isEnable = false;
        boolean isWeekEnable = false;
        boolean isTimeEnable = false;
        boolean isSingleTime = false;
        try {
            Date date = new Date(time);
            long startSetTime = Settings.Global.getLong(mContext.getContentResolver(),TAG_CLASS_DISABLED_TIME,0);
            Date startSetData = new Date(startSetTime);
            boolean isSameDay = isSameDay(date,startSetData);
            int week = (date.getDay() + 6) % 7;
            week = 1 << (6 - week);
            JSONObject jsonObject = new JSONObject(data);
            isEnable = jsonObject.optBoolean("enabled",false);
            String repeatStr = jsonObject.optString("repeat","0000000");
            int repeatWeek = Integer.parseInt(repeatStr,2);
            Log.d(TAG,"week:"+week+", repeatWeek:"+repeatWeek);
            isSingleTime = isSameDay && (repeatWeek == 0);
            isWeekEnable = (week & repeatWeek) != 0;
            JSONArray jsonArray = jsonObject.optJSONArray("time");
            int length = jsonArray.length();
            for(int i = 0; i < length; i++){
                JSONObject jsonSun = jsonArray.getJSONObject(i);
                String startTime = jsonSun.optString("start","00:00");
                String endTime = jsonSun.optString("end","00:00");
                String nowTime = mDateFormat.format(date);
                Date date1 = mDateFormat.parse(startTime.trim());
                Date date2 = mDateFormat.parse(endTime.trim());
                Date dateNow = mDateFormat.parse(nowTime.trim());
                Log.d(TAG,"startTime:"+startTime+", endTime:"+endTime+", nowTime:"+nowTime);
                if(dateNow.getTime() >= date1.getTime() && dateNow.getTime() < date2.getTime()){
                    isTimeEnable = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"isEnable:"+isEnable+", isWeekEnable:"+isWeekEnable+", isTimeEnable:"+isTimeEnable+", isSingleTime:"+isSingleTime);
        return isEnable && (isWeekEnable || isSingleTime) && isTimeEnable;
    }

    public boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        if (ds1.equals(ds2)) {
            return true;
        } else {
            return false;
        }
    }

    public String getClassdisabledData(Context context){
//        String s = "{\"repeat\":\"1111101\",\"time\":[{\"start\":\"02:00\",\"end\":\"12:00\"},{\"start\":\"14:00\",\"end\":\"17:30\"}],\"enabled\":false}";
//        return s;
        return Settings.Global.getString(context.getContentResolver(),TAG_CLASS_DISABLED);
    }

    private final ContentObserver sMissCallObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            new Thread(new Runnable() {
                public void run() {
                    synchronized (LOCK){
                        int phoneCount = getMissCallCount();
                        Message msg = mHandler.obtainMessage(CALL_MSG_WHAT, phoneCount, 0, null);
                        mHandler.sendMessage(msg);
                    }
                }
            }).start();
        }
    };

    private final ContentObserver sMissWeTalkObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            new Thread(new Runnable() {
                public void run() {
                    synchronized (LOCK){
                        int num = getAllContactsUnreadCount(mContext);
                        Message msg = mHandler.obtainMessage(WETALK_MSG_WHAT, num, 0, null);
                        mHandler.sendMessage(msg);
                    }
                }
            }).start();
        }
    };

    Handler mHandler= new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case CALL_MSG_WHAT:
                    Log.i(TAG,"miss call mun :"+msg.arg1);
                    for(CallUnreadChangedCallback callback : mCallUnreadChangedCallback) {
                        callback.onCallUnreadChanged(msg.arg1);
                    }
                    return;
                case WETALK_MSG_WHAT:
                    Log.i(TAG,"miss wetalk mun :"+msg.arg1);
                    for(WeTalkUnreadChangedCallback callback : mWeTalkUnreadChangedCallback) {
                        callback.onWeTalkUnreadChanged(+msg.arg1);
                    }
                    return;
            }
            super.dispatchMessage(msg);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return ;
        }
        if(TextUtils.equals(action,Intent.ACTION_DATE_CHANGED) ||
                TextUtils.equals(action,Intent.ACTION_TIMEZONE_CHANGED) ||
                TextUtils.equals(action,Intent.ACTION_TIME_CHANGED)){
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WEEK_NAME_CN_SHORT.length;
            for(DateChangedCallback callback : mDateChangedCallback){
                callback.onDateChange(year,month,day,week);
            }
        }else if(TextUtils.equals(action,Intent.ACTION_TIME_TICK)){
            if(!TextUtils.isEmpty(mClassDisableData)){
                classDisableChanged();
            }
        }else if(TextUtils.equals(action,ACTION_STEP_ADD)){
            int steps = intent.getIntExtra("steps", 0);
            mStepCount = steps;
            Log.i(TAG,"steps : " + steps);
            fireStepChanged();
        }else if(TextUtils.equals(action,READBOY_ACTION_CLASS_DISABLE_CHANGED)){
            mClassDisableData = Settings.Global.getString(context.getContentResolver(),TAG_CLASS_DISABLED);
            classDisableChanged();
        }else if(TextUtils.equals(action,Intent.ACTION_SCREEN_OFF)){
            if(mScreenOffListener != null){
                mScreenOffListener.onScreenOff();
            }
        }
    }

}
