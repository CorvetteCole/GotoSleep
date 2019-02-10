package com.corvettecole.gotosleep.utilities;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class NotificationUtils {

    // We set a tag to be able to cancel all work of this type if needed
    public static final String workTag = "notificationWork";

    public void setNotification(int requestCode, long delayInMillis) {
        // Store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder().putInt(workTag, requestCode).build();
        // we then retrieve it inside the NotifyWorker with:
        // final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .addTag(workTag)
                .build();
        WorkManager.getInstance().enqueue(notificationWork);
    }

}
