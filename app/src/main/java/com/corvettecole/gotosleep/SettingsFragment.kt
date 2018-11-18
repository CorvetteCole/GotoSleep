package com.corvettecole.gotosleep

import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast


import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

import java.net.MalformedURLException
import java.net.URL
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.corvettecole.gotosleep.MainActivity.Companion.shouldUpdateConsent


class SettingsFragment : BasePreferenceFragmentCompat(), BillingProcessor.IBillingHandler {

    private var advancedOptionsPurchased: Boolean = false
    private var smartNotificationsEnabled: Boolean = false
    private var enableAdvancedOptions: Boolean = false
    private var adsEnabled: Boolean = false
    private var bp: BillingProcessor? = null
    private var notificationManager: NotificationManager? = null
    private var usageStatsManager: UsageStatsManager? = null
    private var sharedPreferences: SharedPreferences? = null
    private val TAG = "SettingsFragment"
    private val consentForm: ConsentForm? = null

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        sharedPreferences = preferenceManager.sharedPreferences
        notificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        usageStatsManager = activity!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager


        Log.d("PREFERENCES", rootKey.plus(""))
        if (rootKey.isNullOrBlank()) {

            bp = BillingProcessor(context!!, resources.getString(R.string.license_key), this)
            bp!!.initialize()

            val adsEnabledPref = this.findPreference(ADS_ENABLED_KEY)
            val customNotificationsPref = this.findPreference(CUSTOM_NOTIFICATIONS_KEY)
            val autoDnDPref = this.findPreference(DND_KEY)
            val smartNotificationsPref = this.findPreference(SMART_NOTIFICATIONS_KEY)
            val inactivityTimerPref = this.findPreference(INACTIVITY_TIMER_KEY)
            val notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY)
            val notificationDelay = this.findPreference(NOTIF_DELAY_KEY)
            val GDPR = this.findPreference(GDPR_KEY)
            val delayDnDPref = this.findPreference(DND_DELAY_KEY)



            advancedOptionsPurchased = sharedPreferences!!.getBoolean(ADVANCED_PURCHASED_KEY, false)
            adsEnabled = sharedPreferences!!.getBoolean(ADS_ENABLED_KEY, false)
            smartNotificationsEnabled = sharedPreferences!!.getBoolean(SMART_NOTIFICATIONS_KEY, false)


