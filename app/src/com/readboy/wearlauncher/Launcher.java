package com.readboy.wearlauncher;

import android.Manifest;
import android.app.ActivityManager;
import android.app.readboy.PersonalInfo;
import android.app.readboy.ReadboyWearManager;
import android.app.readboy.IReadboyWearListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.IPowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.readboy.wearlauncher.SimBind.SimBindController;
import com.readboy.wearlauncher.application.AppInfo;
import com.readboy.wearlauncher.application.AppsLoader;
import com.readboy.wearlauncher.battery.BatteryController;
import com.readboy.wearlauncher.dialog.ClassDisableDialog;
import com.readboy.wearlauncher.dialog.InstructionsDialog;
import com.readboy.wearlauncher.notification.NotificationActivity;
import com.readboy.wearlauncher.utils.ClassForbidUtils;
import com.readboy.wearlauncher.utils.Utils;
import com.readboy.wearlauncher.utils.WatchController;
import com.readboy.wearlauncher.view.DaialParentLayout;
import com.readboy.wearlauncher.view.DialBaseLayout;
import com.readboy.wearlauncher.view.GestureView;
import com.readboy.wearlauncher.view.MyViewPager;
import com.readboy.wearlauncher.view.NegativeScreen;
import com.readboy.wearlauncher.view.WatchAppGridView;
import com.readboy.wearlauncher.view.WatchDials;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class Launcher extends FragmentActivity implements BatteryController.BatteryStateChangeCallback,
        GestureView.MyGestureListener, WatchAppGridView.OnClickItemListener, LoaderManager.LoaderCallbacks<ArrayList<AppInfo>>, WatchController.ClassDisableChangedCallback,
        WatchController.ScreenOff, SimBindController.SimBindCallback {
    public static final String TAG = Launcher.class.getSimpleName();

    private LauncherApplication mApplication;
    private FragmentManager mFragmentManager;
    private static final int PERMISSIONS_REQUEST_CODE = 0x33;

    private static final int LOADER_ID = 0x10;

    private GestureView mGestureView;
    DialBaseLayout mLowDialBaseLayout;
    View mSimBindLayout;
    private LayoutInflater mInflater;
    private MyViewPager mViewpager;
    private ViewPagerAdpater mViewPagerAdpater;
    private List<View> mViewList = new ArrayList<View>();
    private NegativeScreen mNegativeView;
    private DaialParentLayout mDaialView;
    private WatchAppGridView mAppView;
    private WatchDials mWatchDials;
    int mTouchSlopSquare;
    int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private int mWatchType;
    private Toast mToast;

    WatchController mWatchController;
    BatteryController mBatteryController;
    int mBatteryLevel = -1;
    SimBindController mSimBindController;

    private boolean bIsClassDisable = false;
    private boolean bIsTouchable = false;

    private TelephonyManager mTelephonyManager;

    private static final String[] sPermissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        //screen width:240、height:240,density:0.75,densityDpi:120

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mFragmentManager = getSupportFragmentManager();
        ViewConfiguration configuration = ViewConfiguration.get(Launcher.this);
        int touchSlop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = touchSlop * 20;

        mApplication = (LauncherApplication) getApplication();
        mInflater = LayoutInflater.from(this);
        mWatchType = LauncherSharedPrefs.getWatchType(this);
        mBatteryController = new BatteryController(this);
        mBatteryController.addStateChangedCallback(this);

        mWatchController = mApplication.getWatchController();
        mWatchController.addClassDisableChangedCallback(this);

        mSimBindController = new SimBindController(this);
        mSimBindController.addSimBindCallbacks(this);

        mGestureView = (GestureView) findViewById(R.id.content_container);
        mGestureView.setGestureListener(this);
        mLowDialBaseLayout = (DialBaseLayout) findViewById(R.id.low);
        mLowDialBaseLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mToast == null) {
                        mToast = Toast.makeText(Launcher.this, R.string.notice_low_power_for_phone, Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.CENTER, 0, 0);
                        TextView textView = (TextView) mToast.getView().findViewById(android.R.id.message);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    }
                    mToast.setText(R.string.notice_low_power_for_phone);
                    mToast.show();
                }
                return true;
            }
        });

        mSimBindLayout = findViewById(R.id.sim_bind);
        mSimBindLayout.setVisibility(View.GONE);

        mNegativeView = (NegativeScreen) mInflater.inflate(R.layout.negative_screen, null);

        mDaialView = (DaialParentLayout) mInflater.inflate(R.layout.watch_dial_layout, null);
        mDaialView.removeAllViews();
        DialBaseLayout childDaialView = (DialBaseLayout) mInflater.inflate(WatchDials.mDialList.get(mWatchType % WatchDials.mDialList.size()), mDaialView, false);
        childDaialView.addChangedCallback();
        childDaialView.onResume();
        childDaialView.setButtonEnable();
        mDaialView.addView(childDaialView);
        mAppView = (WatchAppGridView) mInflater.inflate(R.layout.watch_app_gridview, null);
        mAppView.setOnClickItemListener(this);
        mViewList.clear();
        mViewList.add(mNegativeView);
        mViewList.add(mDaialView);
        mViewList.add(mAppView);
        loadApps(false);
        mViewpager = (MyViewPager) findViewById(R.id.viewpager);
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    mAppView.moveToTop();
                    mNegativeView.moveToTop();
                }
                if (bIsClassDisable && position == 2) {
                    mHandler.removeMessages(0x10);
                    mHandler.sendEmptyMessageDelayed(0x10, 1000 * 2);
                }

                // add by divhee start
                if (mViewPagerAdpater != null) {
                    mViewPagerAdpater.setNowPagerNumber(position);
                }
                // add by divhee end
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mViewPagerScrollState = state;
            }
        });
        mViewPagerAdpater = new ViewPagerAdpater(mViewList);
        mViewpager.setAdapter(mViewPagerAdpater);
        mViewpager.setOffscreenPageLimit(3);
        mViewpager.setCurrentItem(1);
        OverScrollDecoratorHelper.setUpOverScroll(mViewpager);
        mWatchController.setScreenOffListener(this);
        //startPowerAnimService();
        //Utils.setFirstBoot(Launcher.this,true);
        if (Utils.isFirstBoot(Launcher.this)) {
            InstructionsDialog.showInstructionsDialog(Launcher.this);
        }

        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        System.out.println("-----------  Provision USER_SETUP_COMPLETE");
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClassDisableDialog.recycle();
        mWatchController.removeClassDisableChangedCallback(this);
        mBatteryController.unregisterReceiver();
        mBatteryController.removeStateChangedCallback(this);
        mSimBindController.unregisterReceiver();
        mSimBindController.removeSimBindCallbacks(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bIsTouchable = false;
        closeDials(false);
        dialPasue();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadApps(true);

        // add by divhee start
        if (mViewpager != null && mViewpager.getAdapter() != null) {
            mViewpager.getAdapter().notifyDataSetChanged();
        }
        // add by divhee end

        bIsTouchable = true;
        LauncherApplication.setTouchEnable(true);
        //requestPermissions(sPermissions);
        forceUpdateDate();
        dialResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            Log.d(TAG, String.format("Action down (%f,%f)", ev.getRawX(), ev.getRawY()));
        if (!LauncherApplication.isTouchEnable() || !bIsTouchable) {
            return true;
        }
        if (Utils.isFirstBoot(Launcher.this)) {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                InstructionsDialog.showInstructionsDialog(Launcher.this);
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClassDisableChange(boolean show) {
        Log.e("cwj", "onClassDisableChange: show=" + show);
        if (bIsClassDisable != show) {
            bIsClassDisable = show;
            ClassForbidUtils.handleClassForbid(bIsClassDisable, Launcher.this);
//            ReadboyWearManager rwm = (ReadboyWearManager)Launcher.this.getSystemService(Context.RBW_SERVICE);
//            rwm.setClassForbidOpen(show);
            if(bIsClassDisable){
                if(needGoToHOme(Launcher.this,0)){
                    startActivity(new Intent(Launcher.this,Launcher.class));
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (needGoToHOme(Launcher.this, 0)) {
                            startActivity(new Intent(Launcher.this, Launcher.class));
                        }
                    }
                }, 500);
                if (mGestureView != null && mGestureView.getVisibility() == View.VISIBLE && mViewpager.getCurrentItem() != 1) {
                    mViewpager.setCurrentItem(1);
                }
                if (isHome(Launcher.this)) {
                    ClassDisableDialog.showClassDisableDialog(Launcher.this);
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.checkAndDealWithAirPlanMode(Launcher.this);
                    }
                }, 1000);
            } else {
                mHandler.removeMessages(0x10);
            }
        }
    }

    int lowPowerLevel = 15;

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        if (mGestureView == null || mLowDialBaseLayout == null) return;
        if (mBatteryLevel == -1 || mBatteryLevel != level) {
            mBatteryLevel = level;
            ReadboyWearManager rwm = (ReadboyWearManager) Launcher.this.getSystemService(Context.RBW_SERVICE);
            PowerManager mPowerManager = (PowerManager) Launcher.this.getSystemService(Context.POWER_SERVICE);
            if (mBatteryLevel < lowPowerLevel) {//low powe
                mGestureView.setVisibility(View.INVISIBLE);
                mLowDialBaseLayout.setVisibility(View.VISIBLE);
                mLowDialBaseLayout.addChangedCallback();
                mLowDialBaseLayout.onResume();
                mLowDialBaseLayout.setButtonEnable();
                rwm.setLowPowerMode(true);
                if (!mPowerManager.isPowerSaveMode()) {
                    mPowerManager.setPowerSaveMode(true);
                }
                if (needGoToHOme(Launcher.this, 1)) {
                    startActivity(new Intent(Launcher.this, Launcher.class));
                }
            } else {
                mGestureView.setVisibility(View.VISIBLE);
                mLowDialBaseLayout.setVisibility(View.GONE);
                rwm.setLowPowerMode(false);
                if (mPowerManager.isPowerSaveMode()) {
                    mPowerManager.setPowerSaveMode(false);
                }
            }
        } else {
            ReadboyWearManager rwm = (ReadboyWearManager) Launcher.this.getSystemService(Context.RBW_SERVICE);
            PowerManager mPowerManager = (PowerManager) Launcher.this.getSystemService(Context.POWER_SERVICE);
            if (level < lowPowerLevel && !mPowerManager.isPowerSaveMode()) {
                rwm.setLowPowerMode(true);
                mPowerManager.setPowerSaveMode(true);
            } else if (level >= lowPowerLevel && mPowerManager.isPowerSaveMode()) {
                rwm.setLowPowerMode(false);
                mPowerManager.setPowerSaveMode(false);
            }
        }
    }

    @Override
    public void onPowerSaveChanged() {

    }

    /// add by cwj start @{
    String sim = null;
    Button btn_shutdown;
    Button btn_bind;

    View.OnClickListener mSimBindOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_shutdown:
                    shutDown();
                    break;
                case R.id.btn_bind:
                    Log.d("cwj", "onClick btn_bind");
                    if (sim == null)
                        return;
                    btn_bind.setEnabled(false);
                    simBind();
                    break;
            }
        }
    };

    /**
     * 关机
     */
    private void shutDown() {
        try {
            IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
            pm.shutdown(false, false);
        } catch (Exception e) {
            Log.e("cwj", "shutDown error:" + e);
            return;
        }
    }

    /**
     * 绑定
     */
    private void simBind() {
        if (TextUtils.isEmpty(sim)) {
            Log.e("cwj", "sim is empty " + sim);
            simBindHand.sendEmptyMessage(0);
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("sim", sim);
            String dataStr = data.toString();
            Log.d("cwj", dataStr);
            ReadboyWearManager mServiceManger = (ReadboyWearManager) this.getSystemService(Context.RBW_SERVICE);
            mServiceManger.customRequest("carrier", "bindsim", dataStr, new IReadboyWearListener.Stub() {

                @Override
                public void pushSuc(String cmd, String serial, int code, String data, String result) throws RemoteException {
                    Log.d("cwj",
                            "data:" + data + "\n" + "result:" + result);

                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject resultJs = new JSONObject(result);
                            if ((!resultJs.isNull("n") && resultJs.getInt("n") != 0)) {
                                simBindHand.sendEmptyMessage(0);
                            } else {
                                simBindHand.sendEmptyMessage(1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        simBindHand.sendEmptyMessage(0);
                    }
                }

                @Override
                public void pushFail(String cmd, String serial, int code, String errorMsg) throws RemoteException {
                    Log.e("cwj", "pushFail");
                    simBindHand.sendEmptyMessage(0);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Handler simBindHand = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mToast == null) {
                mToast = Toast.makeText(Launcher.this, R.string.sim_bind_fail_tip, Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.CENTER, 0, 0);
                TextView textView = (TextView) mToast.getView().findViewById(android.R.id.message);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            }
            switch (msg.what) {
                case 0:
                    btn_bind.setEnabled(true);
                    Log.e("cwj", "bindFail");

                    mToast.setText(R.string.sim_bind_fail_tip);
                    mToast.show();
                    break;
                case 1:
                    onDriverStatusChanged(true, null);
                    if (!TextUtils.isEmpty(sim)) {
                        Settings.Global.putString(Launcher.this.getContentResolver(), "bind_sim", sim);
                    }
                    sim = null;

                    mToast.setText(R.string.sim_bind_succ_tip);
                    mToast.show();
                    break;
            }
        }
    };

    @Override
    public void onBindStart(String sim) {
        Log.d("cwj", "onBindStart");
        if (mGestureView == null || mLowDialBaseLayout == null || mSimBindLayout == null) return;
        if (sim == null) return;
        this.sim = sim;
        mSimBindLayout.setVisibility(View.VISIBLE);
        mSimBindLayout.setOnClickListener(mSimBindOnClickListener);
        if (!isHome(Launcher.this)) {
            startActivity(new Intent(Launcher.this, Launcher.class));
        }
        mSimBindLayout.findViewById(R.id.binding_layout).setVisibility(View.VISIBLE);
        mSimBindLayout.findViewById(R.id.disable_layout).setVisibility(View.GONE);

        TextView tv_bind_tip_item_1_number = (TextView) mSimBindLayout.findViewById(R.id.tv_bind_tip_item_1_number);
        if (sim == null) sim = "";
        tv_bind_tip_item_1_number.setText("   " + sim);

        mSimBindLayout.findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        if (btn_shutdown == null) {
            btn_shutdown = (Button) mSimBindLayout.findViewById(R.id.btn_shutdown);
            btn_shutdown.setOnClickListener(mSimBindOnClickListener);
        }
        if (btn_bind == null) {
            btn_bind = (Button) mSimBindLayout.findViewById(R.id.btn_bind);
        }
        btn_shutdown.setVisibility(View.VISIBLE);
        btn_shutdown.setBackgroundResource(R.drawable.btn_sim_bind_shutdown_left);
        btn_bind.setVisibility(View.VISIBLE);
        btn_bind.setEnabled(true);
        btn_bind.setOnClickListener(mSimBindOnClickListener);
    }

    @Override
    public void onDriverStatusChanged(boolean enable, String reason) {
        Log.d("cwj", "onDriverStatusChanged enable=" + enable);
        if (mSimBindLayout == null) return;
        if (!enable) {
            mSimBindLayout.setVisibility(View.VISIBLE);
            mSimBindLayout.setOnClickListener(mSimBindOnClickListener);
            if (!isHome(Launcher.this)) {
                startActivity(new Intent(Launcher.this, Launcher.class));
            }
            mSimBindLayout.findViewById(R.id.binding_layout).setVisibility(View.GONE);
            mSimBindLayout.findViewById(R.id.disable_layout).setVisibility(View.VISIBLE);

            TextView tv_info = (TextView) mSimBindLayout.findViewById(R.id.tv_info);
            if (reason != null) {
                tv_info.setText(reason);
            } else {
                tv_info.setText(R.string.sim_bind_driver_disable_default);
            }

            mSimBindLayout.findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
            if (btn_shutdown == null) {
                btn_shutdown = (Button) mSimBindLayout.findViewById(R.id.btn_shutdown);
                btn_shutdown.setOnClickListener(mSimBindOnClickListener);
            }
            if (btn_bind == null) {
                btn_bind = (Button) mSimBindLayout.findViewById(R.id.btn_bind);
            }
            btn_shutdown.setVisibility(View.VISIBLE);
            btn_shutdown.setBackgroundResource(R.drawable.btn_sim_bind_shutdown_single);
            btn_bind.setVisibility(View.GONE);
            btn_bind.setOnClickListener(null);
        } else {
            mSimBindLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNotCMCC(String sim) {
        onDriverStatusChanged(false, this.getResources().getString(R.string.sim_bind_not_cmcc));
    }
    /// end @}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mSimBindLayout.getVisibility() == View.VISIBLE) {
            return false;
        }
        if (WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED) {
            return false;
        }
        int cur = mViewpager.getCurrentItem();
        if (e1 == null || e2 == null || cur != 1) {
            return false;
        }
        float vDistance = e1.getY() - e2.getY();
        boolean bVerticalMove = Math.abs(velocityX) - Math.abs(velocityY) < 0;
        Log.e(TAG, "vDistance: " + vDistance + "            bVerticalMove: " + bVerticalMove +
                "   " + (mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE));
        if (vDistance > mTouchSlopSquare / 5 && bVerticalMove /*&& mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE*/) {
            ReadboyWearManager rwm = (ReadboyWearManager) Launcher.this.getSystemService(Context.RBW_SERVICE);
            PersonalInfo info = rwm.getPersonalInfo();
            if (info != null && info.isHasSiri() == 1) {
                Log.e(TAG, "onFling: start Speech activity.");
                Utils.startActivity(Launcher.this, "com.readboy.watch.speech", "com.readboy.watch.speech.Main2Activity");
            } else {
                Utils.startActivity(Launcher.this, "com.android.settings", "com.android.qrcode.MainActivity");
            }
            return true;
        } else if (vDistance < -mTouchSlopSquare / 2 && bVerticalMove /*&& mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE*/) {
            /*boolean isEnable = ((LauncherApplication) LauncherApplication.getApplication()).getWatchController().isNowEnable();*/
            ReadboyWearManager rwm = (ReadboyWearManager)Launcher.this.getSystemService(Context.RBW_SERVICE);
            boolean isEnable = rwm.isClassForbidOpen();
            if (isEnable) {
                ClassDisableDialog.showClassDisableDialog(Launcher.this);
                Utils.checkAndDealWithAirPlanMode(Launcher.this);
                return true;
            }
            if (!isNotificationEnabled()) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } else {
                startActivity(new Intent(Launcher.this, NotificationActivity.class));
                Log.d(TAG, "lzx switchToFragment");
                //Utils.startActivity(Launcher.this, "com.readboy.wearlauncher",NotificationActivity.class.getName());
                //((Launcher)getActivity()).switchToFragment(NotificationFragment.class.getName(),null,true,true);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mSimBindLayout.getVisibility() == View.VISIBLE) {
            return false;
        }
        if (WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED) {
            mGestureView.setIsGestureDrag(true);
            closeDials(true);
            mWatchType = LauncherSharedPrefs.getWatchType(Launcher.this);
            setDialFromType(mWatchType);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mSimBindLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        if (WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENING ||
                WatchDials.getWatchDialsStatus() == WatchDials.ANIMATE_STATE_OPENED) {
            return;
        }
        int cur = mViewpager.getCurrentItem();
        if (cur == 1) {
            mGestureView.setIsGestureDrag(true);
            openDials();
        }
    }

    @Override
    public void onClick(int position) {
        AppInfo info = mAppView.getAppInfo(position);
        Utils.startActivity(Launcher.this, info.mPackageName, info.mClassName);
    }

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            return null;
        }
        return new AppsLoader(Launcher.this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader, ArrayList<AppInfo> appInfos) {
        if (loader.getId() == LOADER_ID) {
            mAppView.refreshData(appInfos);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {

    }

    @Override
    public void onScreenOff() {
        if (mGestureView != null && mGestureView.getVisibility() == View.VISIBLE && isHome(Launcher.this)
                && mViewpager.getCurrentItem() != 1) {
            mViewpager.setCurrentItem(1);
        }
    }

    class ViewPagerAdpater extends PagerAdapter {
        public List<View> mViewList;
        // add by divhee start
        private int nowPagerNumber = -1;
        // add by divhee end

        public ViewPagerAdpater(List<View> viewList) {
            this.mViewList = viewList;
        }

        public void setData(List<View> viewList) {
            mViewList = viewList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mViewList != null ? mViewList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position >= 0 && position < mViewList.size()) {
                container.removeView(mViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // TODO Auto-generated method stub
            container.addView(mViewList.get(position));
            // add by divhee start
            mViewList.get(position).setTag(R.id.launcher_viewpager_pager_postion_id, position);
            // add by divhee end
            return mViewList.get(position);
        }

        // add by divhee start
        @Override
        public int getItemPosition(Object object) {
            if (object != null && object instanceof View) {
                try {
                    int objPagerIdx = (Integer) ((View) object).getTag(R.id.launcher_viewpager_pager_postion_id);
                    if (nowPagerNumber == objPagerIdx) {
                        return POSITION_NONE;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.getItemPosition(object);
        }

        public void setNowPagerNumber(int pagerNumber) {
            nowPagerNumber = pagerNumber;
            //Log.e("", "====divhee==setNowPagerNumber=="+nowPagerNumber);
        }

        public int getNowPagerNumber() {
            return nowPagerNumber;
        }
        // add by divhee end
    }

    public Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (mGestureView != null && mGestureView.getVisibility() == View.VISIBLE && mViewpager.getCurrentItem() == 2) {
                mViewpager.setCurrentItem(1);
            }
            super.dispatchMessage(msg);
        }
    };

    private void requestPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int permissionStatus = checkSelfPermission(permission);
                Log.v(TAG, "permissions=" + permissions + ",permissionStatus=" + permissionStatus);
                if (permissionStatus != PackageManager.PERMISSION_GRANTED/* && shouldShowRequestPermissionRationale(permission)*/) {
                    requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
                    break;
                }
            }
        }
    }

    private boolean isGrantedPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int permissionStatus = checkSelfPermission(permission);
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNotificationEnabled() {
        String pkgName = this.getPackageName();
        final String flat = Settings.Secure.getString(Launcher.this.getContentResolver(),
                "enabled_notification_listeners");
        Log.d(TAG, "notification flat:" + flat);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void forceUpdateDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = (calendar.get(Calendar.DAY_OF_WEEK) - 1) % WatchController.WEEK_NAME_CN_SHORT.length;
        if (mLowDialBaseLayout != null && mLowDialBaseLayout.isShown()) {
            mLowDialBaseLayout.onDateChange(year, month, day, week);
        }
        if (mDaialView != null /*&& mDaialView.isShown()*/) {
            View view = mDaialView.getChildAt(0);
            if (view instanceof DialBaseLayout) {
                ((DialBaseLayout) view).onDateChange(year, month, day, week);
            }
        }
    }

    private void dialPasue() {
        if (mLowDialBaseLayout != null && mLowDialBaseLayout.isShown()) {
            mLowDialBaseLayout.onPause();
            mLowDialBaseLayout.removeChangedCallback();
        }
        if (mDaialView != null /*&& mDaialView.isShown()*/) {
            View view = mDaialView.getChildAt(0);
            if (view instanceof DialBaseLayout) {
                ((DialBaseLayout) view).onPause();
                ((DialBaseLayout) view).removeChangedCallback();
            }
        }
    }

    private void dialResume() {
        Log.d("cwj", "dialResume");
        if (mLowDialBaseLayout != null && mLowDialBaseLayout.isShown()) {
            mLowDialBaseLayout.onResume();
            mLowDialBaseLayout.addChangedCallback();
        }

        if (mDaialView != null /*&& mDaialView.isShown()*/) {
            View view = mDaialView.getChildAt(0);
            if (view instanceof DialBaseLayout) {
                ((DialBaseLayout) view).onResume();
                ((DialBaseLayout) view).addChangedCallback();
            }
        }
    }

    private void forceCloseWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        synchronized (TAG) {
            if (wakeLock != null) {
                Log.v(TAG, "Releasing wakelock");
                try {
                    wakeLock.release();
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            } else {
                Log.e(TAG, "Wakelock reference is null");
            }
        }
    }

    public void startPowerAnimService() {
        Intent intent = new Intent();
        ComponentName component = new ComponentName("com.readboy.floatwindow", "com.readboy.floatwindow.FloatWindowService");
        intent.setComponent(component);
        Intent tmp = Utils.createExplicitFromImplicitIntent(Launcher.this, intent);
        if (tmp != null) {
            try {
                Intent eintent = new Intent(tmp);
                Launcher.this.startService(eintent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadApps(boolean reLoad) {
        if (reLoad) {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void openDials() {
        mWatchDials = WatchDials.fromXml(Launcher.this);
        mGestureView.cancelLongPress();
        mGestureView.addView(mWatchDials);
        mWatchDials.animateOpen();
    }

    private void closeDials(boolean saveMode) {
        if (mWatchDials != null && mWatchDials.isShown()) {
            mWatchDials.animateClose(saveMode);
        }
    }

    private void setDialFromType(int type) {
        View view = mDaialView.getChildAt(0);
        if (view instanceof DialBaseLayout) {
            ((DialBaseLayout) view).onPause();
            ((DialBaseLayout) view).removeChangedCallback();
        }
        mDaialView.removeAllViews();
        DialBaseLayout childDaialView = (DialBaseLayout) mInflater.inflate(WatchDials.mDialList.get(type % WatchDials.mDialList.size()), mDaialView, false);
        childDaialView.addChangedCallback();
        childDaialView.onResume();
        childDaialView.setButtonEnable();
        mDaialView.addView(childDaialView);
        mViewPagerAdpater.setData(mViewList);
        dialResume();
    }

    private boolean needGoToHOme(Context context, int type) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTask = manager.getRunningTasks(1);
        String _pkgName = null;
        String topActivityName = null;
        if (runningTask != null) {
            _pkgName = runningTask.get(0).topActivity.getPackageName();
            topActivityName = runningTask.get(0).topActivity.getClassName();
        } else {
            return false;
        }

        if (_pkgName != null && topActivityName != null) {
            if (type == 1 && _pkgName.equals("com.android.dialer")) {
                return false;
            } else if (_pkgName.equals("com.android.dialer") && topActivityName.equals("com.android.incallui.InCallActivity")) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                return false;
            } else if (_pkgName.equals("com.readboy.wearlauncher") && topActivityName.equals("com.readboy.wearlauncher.Launcher")) {
                return false;
            }
        }

        return true;
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.e("cwj", "needGoToHOme state " + state + " " + TelephonyManager.CALL_STATE_IDLE);
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*boolean isEnable = ((LauncherApplication)
                                LauncherApplication.getApplication()).getWatchController().isNowEnable();*/
			            ReadboyWearManager rwm = (ReadboyWearManager)Launcher.this.getSystemService(Context.RBW_SERVICE);
			            boolean isEnable = rwm.isClassForbidOpen();
                        Log.e("cwj", "needGoToHOme isEnable " + isEnable);
                        if (isEnable) {
                            startActivity(new Intent(Launcher.this, Launcher.class));
                            Log.e("cwj", "needGoToHOme startActivity");
                            if (isHome(Launcher.this)) {
                                ClassDisableDialog.showClassDisableDialog(Launcher.this);
                            }
                        }
                    }
                }, 500);
            }
        }
    };

    private boolean isHome(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTask = manager.getRunningTasks(1);
        String _pkgName = null;
        String topActivityName = null;
        if (runningTask != null) {
            _pkgName = runningTask.get(0).topActivity.getPackageName();
            topActivityName = runningTask.get(0).topActivity.getClassName();
        }
        if (_pkgName != null && topActivityName != null) {
            if (_pkgName.equals("com.readboy.wearlauncher") && topActivityName.equals("com.readboy.wearlauncher.Launcher")) {
                return true;
            }
        }
        return false;
    }

    private Fragment getVisibleFragment(FragmentManager fragmentManager) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null) {
            return null;
        }
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()
                    && fragment.getUserVisibleHint()) {
                return fragment;
            }
        }

        return null;
    }

    public Fragment switchToFragment(String fragmentName, Bundle args,
                                     boolean addToBackStack, boolean withTransition) {

        Fragment f = Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        if (withTransition) {
//            transaction.setCustomAnimations(R.anim.push_in_right,
//                    R.anim.push_out_right, R.anim.back_in_left,
//                    R.anim.back_out_left);
//        }
        if (addToBackStack) {
            transaction.addToBackStack(fragmentName.getClass().getSimpleName());
        }
        transaction.replace(R.id.content_container, f, fragmentName);
        transaction.commitAllowingStateLoss();
        mFragmentManager.executePendingTransactions();
        return f;
    }
}
