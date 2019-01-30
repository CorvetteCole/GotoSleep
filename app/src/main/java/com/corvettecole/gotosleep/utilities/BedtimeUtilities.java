package com.corvettecole.gotosleep.utilities;

import android.util.Log;

import java.util.Calendar;

public class BedtimeUtilities {

    public static int[] parseBedtime(String bedtime){
        int bedtimeHour = Integer.parseInt(bedtime.substring(0, bedtime.indexOf(":")));
        int bedtimeMin = Integer.parseInt(bedtime.substring(bedtime.indexOf(":") + 1, bedtime.length()));
        return new int[]{bedtimeHour, bedtimeMin};
    }

    public static Calendar getBedtimeCal (int[] bedtime){
        String TAG = "getBedtimeCal";
        Log.d(TAG, "bedtime[0], bedtime[1] " + bedtime[0] + "," + bedtime[1]);
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, bedtime[0]);
        calendar.set(Calendar.MINUTE, bedtime[1]);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

}
