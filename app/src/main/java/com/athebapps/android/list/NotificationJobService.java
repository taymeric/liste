package com.athebapps.android.list;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

import com.firebase.jobdispatcher.JobService;


/** JobService used to create and send a notification to the NotificationManager and
 * update the SharedPreferences if the source of this notification is a reminder (as
 * opposed to a direct notification). */
public class NotificationJobService extends JobService {

    public static final String NOTIFICATION_ID_KEY = "notification_id";
    public static final String NOTIFICATION_SOURCE_IS_REMINDER = "notification_source_reminder";

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = DatabaseUtils.createNotificationFromListProducts(this);
        Bundle bundle = job.getExtras();
        int id = 0;
        boolean reminder = false;
        if (bundle != null) {
            id = bundle.getInt(NOTIFICATION_ID_KEY);
            reminder = bundle.getBoolean(NOTIFICATION_SOURCE_IS_REMINDER);
        }
        if (notificationManager != null)
            notificationManager.notify(id, notification);

        if (reminder) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            PreferenceUtils.setAlarm(this, sharedPreferences, false, null);
        }

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}
