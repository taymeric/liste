package com.example.android.liste;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;


public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION = "notification";
    public static final String NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        // Modification : Créer la notification ici pour avoir une liste à jour.
        // Utiliser un Service ou IntentService pour éviter de faire trop de travail ici

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceUtils.setAlarm(context, sharedPreferences, false, null);
    }
}
