package com.corvettecole.gotosleep;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.preference.Preference;


public class SettingsFragment extends BasePreferenceFragmentCompat{

    final static String NOTIF_DELAY_KEY = "pref_notificationDelay";
    final static String NOTIF_AMOUNT_KEY = "pref_numNotifications";
    final static String NOTIF_ENABLE_KEY = "pref_notificationEnabled";
    final static String BEDTIME_KEY = "pref_bedtime";
    final static String DND_KEY = "pref_autoDoNotDisturb";
    final static String BUTTON_HIDE_KEY = "pref_buttonHide";

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        /*#TODO check if premium unlock has been purchased. If so, enable premium category, set adsEnabled to false, and disable ads pref
        getPreferenceScreen().findPreference("pref_adsEnabled").setEnabled(false);
        getPreferenceScreen().findPreference("pref_premium").setEnabled(true);
        */

        Preference bedtime = this.findPreference(BEDTIME_KEY);
        bedtime.setSummary("Bedtime is " + getPreferenceManager().getSharedPreferences().getString(BEDTIME_KEY, "19:35"));

        final Preference adsEnabledPref = this.findPreference("pref_adsEnabled");

        adsEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //if enable ads is switched off, set premium options to false;
                if (!(boolean)newValue){
                    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                    sharedPreferences.edit().putBoolean("pref_smartNotifications", false).apply();
                }
                return true;
            }
        });

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            this.findPreference(DND_KEY).setEnabled(false);
            this.findPreference(DND_KEY).setSummary("Android 6.0 (Marshmallow) and up required");
        }

        final Preference notificationDelay = this.findPreference(NOTIF_DELAY_KEY);
        notificationDelay.setSummary(getPreferenceManager().getSharedPreferences().getString(NOTIF_DELAY_KEY, "15") + " minute delay between sleep notifications");
        notificationDelay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notificationDelay.setSummary(newValue + " minute delay between sleep notifications");
                return true;
            }
        });

        final Preference notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY);
        notificationAmount.setSummary(getPreferenceManager().getSharedPreferences().getString(NOTIF_AMOUNT_KEY, "2") + " sleep reminders will be sent");
        notificationAmount.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notificationAmount.setSummary(newValue + " sleep reminders will be sent");
                return true;
            }
        });
    }
}

