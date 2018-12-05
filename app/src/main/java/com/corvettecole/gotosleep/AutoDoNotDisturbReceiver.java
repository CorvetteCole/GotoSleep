package com.corvettecole.gotosleep;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.NOTIFICATION_REQUEST_CODE;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.setNextDayNotification;
import static com.corvettecole.gotosleep.MainActivity.cancelNextNotification;
import static com.corvettecole.gotosleep.MainActivity.getBedtimeCal;
import static com.corvettecole.gotosleep.MainActivity.parseBedtime;
import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;

public class AutoDoNotDisturbReceiver extends BroadcastReceiver {

    private final String TAG = "AutoDoNotDisturbReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AutoDnDReceiver", "Attempting to enable DnD");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar bedtime;
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "22:00")));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mNotificationManager.isNotificationPolicyAccessGranted()) {
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
            Toast.makeText(context, context.getString(R.string.autoDnDToast), Toast.LENGTH_SHORT).show();
        }

        cancelNextNotification(context);
        setNextDayNotification(context, bedtime, TAG);

        mNotificationManager.cancel(NOTIFICATION_REQUEST_CODE);
    }
}
