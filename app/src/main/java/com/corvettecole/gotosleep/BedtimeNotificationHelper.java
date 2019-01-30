/**
 *         Go to Sleep is an open source app to manage a healthy sleep schedule
 *         Copyright (C) 2019 Cole Gerdemann
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.corvettecole.gotosleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.parseBedtime;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.FIRST_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_ENABLE_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.ONE_DAY_MILLIS;

public class BedtimeNotificationHelper extends BroadcastReceiver {

    private Calendar bedtime;
    private final String TAG = "NotificationHelper";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Device booted, broadcast received, setting bedtime notification");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true);

        if (notificationsEnabled) {
            bedtime = Calendar.getInstance();
            bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "22:00")));
            if (bedtime.getTimeInMillis() < System.currentTimeMillis()){
                bedtime.setTimeInMillis(bedtime.getTimeInMillis() + ONE_DAY_MILLIS);
            }
            settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
            setBedtimeNotification(context, bedtime);
        }
    }

    private void setBedtimeNotification(Context context, Calendar bedtime){
        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtime.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, bedtime.getTimeInMillis(), pendingIntent);
        }


    }

}
