package com.example.android.liste;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, NotificationIntentService.class);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationIntent.putExtra(NOTIFICATION_ID, id);
        context.startService(notificationIntent);
    }
}
