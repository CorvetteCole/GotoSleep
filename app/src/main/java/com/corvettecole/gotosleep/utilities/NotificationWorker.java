package com.corvettecole.gotosleep.utilities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.corvettecole.gotosleep.AutoDoNotDisturbReceiver;
import com.corvettecole.gotosleep.MainActivity;
import com.corvettecole.gotosleep.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.SettingsFragment.isUsageAccessGranted;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.parseBedtime;
import static com.corvettecole.gotosleep.utilities.Constants.ADS_ENABLED_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.ADVANCED_PURCHASED_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_CHANNEL_ID;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.DND_DELAY_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.DND_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.DO_NOT_DISTURB_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.DO_NOT_DISTURB_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.INACTIVITY_TIMER_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.LAST_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.LAUNCH_APP_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NEXT_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIFICATION_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIFICATION_SOUND_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_AMOUNT_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_DELAY_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.ONE_HOUR_MILLIS;
import static com.corvettecole.gotosleep.utilities.Constants.ONE_MINUTE_MILLIS;
import static com.corvettecole.gotosleep.utilities.Constants.SEND_ONE_NOTIFICATION;
import static com.corvettecole.gotosleep.utilities.Constants.SMART_NOTIFICATIONS_KEY;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.cancelNextNotification;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.setNextDayNotification;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.setNotification;

public class NotificationWorker extends Worker {

    private int DnD_delay = 2; //in minutes

    Context context;

    private Calendar bedtime;
    private int numNotifications;
    private int notificationDelay;
    private int userActiveMargin;
    private boolean adsEnabled;
    private boolean advancedOptionsPurchased;
    private boolean smartNotifications;
    private boolean autoDND;
    private boolean userActive = true;
    private final String TAG = "bedtimeNotifReceiver";
    private int currentNotification;

    private boolean shouldEnableAdvancedOptions = false;
    private boolean notificationSoundsEnabled = false;
    private boolean sendOneNotification = false;
    private long lastNotification;
    private UsageStatsManager usageStatsManager;
    private String[] notifications = new String[5];

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Worker.Result doWork() {

        triggerNotification();

        return Worker.Result.success();
        // (Returning retry() tells WorkManager to try this task again
        // later; failure() says not to try again.)
    }

