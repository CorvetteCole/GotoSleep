package com.corvettecole.gotosleep

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log

import com.android.billingclient.api.BillingClient
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceScreen


class SettingsFragment : BasePreferenceFragmentCompat() {
    private var advancedOptionsPurchased: Boolean = false
    private var enableAdvancedOptions: Boolean = false
    private var adsEnabled: Boolean = false

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val sharedPreferences = preferenceManager.sharedPreferences



        Log.d("PREFERENCES", rootKey.plus(""))
        if (rootKey.isNullOrBlank()) {


            advancedOptionsPurchased = sharedPreferences.getBoolean(ADVANCED_PURCHASED_KEY, false)
            adsEnabled = sharedPreferences.getBoolean(ADS_ENABLED_KEY, false)

            if (advancedOptionsPurchased) {
                preferenceScreen.findPreference("pref_adsEnabled").isEnabled = false
                preferenceScreen.findPreference("pref_advanced_options").isEnabled = true
                sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply()
            } else {
                val advancedPurchasePref = this.findPreference("pref_advanced_purchase")
                advancedPurchasePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    Log.d("Settings", "purchase was clicked!!!")




                    false
                }

            }



            enableAdvancedOptions = advancedOptionsPurchased || adsEnabled


            val bedtime = this.findPreference(BEDTIME_KEY)
            bedtime.summary = "Bedtime is " + sharedPreferences.getString(BEDTIME_KEY, "19:35")!!

            val adsEnabledPref = this.findPreference(ADS_ENABLED_KEY)
            val customNotificationsPref = this.findPreference(CUSTOM_NOTIFICATIONS_KEY)


            customNotificationsPref.isEnabled = enableAdvancedOptions


            adsEnabledPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                //if enable ads is switched off, set premium options to false;
                adsEnabled = newValue as Boolean
                enableAdvancedOptions = advancedOptionsPurchased || adsEnabled
                if (!newValue && !enableAdvancedOptions) {
                    sharedPreferences.edit().putBoolean("pref_smartNotifications", false).apply()
                    customNotificationsPref.isEnabled = false
                } else {
                    customNotificationsPref.isEnabled = true
                }

                true
            }

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                this.findPreference(DND_KEY).isEnabled = false
                this.findPreference(DND_KEY).summary = "Android 6.0 (Marshmallow) and up required"
            }

            val notificationDelay = this.findPreference(NOTIF_DELAY_KEY)
            notificationDelay.summary = sharedPreferences.getString(NOTIF_DELAY_KEY, "15")!! + " minute delay between sleep notifications"
            notificationDelay.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                notificationDelay.summary = newValue.toString() + " minute delay between sleep notifications"
                true
            }

            val notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY)
            notificationAmount.summary = sharedPreferences.getString(NOTIF_AMOUNT_KEY, "2")!! + " sleep reminders will be sent"
            notificationAmount.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                notificationAmount.summary = newValue.toString() + " sleep reminders will be sent"
                true
            }
        } else {
            val notification1 = this.findPreference(NOTIFICATION_1_KEY)
            val notification2 = this.findPreference(NOTIFICATION_2_KEY)
            val notification3 = this.findPreference(NOTIFICATION_3_KEY)
            val notification4 = this.findPreference(NOTIFICATION_4_KEY)
            val notification5 = this.findPreference(NOTIFICATION_5_KEY)

            notification1.summary = sharedPreferences.getString(NOTIFICATION_1_KEY, getString(R.string.notification1))
            notification2.summary = sharedPreferences.getString(NOTIFICATION_2_KEY, getString(R.string.notification2))
            notification3.summary = sharedPreferences.getString(NOTIFICATION_3_KEY, getString(R.string.notification3))
            notification4.summary = sharedPreferences.getString(NOTIFICATION_4_KEY, getString(R.string.notification4))
            notification5.summary = sharedPreferences.getString(NOTIFICATION_5_KEY, getString(R.string.notification5))

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


        }//(!(rootKey + " ").equals("pref_customNotifications_category ")) {
    }

    override fun getCallbackFragment(): Fragment {
        return this
        // or you can return the parent fragment if it's handling the screen navigation,
        // however, in that case you need to traverse to the implementing parent fragment
    }

    override fun onResume() {
        Log.d("settings", "onResume called!")
        super.onResume()
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
        internal val ADS_ENABLED_KEY = "pref_adsEnabled"
        internal val ADVANCED_PURCHASED_KEY = "advanced_options_purchased"
    }

}

