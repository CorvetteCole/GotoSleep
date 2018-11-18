package com.corvettecole.gotosleep

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.NOTIFICATION_REQUEST_CODE

class AutoDoNotDisturbReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AutoDnDReceiver", "Attempting to enable DnD")
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
            Toast.makeText(context, "Enabled DnD... Go to sleep!", Toast.LENGTH_SHORT).show()
        }
        mNotificationManager.cancel(NOTIFICATION_REQUEST_CODE)
    }
}
