package com.athebapps.android.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;

/**
 *  BroadcastReceiver to get notified of the Alarm triggering and to launch the JobIntentService
 *  that handle the response.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ID_KEY = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Because we can't launch a Service while the app is in the background on Android O and above,
        // we schedule a Job instead and use FirebaseJobDispatcher for compatibility.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        int id = intent.getIntExtra(NOTIFICATION_ID_KEY, 0);
        Bundle myExtras = new Bundle();
        myExtras.putInt(NOTIFICATION_ID_KEY, id);

        Job notificationJob = dispatcher.newJobBuilder()
                .setService(NotificationIntentService.class) // the JobService that will be called
                .setExtras(myExtras)
                .setTag("notification-job") // uniquely identifies the job
                .build();

        dispatcher.schedule(notificationJob);
    }
}