    private void triggerNotification() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        bedtime = Calendar.getInstance();
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "22:00")));
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
        notificationSoundsEnabled = settings.getBoolean(NOTIFICATION_SOUND_KEY, false);
        sendOneNotification = settings.getBoolean(SEND_ONE_NOTIFICATION, false);

        shouldEnableAdvancedOptions = adsEnabled || advancedOptionsPurchased;

        createNotificationChannel(context);

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

        if (isUsageAccessGranted(context) && smartNotifications && shouldEnableAdvancedOptions) { //if any of these are not met, code will fall back to normal notifications
            //smart notification code block
            if (isUserActive(lastNotification, System.currentTimeMillis()) && (System.currentTimeMillis() - bedtime.getTimeInMillis() < 6 * ONE_HOUR_MILLIS)) {
                showNotification(context, getNotificationTitle(), getNotificationContent(getApplicationContext()));
                settings.edit().putLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis()).apply();
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
                setNextNotification(context);
            } else if (sendOneNotification && currentNotification == 1){
                showNotification(context, getNotificationTitle(), getNotificationContent(getApplicationContext()));
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
            } else {
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                if (autoDND) {
                    enableDoNotDisturb(context);
                }
                cancelNextNotification(context);
                setNextDayNotification(context, bedtime, TAG);
            }
        } else {
            //normal notification code block
            showNotification(context, getNotificationTitle(), getNotificationContent(getApplicationContext()));
            if (currentNotification < numNotifications) {
                setNextNotification(context);
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
            } else if (currentNotification == numNotifications) {
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                if (autoDND) {
                    enableDoNotDisturb(context);
                }
                cancelNextNotification(context);
                setNextDayNotification(context, bedtime, TAG);
            }
        }
    }

    private boolean isUserActive(long startTime, long currentTime){
        String TAG = "isUserActive";
        if (currentNotification == 1){
            startTime = startTime - notificationDelay * ONE_MINUTE_MILLIS;
        }

        //#TODO experiment with using a daily interval (make sure it works past midnight)
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startTime, currentTime);

        UsageStats minUsageStat = null;

        long min = Long.MAX_VALUE;
        for (UsageStats usageStat : queryUsageStats){
            if ((System.currentTimeMillis() - usageStat.getLastTimeStamp() < min) && (usageStat.getTotalTimeInForeground() > ONE_MINUTE_MILLIS) && !usageStat.getPackageName().equals("com.corvettecole.gotosleep")){  //make sure app has been in foreground for more than one minute to filter out background apps
                minUsageStat = usageStat;
                min = System.currentTimeMillis() - usageStat.getLastTimeStamp();
            }
        }

        if (minUsageStat != null) {
            Log.d(TAG, minUsageStat.getPackageName() + " last time used: " + minUsageStat.getLastTimeUsed() + " time in foreground: " + minUsageStat.getTotalTimeInForeground());
            Log.d(TAG, "getLastTimeStamp: " + minUsageStat.getLastTimeStamp() + " getLastUsed: " + minUsageStat.getLastTimeUsed() + " current time: " + System.currentTimeMillis());
            Log.d(TAG, (System.currentTimeMillis() - minUsageStat.getLastTimeUsed() <= userActiveMargin * ONE_MINUTE_MILLIS) + "");
            return System.currentTimeMillis() - minUsageStat.getLastTimeStamp() <= userActiveMargin * ONE_MINUTE_MILLIS;
        } else {
            Log.e(TAG, "minUsageStat was null!");
            Log.e(TAG, queryUsageStats.toString());
            return false;
        }
    }

    private void enableDoNotDisturb(Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + (ONE_MINUTE_MILLIS * DnD_delay));
        Log.d(TAG, "Setting auto DND for " + DnD_delay + " minutes from now: " + calendar.getTime());

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
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.moonPrimary));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (notificationSoundsEnabled) {  //if device does not support notification channels check if notification sound is enabled
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
            } else {
                mBuilder.setSound(null);
            }
        }

        mBuilder.addAction(R.drawable.ic_do_not_disturb, context.getString(R.string.notifAction), snoozePendingIntent);

        notificationManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build());

    }

    private void setNextNotification(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + notificationDelay * 60000);
        Log.d(TAG, "Setting next notification in " + notificationDelay + " minutes");
        setNotification(NEXT_NOTIFICATION_ALARM_REQUEST_CODE, calendar.getTimeInMillis());
    }

    private String getNotificationContent(Context context) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Date endDate;
        Date startDate;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

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
        if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
            if (currentNotification == 1) {
                return context.getString(R.string.notifTitleFirst);
            } else if (min == 1) {
                return String.format(context.getString(R.string.notifTitleSingular), min);
            } else if(min >= 2 && min <= 4){
                return String.format(context.getString(R.string.notifTitleFunky), min);
            } else {
                return String.format(context.getString(R.string.notifTitlePlural), min);
            }
        } else {
            if (currentNotification == 1) {
                return context.getString(R.string.notifTitleFirst);
            } else if (min == 1) {
                return String.format(context.getString(R.string.notifTitleSingular), min);
            } else {
                return String.format(context.getString(R.string.notifTitlePlural), min);
            }
        }
    }

    private String getNotificationTitle(){
        int notificationTitleIndex = currentNotification - 1;
        if (notificationTitleIndex > notifications.length){
            while (notificationTitleIndex > notifications.length){
                notificationTitleIndex = notificationTitleIndex - 5;

            }
        }
        try {
            return notifications[notificationTitleIndex];
        } catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "getNotificationTitle: ",e);
            return  notifications[0];
        }
    }

}
