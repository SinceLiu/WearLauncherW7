package com.readboy.wearlauncher.stepcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;

/**
 * Created by 1 on 2017/5/4.
 */

public class StepController extends BroadcastReceiver {
    private static final String TAG = "StepController";
    public static final String ACTION_STEP_ADD = "com.readboy.action.StepCountService.stepAdd";

    private Context mContext;
    private int mStepCount;
    private ArrayList<StepChangeCallback> mStepChangeCallback = new ArrayList<>();

    public StepController(Context context){
        mContext = context;
    }

    /** 开启计步监听*/
    public void registerStepAddReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STEP_ADD);
        mContext.registerReceiver(this, filter);
    }

    /** 注销计步监听*/
    public void unregisterStepAddReceiver() {
        mContext.unregisterReceiver(this);
    }

    public void addStepChangeCallback(StepChangeCallback cb){
        mStepChangeCallback.add(cb);
        cb.onStepChange(mStepCount);
    }

    public void removeStepChangeCallback(StepChangeCallback cb){
        mStepChangeCallback.remove(cb);
    }
    private void fireStepChange(){
        for (StepChangeCallback cb : mStepChangeCallback){
            cb.onStepChange(mStepCount);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_STEP_ADD.equals(intent.getAction())) {
            int steps = intent.getIntExtra("steps", 0);
            mStepCount = steps;
            fireStepChange();
        }
    }

    public interface StepChangeCallback {
        void onStepChange(int step);
    }
}
