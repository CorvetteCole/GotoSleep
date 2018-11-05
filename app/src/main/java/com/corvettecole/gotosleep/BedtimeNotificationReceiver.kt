package com.corvettecole.gotosleep

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
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
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_AMOUNT_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_DELAY_KEY

class BedtimeNotificationReceiver : BroadcastReceiver() {

    internal val reqCode = 8
    private var bedtime: Calendar? = null
    private var numNotifications: Int = 0
    private var notificationDelay: Int = 0
    private var adsEnabled: Boolean = false
    private var advancedOptionsPurchased: Boolean = false
    internal val TAG = "bedtimeNotifReceiver"
    private var currentNotification: Int = 0

    val notificationContent: Array<String?>
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
            val day = (difference / (1000 * 60 * 60 * 24)).toInt()
            val hour = ((difference - 1000 * 60 * 60 * 24 * day) / (1000 * 60 * 60)).toInt()
            Log.d(TAG, ((difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()) / (1000 * 60)).toString() + " min")
            val min = (difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()).toInt() / (1000 * 60)
            Log.i(TAG, "Days: $day Hours: $hour, Mins: $min")

            val totalMin = hour * 60 + min


            Log.d(TAG, "currentNotification: $currentNotification")


            return if (currentNotification == 1) {
                arrayOf(notifications[currentNotification - 1], "Time to head to bed.")
            } else {
                arrayOf(notifications[currentNotification - 1], "It is $totalMin minutes past your bedtime!")
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        bedtime = Calendar.getInstance()
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "19:35")!!))
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3.toString() + "")!!)
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15.toString() + "")!!)
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false)
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false)

        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1)

        if (adsEnabled || advancedOptionsPurchased) {
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


        val notificationContent = notificationContent
        //check for more info
        showNotification(context, notificationContent[0], notificationContent[1])

        if (currentNotification < numNotifications) {
            setNextNotification(context, 12)
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply()
        } else if (currentNotification == numNotifications) {
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
            setNextDayNotification(context, 1)
        }
    }

    fun showNotification(context: Context, title: String?, content: String?) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, reqCode, intent, 0)
        val mBuilder = NotificationCompat.Builder(context, BEDTIME_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_moon)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColorized(true)
                .setColor(context.resources.getColor(R.color.moonPrimary))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reqCode, mBuilder.build())

    }

    fun setNextNotification(context: Context, REQUEST_CODE_BEDTIME: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + notificationDelay * 60000
        Log.d(TAG, "Setting next notification in $notificationDelay minutes")

        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE_BEDTIME, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun setNextDayNotification(context: Context, REQUEST_CODE_BEDTIME: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = bedtime!!.timeInMillis + ONE_DAY_MILLIS
        Log.d(TAG, "Setting notification for tomorrow")

        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE_BEDTIME, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    companion object {
        internal val ONE_DAY_MILLIS = 86400000
        internal val CURRENT_NOTIFICATION_KEY = "current_notification"
    }


}
