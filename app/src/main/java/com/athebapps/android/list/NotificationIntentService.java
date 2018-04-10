package com.athebapps.android.list;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

import com.firebase.jobdispatcher.JobService;

import static com.athebapps.android.list.NotificationReceiver.NOTIFICATION_ID_KEY;

/** JobService used to create and send a notification to the NotificationManager and
 * update the SharedPreferences. */
public class NotificationIntentService extends JobService {

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = DatabaseUtils.createNotificationFromListProducts(this);
        Bundle bundle = job.getExtras();
        int id = 0;
        if (bundle != null)
            id = bundle.getInt(NOTIFICATION_ID_KEY);
        if (notificationManager != null)
            notificationManager.notify(id, notification);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceUtils.setAlarm(this, sharedPreferences, false, null);

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}
