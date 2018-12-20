package com.readboy.wearlauncher.application;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.readboy.wearlauncher.view.IconCache;

public class AppInfo {
	public ResolveInfo mResolveInfo;
	public Bitmap mIcon;
	public Drawable mIconDrawable;
	public StateListDrawable mStateListDrawable;
	public String mAppName;
	public String mClassName;
	public String mPackageName;

	public AppInfo(ResolveInfo info,Drawable iconDrawable,String appName, String packageName,String className){
		mResolveInfo = info;
		mIconDrawable = iconDrawable;
		mAppName = appName;
		mClassName = className;
		mPackageName = packageName;

//		mStateListDrawable = new StateListDrawable();
//		mStateListDrawable.addState(new int[] { android.R.attr.state_pressed }, Utils.addShadow(context,mIconDrawable));
//		mStateListDrawable.addState(new int[] {},mIconDrawable);
	}

	public Bitmap getIcon(IconCache iconCache) {
		if (mIcon == null) {
			updateIcon(iconCache);
		}
		return mIcon;
	}

	public void updateIcon(IconCache iconCache) {
		Drawable drawable = iconCache.getFullResIcon(mResolveInfo);
		mIcon = iconCache.drawableToBitmap(drawable,0);
	}

	@Override
	public String toString() {
		return "{AppName="+mAppName+" PackageName="+mPackageName+" ClassName="+mClassName+"}";
	}
}
