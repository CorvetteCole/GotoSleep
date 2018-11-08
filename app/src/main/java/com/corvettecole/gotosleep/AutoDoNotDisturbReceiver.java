package com.corvettecole.gotosleep;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.NOTIFICATION_REQUEST_CODE;

public class AutoDoNotDisturbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AUTODNDREC", "Attempting to enable DnD");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
            Toast.makeText(context, "Enabled DnD... Go to sleep!", Toast.LENGTH_SHORT).show();
        }
        mNotificationManager.cancel(NOTIFICATION_REQUEST_CODE);
    }
}
