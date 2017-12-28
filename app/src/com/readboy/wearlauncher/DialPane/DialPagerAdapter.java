package com.readboy.wearlauncher.DialPane;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.readboy.wearlauncher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1 on 2017/5/9.
 */

public class DialPagerAdapter extends PagerAdapter{

    private static final String TAG = "DialPagerAdapter";
    private Context mContext;
    private  List<Integer> mLayoutList;

    public DialPagerAdapter(Context context,List<Integer> list){
        mContext = context;
        mLayoutList = list;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mLayoutList == null ? 0 : mLayoutList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {

        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_watch_item, viewGroup, false);
        ViewGroup views = (ViewGroup) layout.findViewById(R.id.as_lyid);
        //layout.setOnLongClickListener(new OnMyLongClickListener(position));
//        layout.setOnClickListener(new OnMyClickListener(position));
        addChildView(views,position);
        /*layout.setOnClickListener(new OnMyClickListener(position));
        layout.setOnLongClickListener(new OnMyLongClickListener(position));
        ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
        Log.d(TAG,"position:"+position+",id:"+mList.get(position));
        imageView.setBackgroundResource(mList.get(position));*/
        viewGroup.addView(layout, 0);
        return layout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public void setClickListener(ClickListener l){
        mClickListener = l;
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    private ClickListener mClickListener;

    private void addChildView(ViewGroup viewGroup,int position){
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        mInflater.inflate(mLayoutList.get(position%mLayoutList.size()), viewGroup, true);
    }

    class OnMyLongClickListener implements View.OnLongClickListener {

        private int mPostion;
        public OnMyLongClickListener(int position){
            mPostion = position;
        }

        @Override
        public boolean onLongClick(View v) {
            if(mClickListener != null)
                mClickListener.onLongClick(v,mPostion);
            return true;
        }
    }

    class OnMyClickListener implements View.OnClickListener{

        private int mPostion;
        public OnMyClickListener(int position) {
            mPostion = position;
        }

        @Override
        public void onClick(View v) {
            if(mClickListener != null)
                mClickListener.onClick(v,mPostion);
        }
    }
}
