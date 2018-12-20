package com.readboy.wearlauncher.view;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.application.AppInfo;
import com.readboy.wearlauncher.utils.Utils;
import com.readboy.wearlauncher.utils.WatchController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 1 on 2017/5/6.
 */

public class WatchAppGridView extends RelativeLayout implements WatchController.ClassDisableChangedCallback {

    Context mContext;
    private LauncherApplication mApplication;
    GridView mGridView;
    GridAdapter mGridAdapter;
    ImageView mImageView;
    private LayoutInflater mInflater;
    WatchController mWatchController;

    List<AppInfo> mAppList = new ArrayList<AppInfo>();

    public WatchAppGridView(Context context) {
        this(context,null);
    }

    public WatchAppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mApplication = (LauncherApplication) context.getApplicationContext();
        mInflater = LayoutInflater.from(context);

        mWatchController = mApplication.getWatchController();
    }

    public void moveToTop(){
        if(mGridView != null){
            mGridView.smoothScrollToPositionFromTop(0,0);
        }
    }

    public void refreshData(ArrayList<AppInfo> data){
        mAppList.clear();
        mAppList = data;
        mGridAdapter.notifyDataSetChanged();
    }

    public AppInfo getAppInfo(int position){
        return mGridView != null ? mAppList.get(position) : null;
    }

    public void setClassDisableShow(boolean show){
        if(show){
            mImageView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.INVISIBLE);
        }else {
            mImageView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.imageView);
        mGridView = (GridView) findViewById(R.id.grid_vid);
        mGridAdapter = new GridAdapter();
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mOnClickItemListener != null){
                    mOnClickItemListener.onClick(position);
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatchController.addClassDisableChangedCallback(this);

        mGridView.requestFocus();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWatchController.removeClassDisableChangedCallback(this);
    }

    private OnClickItemListener mOnClickItemListener;

    public void setOnClickItemListener(OnClickItemListener listener){
        mOnClickItemListener = listener;
    }

    @Override
    public void onClassDisableChange(boolean show) {
        setClassDisableShow(show);
    }

    public interface OnClickItemListener {
        void onClick(int position);

    }

    class GridAdapter extends BaseAdapter {

        IconCache mTconCache;
        public GridAdapter() {
            mTconCache = ((LauncherApplication)LauncherApplication.getApplication()).getIconCache();
        }

        @Override
        public int getCount() {
            return mAppList == null ? 0 : mAppList.size();
        }

        @Override
        public AppInfo getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewTag viewTag;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.view_app_grid_item, null);
                viewTag = new ItemViewTag();
//                viewTag.mName = (TextView) convertView.findViewById(R.id.app_name_tvid);
//                viewTag.mIcon = (IconImageView) convertView.findViewById(R.id.app_icon_ivid);
                viewTag.mIconTextView = (IconTextView) convertView.findViewById(R.id.app_icon_tvid);
                convertView.setTag(viewTag);
            }
            viewTag = (ItemViewTag) convertView.getTag();

            final AppInfo appInfo = mAppList.get(position);
//            viewTag.mName.setText(appInfo.mAppName);
//            viewTag.mIcon.applyFromShortcutInfo(appInfo,mTconCache);
            viewTag.mIconTextView.applyFromShortcutInfo(appInfo,mTconCache);

            return convertView;
        }

        class ItemViewTag
        {
            protected IconTextView mIconTextView;
            protected IconImageView mIcon;
            protected TextView mName;

            public ItemViewTag(){}
        }
    }
}
