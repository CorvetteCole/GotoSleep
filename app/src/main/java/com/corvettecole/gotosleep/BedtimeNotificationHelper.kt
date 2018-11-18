package com.corvettecole.gotosleep

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log

import java.util.Calendar

import android.content.Context.ALARM_SERVICE
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.CURRENT_NOTIFICATION_KEY
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.FIRST_NOTIFICATION_ALARM_REQUEST_CODE
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.ONE_DAY_MILLIS
import com.corvettecole.gotosleep.MainActivity.Companion.getBedtimeCal
import com.corvettecole.gotosleep.MainActivity.Companion.parseBedtime
import com.corvettecole.gotosleep.SettingsFragment.Companion.BEDTIME_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_ENABLE_KEY


class BedtimeNotificationHelper : BroadcastReceiver() {


    private val TAG = "NotificationHelper"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Device booted, broadcast received, setting bedtime notification")

        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true)
        var bedtime: Calendar

        if (notificationsEnabled) {
            bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "19:35")!!))
            if (bedtime.timeInMillis < System.currentTimeMillis()) {
                bedtime.timeInMillis = bedtime.timeInMillis + ONE_DAY_MILLIS
            }
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
            setBedtimeNotification(context, bedtime)
        }
    }

    private fun setBedtimeNotification(context: Context, bedtime: Calendar) {
        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtime.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, bedtime.timeInMillis, pendingIntent)
        }


    }

}
