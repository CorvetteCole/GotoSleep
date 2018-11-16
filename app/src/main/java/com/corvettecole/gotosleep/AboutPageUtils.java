package com.corvettecole.gotosleep;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.TypedValue;

class AboutPageUtils {

    static Boolean isAppInstalled(Context context, String appName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}