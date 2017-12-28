package com.readboy.wearlauncher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ScrollView;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.Utils;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InstructionsDialog {
    private static Dialog dialog = null;
    public static void showInstructionsDialog(final Context context){
        if(dialog == null){
            dialog = new Dialog(context, R.style.dialog_fs);
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setContentView(R.layout.dialog_instructions);
        ScrollView scrollView = (ScrollView) dialog.getWindow().getDecorView().findViewById(R.id.scrollView);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        dialog.getWindow().getDecorView().findViewById(R.id.ok_bid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setFirstBoot(context,false);
                dismissInstructionsDialog();
            }
        });
        dialog.getWindow().getDecorView().findViewById(R.id.cancel_bid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setFirstBoot(context,true);
                dismissInstructionsDialog();
            }
        });
        dialog.show();
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = screenWidth;
        params.height = screenHeight;
        dialog.getWindow().setAttributes(params);
    }

    public static void dismissInstructionsDialog(){
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
