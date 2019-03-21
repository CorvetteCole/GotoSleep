package com.corvettecole.gotosleep.utilities;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.corvettecole.gotosleep.BedtimeNotificationReceiver;
import com.corvettecole.gotosleep.R;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_CHANNEL_ID;
import static com.corvettecole.gotosleep.utilities.Constants.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.FIRST_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NEXT_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIFICATION_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.ONE_DAY_MILLIS;
import static java.lang.Math.abs;

public class NotificationUtilites {

    public static void cancelNextNotification(Context context){
        Intent firstNotification = new Intent(context, BedtimeNotificationReceiver.class);
        PendingIntent firstPendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, firstNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextNotification = new Intent(context, BedtimeNotificationReceiver.class);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context,
                NEXT_NOTIFICATION_ALARM_REQUEST_CODE, nextNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(firstPendingIntent);
        am.cancel(nextPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_REQUEST_CODE);
    }

    public static void setNextDayNotification(Context context, Calendar bedtime, String TAG){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(bedtime.getTimeInMillis() + ONE_DAY_MILLIS);
        Log.d(TAG, "Setting notification for tomorrow");

        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void setNotifications(boolean nextDay, boolean notificationsEnabled, int[] bedtime, int notificationDelay, int numNotifications, Context context) {
        if (notificationsEnabled) {
            Calendar bedtimeCalendar = getBedtimeCal(bedtime);


            if (nextDay){
                bedtimeCalendar.setTimeInMillis(bedtimeCalendar.getTimeInMillis() + ONE_DAY_MILLIS);
            } else if (bedtimeCalendar.getTimeInMillis() < System.currentTimeMillis()){
                bedtimeCalendar.setTimeInMillis(bedtimeCalendar.getTimeInMillis() + ONE_DAY_MILLIS);
            }

            int errorMargin = 30;
            if (PreferenceManager.getDefaultSharedPreferences(context).getInt(CURRENT_NOTIFICATION_KEY, 1) != 1){
                if (abs(System.currentTimeMillis() - bedtimeCalendar.getTimeInMillis()) > ((notificationDelay * numNotifications + errorMargin) * 60000 )){
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                }
            }

            Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtimeCalendar.getTimeInMillis(), pendingIntent);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, bedtimeCalendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(BEDTIME_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setBypassDnd(true);
            channel.enableLights(true);
            //channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
