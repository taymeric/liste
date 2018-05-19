package com.athebapps.android.list;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v7.preference.PreferenceManager;

import com.athebapps.android.list.utils.DatabaseUtils;
import com.athebapps.android.list.utils.PreferenceUtils;

import static com.athebapps.android.list.ListActivity.DIRECT_NOTIFICATION_ID;
import static com.athebapps.android.list.ListActivity.REMINDER_NOTIFICATION_ID;


/** JobIntentService used to create and send a notification to the NotificationManager and
 * update the SharedPreferences if the source of this notification is a reminder (as
 * opposed to a direct notification). */
public class NotificationJobIntentService extends JobIntentService {

    public static final int NOTIFICATION_JOB_ID = 1212;
    public static final String NOTIFICATION_ID_KEY = "notification_id";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        Notification notification = DatabaseUtils.createNotificationFromListProducts(this);

        int id = intent.getIntExtra(NOTIFICATION_ID_KEY, DIRECT_NOTIFICATION_ID);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(NOTIFICATION_JOB_ID, notification);

        // In the case the notification comes from a reminder, we update SharedPreferences which
        // the main activity monitors in order to update the ui.
        if (id == REMINDER_NOTIFICATION_ID) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            PreferenceUtils.setAlarm(this, sharedPreferences, false, null);
        }
    }
}
