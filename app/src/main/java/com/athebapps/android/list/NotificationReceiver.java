package com.athebapps.android.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import static com.athebapps.android.list.NotificationJobIntentService.NOTIFICATION_JOB_ID;

/**
 *  BroadcastReceiver to get notified of the Alarm triggering and to launch the JobIntentService
 *  that handle the response.
 */
public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        JobIntentService.enqueueWork(context, NotificationJobIntentService.class, NOTIFICATION_JOB_ID, intent);
    }
}