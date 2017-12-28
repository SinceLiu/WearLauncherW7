package com.readboy.wearlauncher.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.readboy.wearlauncher.R;

/**
 * Created by Administrator on 2017/7/11.
 */

public class ClassDisableDialog {
    private static final String TAG = "ClassDisableDialog";

    private static Dialog dialog = null;
    private static Context mContext;

    public static void showClassDisableDialog(Context context) {
        if (dialog == null) {
            mContext = context;
            dialog = new Dialog(context, R.style.dialog_fs);//(new AlertDialog.Builder(context)).create();
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        //dialog.getWindow().getAttributes().systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //View view = View.inflate(context, R.layout.dialog_class_disable, null);
        dialog.getWindow().setContentView(R.layout.dialog_class_disable);
        //TODO 不应该每次都设置，和new Dialog冲突
        dialog.getWindow().getDecorView().findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissClassDisableDialog();
            }
        });
        dialog.show();
        mHandler.removeMessages(0x10);
        mHandler.sendEmptyMessageDelayed(0x10, 1000 * 2);
//        DisplayMetrics dm = new DisplayMetrics();
//        dm = context.getApplicationContext().getResources().getDisplayMetrics();
//        int screenWidth = dm.widthPixels;
//        int screenHeight = dm.heightPixels;
//        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//        params.width = screenWidth;
//        params.height = screenHeight;
//        dialog.getWindow().setAttributes(params);
    }

    public static void recycle(){
        mHandler.removeMessages(0x10);
        dialog = null;
    }

    public static void dismissClassDisableDialog() {
        Log.e(TAG, "dismissClassDisableDialog: ");
        if (!isValidContext(mContext)) {
            Log.e(TAG, "dismissClassDisableDialog: context is disabled");
            mHandler.removeMessages(0x10);
            dialog = null;
            mContext = null;
            return;
        }

        if (dialog != null && dialog.isShowing()) {
            mHandler.removeMessages(0x10);
            dialog.dismiss();
            dialog = null;
        }
    }

    private static boolean isValidContext(Context c) {
        if (c instanceof Activity) {
            Log.e(TAG, "isValidContext: activity");
            Activity a = (Activity) c;
            if (a.isDestroyed() || a.isFinishing()) {
                Log.e(TAG, "isValidContext: false");
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    static Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            dismissClassDisableDialog();
            super.dispatchMessage(msg);
        }
    };
}
