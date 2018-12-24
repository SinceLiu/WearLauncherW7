package com.readboy.wearlauncher.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.dialog.ClassDisableDialog;
import com.readboy.wearlauncher.view.DialBaseLayout;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Utils {
	
	//Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS
	public static final String AIRPLANE_MODE_TOGGLEABLE_RADIOS = "airplane_mode_toggleable_radios";

	public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static void checkAndDealWithAirPlanMode(Context context){
        if (!isAirplaneModeOn(context)) return;
        Intent intent = new Intent("com.readboy.settings.AirplaneModeReset");
        context.startActivity(intent);
    }
	
	public static boolean isRadioAllowed(Context context, String type) {
        if (!Utils.isAirplaneModeOn(context)) {
            return true;
        }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.Global.getString(context.getContentResolver(),
                AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }
	
	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {  
        // Retrieve all services that can match the given intent  
        PackageManager pm = context.getPackageManager();  
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);  
        // Make sure only one match was found  
        if (resolveInfo == null || resolveInfo.size() != 1) {  
            return null;  
        }  
        // Get component info and create ComponentName  
        ResolveInfo serviceInfo = resolveInfo.get(0);  
        String packageName = serviceInfo.serviceInfo.packageName;  
        String className = serviceInfo.serviceInfo.name;  
        ComponentName component = new ComponentName(packageName, className);  
        // Create a new intent. Use the old one for extras and such reuse  
        Intent explicitIntent = new Intent(implicitIntent);  
        // Set the component to be explicit  
        explicitIntent.setComponent(component);  
   
        return explicitIntent;  
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        return p.x;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        return p.y;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void startActivity(Context context, String pkg, String cls){
        List<String> ableEnterList = Arrays.asList(context.getResources().getStringArray(
                R.array.ableEnterList));
        boolean isEnable = ((LauncherApplication)LauncherApplication.getApplication()).getWatchController().isNowEnable();
        if(isEnable && !ableEnterList.contains(pkg)){
            ClassDisableDialog.showClassDisableDialog(context);
            checkAndDealWithAirPlanMode(context);
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            if(TextUtils.equals(DialBaseLayout.DIALER_PACKAGE_NAME,pkg) &&
                    ((LauncherApplication) LauncherApplication.getApplication()).getWatchController().getMissCallCountImmediately() > 0){
                intent.setType(android.provider.CallLog.Calls.CONTENT_TYPE);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setClassName(pkg, cls);
            context.startActivity(intent);
            LauncherApplication.setTouchEnable(false);
            Log.d("TEST","start activity pkg:"+pkg+", cls:"+cls);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("TEST","Can not find pkg:"+pkg+", cls:"+cls);
            Toast.makeText(context,"Can not find pkg:"+pkg+",\ncls:"+cls,Toast.LENGTH_SHORT).show();
        }
    }

    private static Activity getActivity(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }else {
            return null;
        }
    }

    public static boolean isFirstBoot(Context context){
        if(Settings.System.getInt(context.getContentResolver(),"readboy_first_open",0) != 1){
            return true;
        }
        return false;
    }
    public static void setFirstBoot(Context context, boolean firstBoot){
        if(firstBoot){
            Settings.System.putInt(context.getContentResolver(),"readboy_first_open",0);
        }else {
            Settings.System.putInt(context.getContentResolver(),"readboy_first_open",1);
        }
    }

    /**
     * 建议模糊度(在0.0到25.0之间)
     */
    private static final int BLUR_RADIUS = 20;
    private static final int SCALED_WIDTH = 100;
    private static final int SCALED_HEIGHT = 100;
    /**
     * 得到模糊后的bitmap
     * thanks http://wl9739.github.io/2016/07/14/教你一分钟实现模糊效果/
     *
     * @param context
     * @param bitmap
     * @param radius
     * @return
     */
    public static Bitmap getBlurBitmap(Context context, Bitmap bitmap, int radius) {
        // 将缩小后的图片做为预渲染的图片。
        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, SCALED_WIDTH, SCALED_HEIGHT, false);
        // 创建一张渲染后的输出图片。
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(radius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static void startSwitchBackgroundAnim(ImageView view, Bitmap bitmap) {
        Drawable oldDrawable = view.getDrawable();
        Drawable oldBitmapDrawable ;
        TransitionDrawable oldTransitionDrawable = null;
        if (oldDrawable instanceof TransitionDrawable) {
            oldTransitionDrawable = (TransitionDrawable) oldDrawable;
            oldBitmapDrawable = oldTransitionDrawable.findDrawableByLayerId(oldTransitionDrawable.getId(1));
        } else if (oldDrawable instanceof BitmapDrawable) {
            oldBitmapDrawable = oldDrawable;
        } else {
            oldBitmapDrawable = new ColorDrawable(0xffc2c2c2);
        }

        if (oldTransitionDrawable == null) {
            oldTransitionDrawable = new TransitionDrawable(new Drawable[]{oldBitmapDrawable, new BitmapDrawable(bitmap)});
            oldTransitionDrawable.setId(0, 0);
            oldTransitionDrawable.setId(1, 1);
            oldTransitionDrawable.setCrossFadeEnabled(true);
            view.setImageDrawable(oldTransitionDrawable);
        } else {
            oldTransitionDrawable.setDrawableByLayerId(oldTransitionDrawable.getId(0), oldBitmapDrawable);
            oldTransitionDrawable.setDrawableByLayerId(oldTransitionDrawable.getId(1), new BitmapDrawable(bitmap));
        }
        oldTransitionDrawable.startTransition(1000);
    }

    /** 获取今天零时时间戳*/
    public static long getTodayStartTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long dayStartTime = calendar.getTimeInMillis();
        dayStartTime = dayStartTime - dayStartTime % 1000;
        return dayStartTime;
    }

    public static  Drawable addShadow(Context context,Drawable src) {
        if (src == null ) {
            return src;
        }

        Bitmap b = drawableToBitmap(src);

        return new BitmapDrawable(context.getResources(), addShadow(b));
    }

    public static Bitmap addShadow(Bitmap bitmap) {
        int shadowWidth = 10;
        int shadowHeight = 10;
        int width = bitmap.getWidth() + shadowWidth;
        int height = bitmap.getHeight() + shadowHeight;

        Rect dst = new Rect(0, 0, width, height);
        Rect src = new Rect(0, 0, width - shadowWidth, height - shadowHeight);

        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Paint blurPaint = new Paint();

        BlurMaskFilter bf = new BlurMaskFilter(20, BlurMaskFilter.Blur.INNER);
        blurPaint.setColor(0xff000000);
        blurPaint.setMaskFilter(bf);

        Bitmap result = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);

        canvas.drawBitmap(bitmap.extractAlpha(blurPaint, null), src, dst,
                blurPaint);
        canvas.drawBitmap(bitmap, shadowWidth / 2, shadowHeight / 2, paint);

        canvas.setBitmap(null);

        return result;
    }

    private static Bitmap drawableToBitmap(Drawable d) {
        if (d == null)
            return null;

        int width = d.getIntrinsicWidth();
        int height = d.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, width, height);
        Rect oldBound = d.copyBounds();
        d.setBounds(rect);
        d.draw(canvas);
        d.setBounds(oldBound);
        canvas.setBitmap(null);
        return bitmap;
    }

    public static boolean isEmpty(CharSequence str){
        if (str == null || str.length() == 0 || str.equals("null") || str.equals("NULL"))
            return true;
        else
            return false;
    }
}