            if (advancedOptionsPurchased) {
                adsEnabledPref.isEnabled = false
                adsEnabledPref.summary = "Ads are disabled, thank you for your support."

                preferenceScreen.findPreference("pref_advanced_options").isEnabled = true
                preferenceManager.sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply()
                preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).isEnabled = true
                delayDnDPref.isEnabled = sharedPreferences!!.getBoolean(DND_KEY, false)
                smartNotificationsPref.isEnabled = true
                preferenceScreen.findPreference("pref_advanced_purchase").summary = "Thank you for supporting me!"
            }

            val consentInformation = ConsentInformation.getInstance(context)
            val publisherIds = arrayOf(context!!.resources.getString(R.string.admob_publisher_id))
            consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    // User's consent status successfully updated.
                    if (consentInformation.isRequestLocationInEeaOrUnknown) {
                        GDPR.setOnPreferenceClickListener { preference ->
                            consentInformation.consentStatus = ConsentStatus.UNKNOWN
                            shouldUpdateConsent = true
                            Toast.makeText(context, "Consent preferences cleared... Go back to main screen to edit", Toast.LENGTH_SHORT).show()
                            //toast here
                            false
                        }
                    } else {
                        GDPR.isVisible = false
                    }


                }

                override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                    // User's consent status failed to update.
                }
            })


            val advancedPurchasePref = this.findPreference("pref_advanced_purchase")
            advancedPurchasePref.setOnPreferenceClickListener { preference ->
                bp!!.purchase(activity, "go_to_sleep_advanced")
                advancedPurchased(sharedPreferences!!, preferenceScreen)




                false
            }





            enableAdvancedOptions = advancedOptionsPurchased || adsEnabled
            if (enableAdvancedOptions) {
                notificationAmount.isEnabled = !smartNotificationsEnabled
            }


            val bedtime = this.findPreference(BEDTIME_KEY)
            try {
                val simpleDateFormat = SimpleDateFormat("HH:mm")
                val time = simpleDateFormat.parse(sharedPreferences!!.getString(BEDTIME_KEY, "19:35"))
                bedtime.summary = "Bedtime is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(time)
            } catch (e: ParseException) {
                e.printStackTrace()
                bedtime.summary = "Bedtime is " + sharedPreferences!!.getString(BEDTIME_KEY, "19:35")!!
            }

            smartNotificationsPref.isEnabled = enableAdvancedOptions
            delayDnDPref.isEnabled = enableAdvancedOptions
            if (enableAdvancedOptions) {
                smartNotificationsPref.summary = "Send notifications until you stop using your phone"
            }
            customNotificationsPref.isEnabled = enableAdvancedOptions


            adsEnabledPref.setOnPreferenceChangeListener { preference, newValue ->
                //if enable ads is switched off, set premium options to false;
                if (!(newValue as Boolean) && !advancedOptionsPurchased) {

                    smartNotificationsPref.isEnabled = false
                    delayDnDPref.isEnabled = false
                    customNotificationsPref.isEnabled = false
                    adsEnabled = false
                } else {
                    smartNotificationsPref.isEnabled = true
                    if (sharedPreferences!!.getBoolean(DND_KEY, false)) {
                        delayDnDPref.isEnabled = true
                    }
                    customNotificationsPref.isEnabled = true
                    adsEnabled = true
                }

                true
            }

            if (sharedPreferences!!.getString(DND_DELAY_KEY, "2") == "1") {
                autoDnDPref.summary = "Automatically enable Do not Disturb 1 minute after the last bedtime reminder is sent"
                delayDnDPref.summary = "Do not Disturb will be activated 1 minute after the last notification is sent"
            } else {
                autoDnDPref.summary = "Automatically enable Do not Disturb " + sharedPreferences!!.getString(DND_DELAY_KEY, "2") + " minutes after the last bedtime reminder is sent"
                delayDnDPref.summary = "Do not Disturb will be activated " + sharedPreferences!!.getString(DND_DELAY_KEY, "2") + " minutes after the last notification is sent"
            }

            delayDnDPref.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue == "1") {
                    autoDnDPref.summary = "Automatically enable Do not Disturb 1 minute after the last bedtime reminder is sent"
                    delayDnDPref.summary = "Do not Disturb will be activated 1 minute after the last notification is sent"
                } else {
                    autoDnDPref.summary = "Automatically enable Do not Disturb $newValue minutes after the last bedtime reminder is sent"
                    delayDnDPref.summary = "Do not Disturb will be activated $newValue minutes after the last notification is sent"
                }
                true
            }


            //#TODO figure out a way to only toggle switch if the notification or usage permission is actually granted.
            // Returning false in onPreferenceChange will not update the preference with the new value, and onClickListeners exist...

            smartNotificationsPref.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue as Boolean) {
                    notificationAmount.isEnabled = false
                    if (!isUsageAccessGranted(context!!)) {
                        val usageSettings = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        startActivity(usageSettings)
                    }
                } else if (!newValue) {
                    notificationAmount.isEnabled = true
                }
                true
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                this.findPreference(DND_KEY).isEnabled = false
                this.findPreference(DND_KEY).summary = "Android 6.0 (Marshmallow) and up required"
                //# TODO add else if to check if user device is an LG G4. If so, disable option with reason
            } else {
                autoDnDPref.setOnPreferenceChangeListener { preference, newValue ->

                    // Check if the notification policy access has been granted for the app.
                    enableAdvancedOptions = advancedOptionsPurchased || adsEnabled
                    if (newValue as Boolean) {
                        if (!notificationManager!!.isNotificationPolicyAccessGranted) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            startActivity(intent)
                        }
                        if (enableAdvancedOptions) {
                            delayDnDPref.isEnabled = true
                        }
                    } else {
                        delayDnDPref.isEnabled = false
                    }

                    true
                }
            }

            notificationDelay.summary = sharedPreferences!!.getString(NOTIF_DELAY_KEY, "15")!! + " minute delay between sleep notifications"
            notificationDelay.setOnPreferenceChangeListener { preference, newValue ->
                notificationDelay.summary = newValue.toString() + " minute delay between sleep notifications"
                true
            }

            if (sharedPreferences!!.getString(NOTIF_AMOUNT_KEY, "3") == "1") {
                notificationAmount.summary = "1 sleep reminder will be sent"
            } else {
                notificationAmount.summary = sharedPreferences!!.getString(NOTIF_AMOUNT_KEY, "3")!! + " sleep reminders will be sent"
            }
            notificationAmount.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue as String == "1") {
                    notificationAmount.summary = "1 sleep reminder will be sent"
                } else {
                    notificationAmount.summary = "$newValue sleep reminders will be sent"
                }
                true
            }

            if (Integer.parseInt(sharedPreferences!!.getString(INACTIVITY_TIMER_KEY, "5")!!) == 1) {
                inactivityTimerPref.summary = "User must be inactive for 1 minute to be considered inactive"
            } else {
                inactivityTimerPref.summary = "User must be inactive for " + sharedPreferences!!.getString(INACTIVITY_TIMER_KEY, "5") + " minutes to be considered inactive"
            }
            inactivityTimerPref.setOnPreferenceChangeListener { preference, newValue ->
                if (Integer.parseInt(newValue as String) == 1) {
                    inactivityTimerPref.summary = "User must be inactive for 1 minute to be considered inactive"
                } else {
                    inactivityTimerPref.summary = "User must be inactive for $newValue minutes to be considered inactive"
                }
                true
            }

        } else {
            startCustomNotificationsScreen()
        }
    }

    private fun startCustomNotificationsScreen() {

        val notification1 = this.findPreference(NOTIFICATION_1_KEY)
        val notification2 = this.findPreference(NOTIFICATION_2_KEY)
        val notification3 = this.findPreference(NOTIFICATION_3_KEY)
        val notification4 = this.findPreference(NOTIFICATION_4_KEY)
        val notification5 = this.findPreference(NOTIFICATION_5_KEY)

        notification1.summary = sharedPreferences!!.getString(NOTIFICATION_1_KEY, getString(R.string.notification1))
        notification2.summary = sharedPreferences!!.getString(NOTIFICATION_2_KEY, getString(R.string.notification2))
        notification3.summary = sharedPreferences!!.getString(NOTIFICATION_3_KEY, getString(R.string.notification3))
        notification4.summary = sharedPreferences!!.getString(NOTIFICATION_4_KEY, getString(R.string.notification4))
        notification5.summary = sharedPreferences!!.getString(NOTIFICATION_5_KEY, getString(R.string.notification5))

        notification1.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            notification1.summary = newValue as String
            true
        }

        notification2.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            notification2.summary = newValue as String
            true
        }

        notification3.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            notification3.summary = newValue as String
            true
        }

        notification4.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            notification4.summary = newValue as String
            true
        }

        notification5.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            notification5.summary = newValue as String
            true
        }
    }

    private fun advancedPurchased(sharedPreferences: SharedPreferences, preferenceScreen: PreferenceScreen) {
        advancedOptionsPurchased = true
        sharedPreferences.edit().putBoolean(ADVANCED_PURCHASED_KEY, true).apply()
        preferenceScreen.findPreference(ADS_ENABLED_KEY).isEnabled = false
        preferenceScreen.findPreference(ADS_ENABLED_KEY).summary = "Ads are disabled, thank you for supporting me!"
        preferenceScreen.findPreference("pref_advanced_options").isEnabled = true
        sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply()
        preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).isEnabled = true
        preferenceScreen.findPreference(SMART_NOTIFICATIONS_KEY).isEnabled = true
        preferenceScreen.findPreference("pref_advanced_purchase").summary = "Thank you for supporting me!"
        if (sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false)) {
            preferenceScreen.findPreference(NOTIF_AMOUNT_KEY).isEnabled = false
        }
        if (sharedPreferences.getBoolean(DND_KEY, false)) {
            preferenceScreen.findPreference(DND_DELAY_KEY).isEnabled = true
        }
    }

    private fun advancedPurchasedError(sharedPreferences: SharedPreferences, preferenceScreen: PreferenceScreen) {
        advancedOptionsPurchased = false
        sharedPreferences.edit().putBoolean(ADVANCED_PURCHASED_KEY, false).apply()
        preferenceScreen.findPreference(ADS_ENABLED_KEY).isEnabled = true
        preferenceScreen.findPreference(ADS_ENABLED_KEY).summary = "Unlock advanced options without paying by showing ads (ads will never be shown outside of the app) "
        preferenceScreen.findPreference("pref_advanced_purchase").summary = "Support me and unlock all advanced options without advertisements for life"
        if (!adsEnabled) {
            preferenceScreen.findPreference("pref_advanced_options").isEnabled = false
            sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply()
            preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).isEnabled = false
            preferenceScreen.findPreference(SMART_NOTIFICATIONS_KEY).isEnabled = false
            preferenceScreen.findPreference(NOTIF_AMOUNT_KEY).isEnabled = true
            preferenceScreen.findPreference(DND_DELAY_KEY).isEnabled = false
        }
    }


    override fun getCallbackFragment(): Fragment {
        return this
        // or you can return the parent fragment if it's handling the screen navigation,
        // however, in that case you need to traverse to the implementing parent fragment
    }

    override fun onResume() {
        Log.d("settings", "onResume called!")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sharedPreferences!!.getBoolean(DND_KEY, false) && !notificationManager!!.isNotificationPolicyAccessGranted) {
                Toast.makeText(context, "Do not disturb access not granted, toggle option to try again", Toast.LENGTH_LONG).show()
            }
        }

        if (sharedPreferences!!.getBoolean(SMART_NOTIFICATIONS_KEY, false) && !isUsageAccessGranted(context!!)) {
            Toast.makeText(context, "Usage access not granted, toggle option to try again", Toast.LENGTH_LONG).show()
        }

        super.onResume()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        if (productId == "go_to_sleep_advanced") {
            Log.d("productPurchased", "go to sleep advanced purchased")
            advancedPurchased(sharedPreferences!!, preferenceScreen)
        }

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        advancedPurchasedError(sharedPreferences!!, preferenceScreen)
    }

    override fun onBillingInitialized() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        if (bp != null) {
            bp!!.release()
        }
        super.onDestroy()
    }

    companion object {

        internal val NOTIF_DELAY_KEY = "pref_notificationDelay"
        internal val NOTIF_AMOUNT_KEY = "pref_numNotifications"
        internal val NOTIF_ENABLE_KEY = "pref_notificationsEnabled"
        internal val BEDTIME_KEY = "pref_bedtime"
        internal val DND_KEY = "pref_autoDoNotDisturb"
        internal val BUTTON_HIDE_KEY = "pref_buttonHide"
        internal val NOTIFICATION_1_KEY = "pref_notification1"
        internal val NOTIFICATION_2_KEY = "pref_notification2"
        internal val NOTIFICATION_3_KEY = "pref_notification3"
        internal val NOTIFICATION_4_KEY = "pref_notification4"
        internal val NOTIFICATION_5_KEY = "pref_notification5"
        internal val CUSTOM_NOTIFICATIONS_KEY = "pref_customNotifications_category"
        internal val SMART_NOTIFICATIONS_KEY = "pref_smartNotifications"
        internal val ADS_ENABLED_KEY = "pref_adsEnabled"
        internal val ADVANCED_PURCHASED_KEY = "advanced_options_purchased"
        internal val INACTIVITY_TIMER_KEY = "pref_activityMargin"
        internal val GDPR_KEY = "pref_gdpr"
        internal val DND_DELAY_KEY = "pref_dndDelay"


        fun isUsageAccessGranted(context: Context): Boolean {
            try {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
                val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                var mode = 0
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName)
                return mode == AppOpsManager.MODE_ALLOWED

            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }

        }
    }

}

