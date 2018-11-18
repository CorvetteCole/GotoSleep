package com.corvettecole.gotosleep;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.core.app.NotificationCompat;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.MainActivity.BEDTIME_CHANNEL_ID;
import static com.corvettecole.gotosleep.MainActivity.getBedtimeCal;
import static com.corvettecole.gotosleep.MainActivity.notifications;
import static com.corvettecole.gotosleep.MainActivity.parseBedtime;
import static com.corvettecole.gotosleep.SettingsFragment.ADS_ENABLED_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.DND_DELAY_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.DND_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.INACTIVITY_TIMER_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_AMOUNT_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_DELAY_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.ADVANCED_PURCHASED_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.SMART_NOTIFICATIONS_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.isUsageAccessGranted;

public class BedtimeNotificationReceiver extends BroadcastReceiver {

    static final int FIRST_NOTIFICATION_ALARM_REQUEST_CODE = 1;
    static final int NOTIFICATION_REQUEST_CODE = 2;
    static final int DO_NOT_DISTURB_REQUEST_CODE = 3;
    static final int DO_NOT_DISTURB_ALARM_REQUEST_CODE = 4;
    static final int NEXT_NOTIFICATION_ALARM_REQUEST_CODE = 5;
    static final int LAUNCH_APP_REQUEST_CODE = 6;
    static final String LAST_NOTIFICATION_KEY = "lastNotificationTime";
    static final long ONE_MINUTE_MILLIS = 60000;
    static final long ONE_DAY_MILLIS = 86400000;
    static final long ONE_HOUR_MILLIS = 3600000;
    private int DnD_delay = 2; //in minutes

    private Calendar bedtime;
    private int numNotifications;
    private int notificationDelay;
    private int userActiveMargin;
    private boolean adsEnabled;
    private boolean advancedOptionsPurchased;
    private boolean smartNotifications;
    private boolean autoDND;
    private final String TAG = "bedtimeNotifReceiver";
    private int currentNotification;
    static final String CURRENT_NOTIFICATION_KEY = "current_notification";

    private boolean shouldEnableAdvancedOptions = false;
    private long lastNotification;
    private UsageStatsManager usageStatsManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        bedtime = Calendar.getInstance();
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "19:35")));
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3 + ""));
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15 + ""));
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false);
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false);
        autoDND = settings.getBoolean(DND_KEY, false);
        smartNotifications = settings.getBoolean(SMART_NOTIFICATIONS_KEY, false);
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1);
        lastNotification = settings.getLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis());
        userActiveMargin = Integer.parseInt(settings.getString(INACTIVITY_TIMER_KEY, "5"));
        DnD_delay = Integer.parseInt(settings.getString(DND_DELAY_KEY, "2"));

        shouldEnableAdvancedOptions = adsEnabled || advancedOptionsPurchased;


        if (shouldEnableAdvancedOptions) {
            for (int i = 0; i < notifications.length; i++) {
                notifications[i] = settings.getString("pref_notification" + (i + 1), "");
            }
            for (String notification : notifications) {
                Log.d(TAG, notification);
            }
        } else {
            notifications[0] = context.getResources().getString(R.string.notification1);
            notifications[1] = context.getResources().getString(R.string.notification2);
            notifications[2] = context.getResources().getString(R.string.notification3);
            notifications[3] = context.getResources().getString(R.string.notification4);
            notifications[4] = context.getResources().getString(R.string.notification5);
        }

        if (currentNotification == 1){
            lastNotification = System.currentTimeMillis();
        }

        showNotification(context, getNotificationTitle(), getNotificationContent());

        if (!smartNotifications || !isUsageAccessGranted(context) || !shouldEnableAdvancedOptions) {
            if (currentNotification < numNotifications) {
                setNextNotification(context);
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
            } else if (currentNotification == numNotifications) {
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                setNextDayNotification(context);
                enableDoNotDisturb(context);
            }

        } else if (isUserActive(UsageStatsManager.INTERVAL_BEST, lastNotification, System.currentTimeMillis()) && (System.currentTimeMillis() - bedtime.getTimeInMillis() < 6 * ONE_HOUR_MILLIS)) {
            //write lastNotification to preferences, set a timer for notifDelay time in the future, show notification now, update currentNotification
            settings.edit().putLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis()).apply();
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
            setNextNotification(context);

        } else {
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
            enableDoNotDisturb(context);
        }
    }

    private boolean isUserActive(int interval, long startTime, long currentTime){
        if (currentNotification == 1){
            startTime = startTime - notificationDelay * ONE_MINUTE_MILLIS;
        }

        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(interval, startTime, currentTime);

        UsageStats minUsageStat = queryUsageStats.get(0);

        long min = Long.MAX_VALUE;
        for (UsageStats usageStat : queryUsageStats){
            if (usageStat.getLastTimeUsed() < min && usageStat.getTotalTimeInForeground() > ONE_MINUTE_MILLIS){
                minUsageStat = usageStat;
            }
        }

        return System.currentTimeMillis() - minUsageStat.getLastTimeUsed() <= userActiveMargin * ONE_MINUTE_MILLIS;
    }

    private void enableDoNotDisturb(Context context){
        if (autoDND) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + (ONE_MINUTE_MILLIS * DnD_delay));
            Log.d(TAG, "Setting auto DND for 2 minutes from now: " + calendar.getTime());

            Intent intent1 = new Intent(context, AutoDoNotDisturbReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    DO_NOT_DISTURB_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, LAUNCH_APP_REQUEST_CODE, intent, 0);
        Intent snoozeIntent = new Intent(context, AutoDoNotDisturbReceiver.class);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, DO_NOT_DISTURB_REQUEST_CODE, snoozeIntent, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if the notification policy access has been granted for the app.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, BEDTIME_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_moon_notification)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.moonPrimary));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted()){
                mBuilder.addAction(R.drawable.ic_do_not_disturb_on_white_24dp, "I'm going to sleep", snoozePendingIntent);
            }
        }

        notificationManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build());

    }

    private String getNotificationContent() {
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
        int min = Math.round(difference/60000); //divide time in milliseconds by 60 000 to get minutes


        Log.d(TAG, "currentNotification: " + currentNotification);

            if (currentNotification == 1) {
                return "Time to head to bed.";
            } else if (min == 1){
                return "It is " + min + " minute past your bedtime!";
            } else {
                return "It is " + min + " minutes past your bedtime!";
            }
    }

    private String getNotificationTitle(){
        int notificationTitleIndex = currentNotification - 1;
        if (notificationTitleIndex > notifications.length){
            while (notificationTitleIndex > notifications.length){
                notificationTitleIndex = notificationTitleIndex - 5;

            }
        }
        return notifications[notificationTitleIndex];
    }

    private void setNextNotification(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + notificationDelay * 60000);
        Log.d(TAG, "Setting next notification in " + notificationDelay + " minutes");

        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                NEXT_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void setNextDayNotification(Context context){
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


}
