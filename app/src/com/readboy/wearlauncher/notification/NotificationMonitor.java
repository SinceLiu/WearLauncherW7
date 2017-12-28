
package com.readboy.wearlauncher.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

public class NotificationMonitor extends NotificationListenerService {
    private static final String TAG = "NotificationMonitor";
    private static final String TAG_PRE = "[NotificationMonitor] ";

    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_NOTIFICATION = "notification";
    public static final String COMMAND_REMOVED = "removed";
    public static final String COMMAND_POSTED = "posted";

    public static final String ACTION_NLS_CONTROL = "com.readboy.notificationlistener.NLSCONTROL";
    public static final String ACTION_NLS_UPDATE = "com.readboy.notificationlistener.UPDATE";
    private NotificationMonitorReceiver mReceiver = new NotificationMonitorReceiver();

    public static NotificationMonitor INSTANCE;

    public static NotificationMonitor getNotificationMonitor() {
        return INSTANCE;
    }

    class NotificationMonitorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && intent.getAction() != null) {
                action = intent.getAction();
                if (action.equals(ACTION_NLS_CONTROL)) {
                    String command = intent.getStringExtra("command");

                    if (TextUtils.equals(command, "cancel") && !TextUtils.isEmpty(intent.getStringExtra("key"))) {
                        String key = intent.getStringExtra("key");
                        Log.e(TAG, "onReceive: cancel notification key = " + key);
                        cancelNotification(key);
                    } else if (TextUtils.equals(command, "clearall")) {
                        NotificationMonitor.this.cancelAllNotifications();
                    } else if (TextUtils.equals(command, "list")) {
//                        for (StatusBarNotification sbn : NotificationMonitor.this.getActiveNotifications()) {
//                            Intent intent1 = new  Intent(ACTION_NLS_UPDATE);
//                            intent1.putExtra("command","list");
//                            intent1.putExtra("sbn",sbn);
//                            sendBroadcast(intent1);
//                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        logNLS("onCreate...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NLS_CONTROL);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logNLS("onBind...");
        return super.onBind(intent);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG, "onListenerConnected...");
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
        Log.e(TAG, "onNotificationRankingUpdate...");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        logNLS("onNotificationPosted: key = " + sbn.getKey());
        Intent intent1 = new Intent(ACTION_NLS_UPDATE);
        intent1.putExtra(EXTRA_COMMAND, COMMAND_POSTED);
        intent1.putExtra(EXTRA_NOTIFICATION, sbn);
        sendBroadcast(intent1);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
        Log.e(TAG, "onNotificationPosted: key = " + sbn.getKey());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        logNLS("onNotificationRemoved: key = " + sbn.getKey());
        Intent intent1 = new Intent(ACTION_NLS_UPDATE);
        intent1.putExtra(EXTRA_COMMAND, COMMAND_REMOVED);
        intent1.putExtra(EXTRA_NOTIFICATION, sbn);
        sendBroadcast(intent1);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);
        Log.e(TAG, "onNotificationRemoved: key = " + sbn.getKey());
    }

    private static void logNLS(Object object) {
        Log.e(TAG, TAG_PRE + object.toString());
    }

    public static void cancelNotificationByKey(String key) {
        INSTANCE.cancelNotification(key);
    }
}
