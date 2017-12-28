package com.readboy.wearlauncher.view;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readboy.wearlauncher.notification.DateTimeView;
import com.readboy.wearlauncher.notification.NotificationMonitor;
import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.Swipe.SwipeLayout;

/**
 * Created by Administrator on 2017/6/17.
 */

public class NotificationItem extends RelativeLayout{
    private static final String TAG = "NotificationItem";

    StatusBarNotification mStatusBarNotification;

    public TextView mTitleTextView;
    public DateTimeView mTimeView;
    public TextView mContentTextView;
    SwipeLayout mSwipeLayout;

    public NotificationItem(Context context) {
        super(context);
    }

    public NotificationItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStatusBarNotification(StatusBarNotification statusBarNotification){
        mStatusBarNotification = statusBarNotification;
        if(mStatusBarNotification != null){
            Notification notification = mStatusBarNotification.getNotification();
            int flags = notification.flags;
            if((flags & Notification.FLAG_AUTO_CANCEL) != 0){
                mSwipeLayout.setSwipeEnabled(true);
            }else {
                mSwipeLayout.setSwipeEnabled(false);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwipeLayout = (SwipeLayout) findViewById(R.id.swipelayout);
        mSwipeLayout.findViewById(R.id.delete).setOnClickListener(new NotificationDelete());
        mSwipeLayout.getSurfaceView().setOnClickListener(new NotificationClicker());
        mTitleTextView = (TextView) findViewById(R.id.title);
        mTimeView = (DateTimeView) findViewById(R.id.time);
        mContentTextView = (TextView) findViewById(R.id.content);
    }

    private final class NotificationClicker implements View.OnClickListener {

        public void onClick(final View v) {

            final StatusBarNotification sbn = mStatusBarNotification;
            if (sbn == null) {
                Log.e(TAG, "NotificationClicker called on an unclickable notification,");
                return;
            }

            final PendingIntent intent = sbn.getNotification().contentIntent;
            final String notificationKey = sbn.getKey();

            Log.d(TAG, "Clicked on content of " + notificationKey);

            if (intent != null) {
                try {
                    intent.send();
                } catch (PendingIntent.CanceledException e) {
                    // the stack trace isn't very helpful here.
                    // Just log the exception message.
                    Log.w(TAG, "Sending contentIntent failed: " + e);
                }
            }
        }
    }

    private final class NotificationDelete implements View.OnClickListener {


        public void onClick(final View v) {

            final StatusBarNotification sbn = mStatusBarNotification;
            if (sbn == null) {
                Log.e(TAG, "NotificationClicker called on an unclickable notification,");
                return;
            }
            Intent i = new Intent(NotificationMonitor.ACTION_NLS_CONTROL);
            i.putExtra("command","cancel");
            i.putExtra("key",sbn.getKey());
            NotificationItem.this.getContext().sendBroadcast(i);
            //NotificationMonitor.getNotificationMonitor().cancelNotification(sbn.getKey());
        }
    }
}
