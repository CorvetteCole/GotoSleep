package com.corvettecole.gotosleep

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import androidx.core.app.NotificationCompat

import android.content.Context.ALARM_SERVICE
import com.corvettecole.gotosleep.MainActivity.Companion.BEDTIME_CHANNEL_ID
import com.corvettecole.gotosleep.MainActivity.Companion.getBedtimeCal
import com.corvettecole.gotosleep.MainActivity.Companion.notifications
import com.corvettecole.gotosleep.MainActivity.Companion.parseBedtime
import com.corvettecole.gotosleep.SettingsFragment.Companion.ADS_ENABLED_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.ADVANCED_PURCHASED_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.BEDTIME_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.DND_DELAY_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.DND_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.INACTIVITY_TIMER_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_AMOUNT_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_DELAY_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.SMART_NOTIFICATIONS_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.isUsageAccessGranted


class BedtimeNotificationReceiver : BroadcastReceiver() {
    private var DnD_delay = 2 //in minutes

    private var bedtime: Calendar? = null
    private var numNotifications: Int = 0
    private var notificationDelay: Int = 0
    private var userActiveMargin: Int = 0
    private var adsEnabled: Boolean = false
    private var advancedOptionsPurchased: Boolean = false
    private var smartNotifications: Boolean = false
    private var autoDND: Boolean = false
    private val TAG = "bedtimeNotifReceiver"
    private var currentNotification: Int = 0

    private var shouldEnableAdvancedOptions = false
    private var lastNotification: Long = 0
    private var usageStatsManager: UsageStatsManager? = null

    private//divide time in milliseconds by 60 000 to get minutes
    val notificationContent: String
        get() {
            val current = Calendar.getInstance()
            current.timeInMillis = System.currentTimeMillis()

            val endDate: Date
            val startDate: Date

            val simpleDateFormat = SimpleDateFormat("HH:mm")

            Log.d(TAG, current.get(Calendar.SECOND).toString() + "")
            current.set(Calendar.SECOND, 0)

            startDate = bedtime!!.time
            endDate = current.time

            Log.d(TAG, bedtime!!.time.toString() + " bedtime")

            Log.d(TAG, current.time.toString() + " current time")


            var difference = endDate.time - startDate.time
            if (difference < 0) {
                try {
                    val dateMax = simpleDateFormat.parse("24:00")
                    val dateMin = simpleDateFormat.parse("00:00")
                    difference = dateMax.time - startDate.time + (endDate.time - dateMin.time)
                } catch (e: ParseException) {
                    Log.e(TAG, e.toString() + "")
                }

            }
            val min = Math.round((difference / 60000).toFloat())


            Log.d(TAG, "currentNotification: $currentNotification")

            return if (currentNotification == 1) {
                "Time to head to bed."
            } else if (min == 1) {
                "It is $min minute past your bedtime!"
            } else {
                "It is $min minutes past your bedtime!"
            }
        }

    private val notificationTitle: String?
        get() {
            var notificationTitleIndex = currentNotification - 1
            if (notificationTitleIndex > notifications.size) {
                while (notificationTitleIndex > notifications.size) {
                    notificationTitleIndex -= 5

                }
            }
            return notifications[notificationTitleIndex]
        }

