package com.corvettecole.gotosleep;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.core.app.NotificationCompat;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.MainActivity.BEDTIME_CHANNEL_ID;
import static com.corvettecole.gotosleep.MainActivity.getBedtimeCal;
import static com.corvettecole.gotosleep.MainActivity.notifications;
import static com.corvettecole.gotosleep.MainActivity.parseBedtime;
import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_AMOUNT_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_DELAY_KEY;

public class BedtimeNotificationReceiver extends BroadcastReceiver {

    final int reqCode = 8;
    private Calendar bedtime;
    private int numNotifications;
    private int notificationDelay;
    final String TAG = "bedtimeNotifReceiver";
    private int currentNotification;
    static final int ONE_DAY_MILLIS = 86400000;

    //#TODO maybe instead of calculating time differences to figure out what number a notification is, set flags in sharedprefs so they are independent of when they are triggered by the android OS
    static final String CURRENT_NOTIFICATION_KEY = "current_notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        bedtime = Calendar.getInstance();
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "19:35")));
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3 + ""));
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15 + ""));

        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1);

        //#TODO if custom notifications are not enabled (ads not enabled and in app purchase not purchased), use default notifications
        for (int i = 0; i < notifications.length; i++) {
            notifications[i] = settings.getString("pref_notification" + (i + 1), "");
        }
        for (String notification : notifications) {
            Log.d(TAG, notification);
        }



        String[] notificationContent = getNotificationContent();
        //check for more info
        showNotification(context, notificationContent[0], notificationContent[1]);

        if (currentNotification < numNotifications) {
            setNextNotification(context, 12);
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
        } else if (currentNotification == numNotifications){
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
            setNextDayNotification(context, 1);
        }
    }

    public void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, BEDTIME_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_moon)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.moonPrimary));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(reqCode, mBuilder.build());

    }

    public String[] getNotificationContent() {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Date endDate;
        Date startDate;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        Log.d(TAG, current.get(Calendar.SECOND) + "");
        current.set(Calendar.SECOND, 0);

        startDate = bedtime.getTime();
        endDate = current.getTime();

        Log.d(TAG, bedtime.getTime() + " bedtime");

        Log.d(TAG, current.getTime() + " current time");


        long difference = endDate.getTime() - startDate.getTime();
        if (difference < 0) {
            try {
                Date dateMax = simpleDateFormat.parse("24:00");
                Date dateMin = simpleDateFormat.parse("00:00");
                difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
            } catch (ParseException e) {
                Log.e(TAG, e + "");
            }
        }
        int day = (int) (difference / (1000 * 60 * 60 * 24));
        int hour = (int) ((difference - (1000 * 60 * 60 * 24 * day)) / (1000 * 60 * 60));
        Log.d(TAG, (difference - (1000 * 60 * 60 * 24 * day) - (1000 * 60 * 60 * hour)) / (1000 * 60) + " min");
        int min = (int) (difference - (1000 * 60 * 60 * 24 * day) - (1000 * 60 * 60 * hour)) / (1000 * 60);
        Log.i(TAG, "Days: " + day + " Hours: " + hour + ", Mins: " + min);

        int totalMin = (hour * 60) + min;


        Log.d(TAG, "currentNotification: " + currentNotification);


            if (currentNotification == 1) {
                return new String[]{notifications[currentNotification - 1], "Time to head to bed."};
            } else {
                return new String[]{notifications[currentNotification - 1], "It is " + totalMin + " past your bedtime!"};
            }
    }

    public void setNextNotification(Context context, int REQUEST_CODE_BEDTIME) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + notificationDelay * 60000);
        Log.d(TAG, "Setting next notification in " + notificationDelay + " minutes");

        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE_BEDTIME, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public void setNextDayNotification(Context context, int REQUEST_CODE_BEDTIME){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(bedtime.getTimeInMillis() + ONE_DAY_MILLIS);
        Log.d(TAG, "Setting notification for tomorrow");

        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE_BEDTIME, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }


}
