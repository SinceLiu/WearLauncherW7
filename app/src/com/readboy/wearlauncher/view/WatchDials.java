package com.readboy.wearlauncher.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.antonyt.infiniteviewpager.GalleryTransformer;
import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.readboy.wearlauncher.DialPane.DialCircleIndicator;
import com.readboy.wearlauncher.DialPane.DialPagerAdapter;
import com.readboy.wearlauncher.LauncherApplication;
import com.readboy.wearlauncher.LauncherSharedPrefs;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/13.
 */

public class WatchDials extends FrameLayout {

    public static final ArrayList<Integer> mDialList = new ArrayList<Integer>(){{
        add(R.layout.dialtype_g_layout_cell);
        add(R.layout.dialtype_b_layout_cell);
        add(R.layout.dialtype_k_layout_cell);
        add(R.layout.dialtype_n_layout_cell);
        add(R.layout.dialtype_j_layout_cell);
        add(R.layout.dialtype_c_layout_cell);
        add(R.layout.dialtype_e_layout_cell);
        add(R.layout.dialtype_m_layout_cell);
        //add(R.layout.dialtype_o_layout_cell);//低电模式
        //add(R.layout.dialtype_h_layout_cell);
        //add(R.layout.dialtype_i_layout_cell);
        //add(R.layout.dialtype_a_layout_cell);
        //add(R.layout.dialtype_d_layout_cell);//低电模式
        //add(R.layout.dialtype_f_layout_cell);
        //add(R.layout.dialtype_l_layout_cell);

    }};
    public static final int ANIMATE_STATE_IDLE = 0;
    public static final int ANIMATE_STATE_OPENING = 1;
    public static final int ANIMATE_STATE_OPENED = 2;
    public static final int ANIMATE_STATE_CLOSING = 3;
    public static final int ANIMATE_STATE_CLOSED = 4;
    public static int mWatchDialsStatus = ANIMATE_STATE_IDLE;
    private Context mContext;

    private ObjectAnimator mOpenCloseAnimator;

    private DialCircleIndicator mDialCircleIndicator;
    private ViewPager mViewPager;

    private int mExpandDuration = 300;
    private int mLastDialIndex;

    public WatchDials(Context context) {
        this(context,null);
    }

    public WatchDials(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WatchDials(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mLastDialIndex = LauncherSharedPrefs.getWatchType(mContext);
    }

    public static WatchDials fromXml(Context context) {
        return (WatchDials) LayoutInflater.from(context).inflate(R.layout.watch_dials, null);
    }

    public static int getWatchDialsStatus(){
        return mWatchDialsStatus;
    }

    public void animateOpen(){
        mWatchDialsStatus = ANIMATE_STATE_OPENING;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.46f,1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",1.46f,1.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.25f,1.0f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this,  scaleX, scaleY, alpha);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                setLayerType(LAYER_TYPE_NONE, null);

                setScaleX(1);
                setScaleY(1);
                setAlpha(1);
                mWatchDialsStatus = ANIMATE_STATE_OPENED;
            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        oa.start();
    }

    public void animateClose(boolean saveMode){
        if(saveMode){
            setChooseModeType();
        }
        mWatchDialsStatus = ANIMATE_STATE_CLOSING;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.46f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.46f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f,0.25f);
        final ObjectAnimator oa = mOpenCloseAnimator =
                LauncherAnimUtils.ofPropertyValuesHolder(this,  scaleX, scaleY, alpha);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup parent = (ViewGroup) WatchDials.this.getParent();
                if (parent != null) {
                    parent.removeView(WatchDials.this);
                }
                setLayerType(LAYER_TYPE_NONE, null);
                mWatchDialsStatus = ANIMATE_STATE_CLOSED;
                System.gc();
            }
            @Override
            public void onAnimationStart(Animator animation) {

            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        oa.start();
    }

    private void setChooseModeType(){
        int type = mViewPager.getCurrentItem();
        LauncherSharedPrefs.setWatchtype(mContext,type);
    }

    private void recycle(){
        if(mViewPager != null){
            InfinitePagerAdapter adapter = (InfinitePagerAdapter)mViewPager.getAdapter();
            int count = adapter.getRealCount();
            Log.d("test","mViewPager count:"+count);
            for(int i = 0; i < count; i++){

            }
        }
    }

    private void recycleImageView(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private void recycleViewGroup(ViewGroup layout){
        if(layout==null) return;
        Drawable drawable = layout.getBackground();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        synchronized(layout){
            for (int i = 0; i < layout.getChildCount(); i++) {
                View subView = layout.getChildAt(i);
                if (subView instanceof ViewGroup) {
                    recycleViewGroup((ViewGroup) subView);
                } else {
                    if (subView instanceof ImageView) {
                        recycleImageView((ImageView)subView);
                    }
                }
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewPager = (ViewPager) findViewById(R.id.watch_type_vpid);
        DialPagerAdapter dialPagerAdapter = new DialPagerAdapter(mContext,mDialList);
        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(dialPagerAdapter);
        mViewPager.setAdapter(wrappedAdapter);
        mViewPager.setCurrentItem(mLastDialIndex);
        mViewPager.setPageTransformer(true, new GalleryTransformer());
        mViewPager.setPageMargin(-Utils.px2dip(mContext,50));
        mViewPager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