    override fun onReceive(context: Context, intent: Intent) {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        bedtime = Calendar.getInstance()
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "19:35")!!))
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3.toString() + "")!!)
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15.toString() + "")!!)
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false)
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false)
        autoDND = settings.getBoolean(DND_KEY, false)
        smartNotifications = settings.getBoolean(SMART_NOTIFICATIONS_KEY, false)
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1)
        lastNotification = settings.getLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis())
        userActiveMargin = Integer.parseInt(settings.getString(INACTIVITY_TIMER_KEY, "5")!!)
        DnD_delay = Integer.parseInt(settings.getString(DND_DELAY_KEY, "2")!!)

        shouldEnableAdvancedOptions = adsEnabled || advancedOptionsPurchased


        if (shouldEnableAdvancedOptions) {
            for (i in notifications.indices) {
                notifications[i] = settings.getString("pref_notification" + (i + 1), "")
            }
            for (notification in notifications) {
                Log.d(TAG, notification)
            }
        } else {
            notifications[0] = context.resources.getString(R.string.notification1)
            notifications[1] = context.resources.getString(R.string.notification2)
            notifications[2] = context.resources.getString(R.string.notification3)
            notifications[3] = context.resources.getString(R.string.notification4)
            notifications[4] = context.resources.getString(R.string.notification5)
        }

        if (currentNotification == 1) {
            lastNotification = System.currentTimeMillis()
        }

        showNotification(context, notificationTitle, notificationContent)

        if (!smartNotifications || !isUsageAccessGranted(context) || !shouldEnableAdvancedOptions) {
            if (currentNotification < numNotifications) {
                setNextNotification(context)
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply()
            } else if (currentNotification == numNotifications) {
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
                setNextDayNotification(context)
                enableDoNotDisturb(context)
            }

        } else if (isUserActive(UsageStatsManager.INTERVAL_BEST, lastNotification, System.currentTimeMillis()) && System.currentTimeMillis() - bedtime!!.timeInMillis < 6 * ONE_HOUR_MILLIS) {
            //write lastNotification to preferences, set a timer for notifDelay time in the future, show notification now, update currentNotification
            settings.edit().putLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis()).apply()
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply()
            setNextNotification(context)

        } else {
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
            enableDoNotDisturb(context)
        }
    }

    private fun isUserActive(interval: Int, startTime: Long, currentTime: Long): Boolean {
        var startTime = startTime
        if (currentNotification == 1) {
            startTime = startTime - notificationDelay * ONE_MINUTE_MILLIS
        }

        val queryUsageStats = usageStatsManager!!.queryUsageStats(interval, startTime, currentTime)

        var minUsageStat = queryUsageStats[0]

        val min = java.lang.Long.MAX_VALUE
        for (usageStat in queryUsageStats) {
            if (usageStat.lastTimeUsed < min && usageStat.totalTimeInForeground > ONE_MINUTE_MILLIS) {
                minUsageStat = usageStat
            }
        }

        return System.currentTimeMillis() - minUsageStat.lastTimeUsed <= userActiveMargin * ONE_MINUTE_MILLIS
    }

    private fun enableDoNotDisturb(context: Context) {
        if (autoDND) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis() + ONE_MINUTE_MILLIS * DnD_delay
            Log.d(TAG, "Setting auto DND for 2 minutes from now: " + calendar.time)

            val intent1 = Intent(context, AutoDoNotDisturbReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(context,
                    DO_NOT_DISTURB_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }

    private fun showNotification(context: Context, title: String?, content: String) {
        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, LAUNCH_APP_REQUEST_CODE, intent, 0)
        val snoozeIntent = Intent(context, AutoDoNotDisturbReceiver::class.java)

        val snoozePendingIntent = PendingIntent.getBroadcast(context, DO_NOT_DISTURB_REQUEST_CODE, snoozeIntent, 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the notification policy access has been granted for the app.
        val mBuilder = NotificationCompat.Builder(context, BEDTIME_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_moon_notification)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColorized(true)
                .setColor(context.resources.getColor(R.color.moonPrimary))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                mBuilder.addAction(R.drawable.ic_do_not_disturb_on_white_24dp, "I'm going to sleep", snoozePendingIntent)
            }
        }

        notificationManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build())

    }

    private fun setNextNotification(context: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + notificationDelay * 60000
        Log.d(TAG, "Setting next notification in $notificationDelay minutes")

        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context,
                NEXT_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun setNextDayNotification(context: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = bedtime!!.timeInMillis + ONE_DAY_MILLIS
        Log.d(TAG, "Setting notification for tomorrow")

        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    companion object {

        internal val FIRST_NOTIFICATION_ALARM_REQUEST_CODE = 1
        internal val NOTIFICATION_REQUEST_CODE = 2
        internal val DO_NOT_DISTURB_REQUEST_CODE = 3
        internal val DO_NOT_DISTURB_ALARM_REQUEST_CODE = 4
        internal val NEXT_NOTIFICATION_ALARM_REQUEST_CODE = 5
        internal val LAUNCH_APP_REQUEST_CODE = 6
        internal val LAST_NOTIFICATION_KEY = "lastNotificationTime"
        internal val ONE_MINUTE_MILLIS: Long = 60000
        internal val ONE_DAY_MILLIS: Long = 86400000
        internal val ONE_HOUR_MILLIS: Long = 3600000
        internal val CURRENT_NOTIFICATION_KEY = "current_notification"
    }


}
