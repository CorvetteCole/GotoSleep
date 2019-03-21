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

import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.parseBedtime;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIFICATION_REQUEST_CODE;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.cancelNextNotification;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.setNextDayNotification;

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


        mNotificationManager.cancel(NOTIFICATION_REQUEST_CODE);
        cancelNextNotification(context);
        setNextDayNotification(context, bedtime, TAG);
    }
}
