package com.corvettecole.gotosleep

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.TypedValue

internal object AboutPageUtils {

    fun isAppInstalled(context: Context, appName: String): Boolean? {
        val pm = context.packageManager
        var installed: Boolean
        try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
            installed = true
        } catch (e: PackageManager.NameNotFoundException) {
            installed = false
        }

        return installed
    }
}