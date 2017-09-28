package com.example.android.liste;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *  BroadcastReceiver to get notified of the Alarm triggering and to launch the IntentService
 *  that handle the response.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID_KEY = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, NotificationIntentService.class);
        int id = intent.getIntExtra(NOTIFICATION_ID_KEY, 0);
        notificationIntent.putExtra(NOTIFICATION_ID_KEY, id);
        context.startService(notificationIntent);
    }
}
