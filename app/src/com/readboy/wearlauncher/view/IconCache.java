/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readboy.wearlauncher.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.R;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 10;
    private static int APP_ICON_SIZE;
    private final ArrayList<AppIconCache> mAppIconHashMaps = new ArrayList<AppIconCache>();

    private static class AppIconCache {
        private String packagesName;
        private int resourceIconId;
        private String resourceIconPath;

        public AppIconCache(String pkg, int resId) {
            packagesName = pkg;
            resourceIconId = resId;
        }

        public AppIconCache(String pkg, String path) {
            packagesName = pkg;
            resourceIconPath = path;
        }

        public boolean contains(final String fullPackageName) {
            if (fullPackageName == null || fullPackageName.length() <= 0)
                return false;
            if (packagesName == null || packagesName.length() <= 0)
                return false;

            String lower = fullPackageName.toLowerCase();
            String srcLower = packagesName.toLowerCase();
            return lower.contains(srcLower);
        }

        public boolean equals(final String fullPackageName) {
            if (fullPackageName == null || fullPackageName.length() <= 0)
                return false;
            if (packagesName == null || packagesName.length() <= 0)
                return false;

            String lower = fullPackageName.toLowerCase();
            String srcLower = packagesName.toLowerCase();

            return TextUtils.equals(lower, srcLower);
        }

        public int getResId() {
            return resourceIconId;
        }

        public String getResPath() {
            return resourceIconPath;
        }
    }

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
    }

    private final Bitmap mDefaultIcon;
    private final LauncherApplication mContext;
    private final PackageManager mPackageManager;
    private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
            INITIAL_ICON_CACHE_CAPACITY);
    private static int mIconDpi;
    private static String mScreenDPI = null;

    public IconCache(LauncherApplication context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        mContext = context;
        mPackageManager = context.getPackageManager();
        mIconDpi = activityManager.getLauncherLargeIconDensity();
        APP_ICON_SIZE = (int) mContext.getResources().getDimension(R.dimen.app_icon_size);
        init();

        switch (mIconDpi) {
            case DisplayMetrics.DENSITY_LOW:
                mScreenDPI = "ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                mScreenDPI = "mdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                mScreenDPI = "hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                mScreenDPI = "xhdpi";
                break;
            default:
                mScreenDPI = "xhdpi";
        }
        Log.i(TAG, "mScreenDPI: " + mScreenDPI + ",mIconDpi:" + mIconDpi);
        // need to set mIconDpi before getting default icon
        mDefaultIcon = makeDefaultIcon();
    }

    private void init() {
        mAppIconHashMaps.clear();
        loadDefault();
    }

    private void putIconCache(String pkg, int resId) {
        mAppIconHashMaps.add(new AppIconCache(pkg, resId));
    }

    private void putIconCache(String pkg, String path) {
        mAppIconHashMaps.add(new AppIconCache(pkg, path));
    }

    private void loadDefault() {
        initIcon();
        initIconMask();
    }

    private void initIcon() {
        //putIconCache("com.dream.calculator", R.drawable.calculator);
    }

    private void initIconMask() {
        final Resources res = mContext.getResources();
        mIconMask = res.getDrawable(R.drawable.icon_mask);
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(null, info.activityInfo);
    }

    private Drawable getFullResIcon(ComponentName name, ResolveInfo info) {
        return getFullResIcon(name, info.activityInfo);
    }

    /**
     * get the application icon bitmap.
     *
     * @param info
     * @return
     */
    private Drawable getApplicationIcon(ComponentName name, ActivityInfo info) {
        String clsName = null;
        String pkgName = null;

        if (name != null) {
            clsName = name.getClassName();
        }

        if (TextUtils.isEmpty(clsName)) {
            clsName = info.applicationInfo.className;
        }

        if (name != null) {
            pkgName = name.getPackageName();
        }

        if (TextUtils.isEmpty(pkgName)) {
            pkgName = info.packageName;
        }

        if (TextUtils.isEmpty(clsName) && TextUtils.isEmpty(pkgName)) {
            return null;
        }

        clsName = TextUtils.isEmpty(clsName) ? "" : clsName.toLowerCase();
        pkgName = TextUtils.isEmpty(pkgName) ? "" : pkgName.toLowerCase();

        Resources res = mContext.getResources();
        // Log.i(TAG, "ActivityInfo: clsName锛? + clsName+",pkgName:"+pkgName);
        int resIconId = 0;
        String resIconPath = null;

        for (AppIconCache iconCache : mAppIconHashMaps) {
            if (iconCache.equals(clsName)) {
                resIconId = iconCache.getResId();
                resIconPath = iconCache.getResPath();
                break;
            }
        }
        if (resIconId == 0) {
            for (AppIconCache iconCache : mAppIconHashMaps) {
                if (iconCache.equals(pkgName)) {
                    resIconId = iconCache.getResId();
                    resIconPath = iconCache.getResPath();
                    break;
                }
            }
        }

        if (resIconId != 0) { // find the icon Drawable Id
            return res.getDrawable(resIconId);
        }

        return null;
    }

    public Drawable getFullResIcon(ActivityInfo info) {
        return getFullResIcon(null, info);
    }

    private Drawable getFullResIcon(ComponentName name, ActivityInfo info) {
        // Find out the preview Icon
        Drawable d = getApplicationIcon(name, info);
        if (d != null) {
            if (mIconType == TYPE_ALL) {
                return addIconMask(d);
            }

            return addShadow(d);
        }
        Resources resources;
        try {
            resources = mPackageManager
                    .getResourcesForApplication(info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }

        // system app
        if ((info.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            Drawable drawable = null;
            if (resources != null) {
                int iconId = info.getIconResource();
                if (iconId != 0) {
                    drawable = getFullResIcon(resources, iconId);
                }
            }
            if (drawable == null) {
                drawable = getFullResDefaultActivityIcon();
            }
            if (mIconType == TYPE_ALL) {
                return addIconMask(drawable);
            }
            return addShadow(drawable);
        }

        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return addIconMask(getFullResIcon(resources, iconId));
            }
        }
        return addIconMask(getFullResDefaultActivityIcon());
    }

    private Drawable mIconMask;
    private Bitmap mIconBitmapMask;
    private int mIconType = TYPE_NONE;

    private static final int TYPE_NONE = 0; // 0-涓嶅姞鑳屾櫙
    private static final int TYPE_THIRD = 1; // 1-绗笁鏂瑰姞鑳屾櫙
    private static final int TYPE_ALL = 2; // 2-鍏ㄩ儴鍔犺儗鏅?

    public Bitmap drawableToBitmap(Drawable d, int dimen) {
        if (d == null)
            return null;
        if(dimen <= 0){
            dimen = APP_ICON_SIZE;
        }
        Bitmap bitmap = Bitmap.createBitmap(dimen, dimen,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, dimen, dimen);
        Rect oldBound = d.copyBounds();
        d.setBounds(rect);
        d.draw(canvas);
        d.setBounds(oldBound);
        canvas.setBitmap(null);
        return bitmap;
    }

    private Bitmap drawableToBitmap(Drawable d, int dimen, boolean cut) {
        if (d == null)
            return null;

        Bitmap bitmap = Bitmap.createBitmap(dimen, dimen,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, dimen, dimen);
        Rect oldBound = d.copyBounds();
        d.setBounds(rect);
        d.draw(canvas);
        d.setBounds(oldBound);
        canvas.setBitmap(null);

        if(cut){
            int firstX = dimen - 1;
            int firstY = dimen - 1;
            int lastX = 0;
            int lastY = 0;
            for(int x = 0; x < dimen; x++){
                for(int y = 0; y < dimen; y++){
                    int pixel = bitmap.getPixel(x, y);
                    int alpha = Color.alpha(pixel);
                    if(alpha >= 0xf8){
                        firstX = firstX < x ? firstX : x;
                        firstY = firstY < y ? firstY : y;
                        lastX = lastX > x ? lastX : x;
                        lastY = lastY > y ? lastY : y;
                    }
                }
            }
            //Log.d(TAG, String.format("point (%d,%d) point(%d,%d)", firstX,firstY,lastX,lastY));
            if(Math.abs(firstX - firstY) < 3 && Math.abs(lastX - lastY) < 3 &&
                    (lastX - firstX) > dimen/2 && (lastY - firstY) > dimen/2){
                int xx = (dimen - firstX * 2)/8 + firstX;
                int yy = dimen - xx;
                if(Color.alpha(bitmap.getPixel(xx, xx)) > 0x0f || Color.alpha(bitmap.getPixel(yy, yy)) > 0x0f ){
                    Bitmap cutBitmap = Bitmap.createBitmap(bitmap, firstX, firstY, lastX - firstX, lastY - firstY);
                    return Bitmap.createScaledBitmap(cutBitmap, dimen, dimen, true);
                }
            }
        }

        return bitmap;
    }

    public Drawable addIconMask(Drawable src) {
        if (mIconMask == null) {
            initIconMask();
        }

        int iconWidth = mIconMask.getIntrinsicWidth();

        if (mIconBitmapMask == null) {
            mIconBitmapMask = drawableToBitmap(mIconMask, iconWidth);
        }

        Bitmap b = Bitmap.createBitmap(iconWidth, iconWidth,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        // Draw Mask.
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int scalepadding = 0;
        boolean isCut = false;
        if (mIconType >= TYPE_THIRD) {
            scalepadding = 8;
            isCut = true;
            c.drawBitmap(mIconBitmapMask, 0, 0, null);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        }

        Bitmap srcBitmap = drawableToBitmap(src, iconWidth - scalepadding,isCut);

        int left = (iconWidth - srcBitmap.getWidth()) / 2;
        int top = (iconWidth - srcBitmap.getHeight()) / 2;

        c.drawBitmap(srcBitmap, left, top, paint);
        c.setBitmap(null);

        Bitmap result = Bitmap.createBitmap(iconWidth, iconWidth,
                Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);

        resultCanvas.drawBitmap(b, 0, 0, null);
        resultCanvas.setBitmap(null);

        return new BitmapDrawable(mContext.getResources(), addShadow(result));
    }

    public Drawable addShadow(Drawable src) {
        // int iconWidth = Utilities.getIconWidth(mContext);
        if (mIconMask == null) {
            initIconMask();
        }

        int iconWidth = mIconMask.getIntrinsicWidth();

        if (mIconBitmapMask == null) {
            mIconBitmapMask = drawableToBitmap(mIconMask, iconWidth);
        }

        Bitmap b = Bitmap.createBitmap(iconWidth, iconWidth,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        // Draw Mask.
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap srcBitmap = drawableToBitmap(src, iconWidth);

        int left = (iconWidth - srcBitmap.getWidth()) / 2;
        int top = (iconWidth - srcBitmap.getHeight()) / 2;

        c.drawBitmap(srcBitmap, left, top, paint);
        c.setBitmap(null);

        return new BitmapDrawable(mContext.getResources(), addShadow(b));
    }

    public static Bitmap addShadow(Bitmap bitmap) {
        int shadowWidth = 0;
        int shadowHeight = 0;
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

        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);

        canvas.drawBitmap(bitmap.extractAlpha(blurPaint, null), src, dst,
                blurPaint);
        canvas.drawBitmap(bitmap, shadowWidth / 2, shadowHeight / 2, paint);

        canvas.setBitmap(null);

        return result;
    }

    private void releaseIconMask() {
        mIconMask = null;
        mIconType = TYPE_NONE;
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
            releaseIconMask();
        }
    }

    public Bitmap getIcon(Intent intent, boolean forceReload) {
        synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(
                    intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo, null,
                    forceReload);
            return entry.icon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
                          HashMap<Object, CharSequence> labelCache) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo, labelCache,
                    false);
            return entry.icon;
        }
    }

    public boolean isDefaultIcon(Bitmap icon) {
        return mDefaultIcon == icon;
    }

    private CacheEntry cacheLocked(ComponentName componentName,
                                   ResolveInfo info, HashMap<Object, CharSequence> labelCache,
                                   boolean forceReload) {

        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);

            ComponentName key = getComponentNameFromResolveInfo(info);
            if (labelCache != null && labelCache.containsKey(key)) {
                entry.title = labelCache.get(key).toString();
            } else {
                entry.title = info.loadLabel(mPackageManager).toString();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            }
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
            entry.icon = Utilities.createIconBitmap(
                    getFullResIcon(componentName, info), mContext);
        } else if (forceReload) {
            entry.icon = Utilities.createIconBitmap(
                    getFullResIcon(componentName, info), mContext);
        }
        return entry;
    }

    public HashMap<ComponentName, Bitmap> getAllIcons() {
        synchronized (mCache) {
            HashMap<ComponentName, Bitmap> set = new HashMap<ComponentName, Bitmap>();
            for (ComponentName cn : mCache.keySet()) {
                final CacheEntry e = mCache.get(cn);
                set.put(cn, e.icon);
            }
            return set;
        }
    }

    public void setEntryIcon(ComponentName componentName, Bitmap bp) {
        CacheEntry entry = mCache.get(componentName);
        if (null != entry) {
            entry.icon = bp;
        }
    }

    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }

}
