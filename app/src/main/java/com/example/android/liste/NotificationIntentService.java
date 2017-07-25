package com.example.android.liste;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.android.liste.NotificationReceiver.NOTIFICATION_ID;


public class NotificationIntentService extends IntentService {

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = DataUtils.getNotification(this);

        int id = 0;
        if (intent != null) id= intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceUtils.setAlarm(this, sharedPreferences, false, null);

    }
}
