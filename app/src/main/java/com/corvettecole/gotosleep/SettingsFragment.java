package com.corvettecole.gotosleep;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.android.billingclient.api.BillingClient;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;


public class SettingsFragment extends BasePreferenceFragmentCompat{

    final static String NOTIF_DELAY_KEY = "pref_notificationDelay";
    final static String NOTIF_AMOUNT_KEY = "pref_numNotifications";
    final static String NOTIF_ENABLE_KEY = "pref_notificationsEnabled";
    final static String BEDTIME_KEY = "pref_bedtime";
    final static String DND_KEY = "pref_autoDoNotDisturb";
    final static String BUTTON_HIDE_KEY = "pref_buttonHide";
    final static String NOTIFICATION_1_KEY = "pref_notification1";
    final static String NOTIFICATION_2_KEY = "pref_notification2";
    final static String NOTIFICATION_3_KEY = "pref_notification3";
    final static String NOTIFICATION_4_KEY = "pref_notification4";
    final static String NOTIFICATION_5_KEY = "pref_notification5";
    final static String CUSTOM_NOTIFICATIONS_KEY = "pref_customNotifications_category";
    final static String ADS_ENABLED_KEY = "pref_adsEnabled";
    final static String ADVANCED_PURCHASED_KEY = "advanced_options_purchased";
    private boolean advancedOptionsPurchased;
    private boolean enableAdvancedOptions;
    private boolean adsEnabled;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();



        Log.d("PREFERENCES", rootKey + " ");
        if //(!(rootKey + " ").equals("pref_customNotifications_category ")) {
                (rootKey == null){


            advancedOptionsPurchased = sharedPreferences.getBoolean(ADVANCED_PURCHASED_KEY, false);
            adsEnabled = sharedPreferences.getBoolean(ADS_ENABLED_KEY, false);

            if (advancedOptionsPurchased) {
                getPreferenceScreen().findPreference("pref_adsEnabled").setEnabled(false);
                getPreferenceScreen().findPreference("pref_advanced_options").setEnabled(true);
                sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply();
            } else {
                final Preference advancedPurchasePref = this.findPreference("pref_advanced_purchase");
                advancedPurchasePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d("Settings", "purchase was clicked!!!");




                        return false;
                    }
                });

            }



            enableAdvancedOptions = advancedOptionsPurchased || adsEnabled;



            Preference bedtime = this.findPreference(BEDTIME_KEY);
            bedtime.setSummary("Bedtime is " + sharedPreferences.getString(BEDTIME_KEY, "19:35"));

            final Preference adsEnabledPref = this.findPreference(ADS_ENABLED_KEY);
            final Preference customNotificationsPref = this.findPreference(CUSTOM_NOTIFICATIONS_KEY);


            customNotificationsPref.setEnabled(enableAdvancedOptions);


            adsEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //if enable ads is switched off, set premium options to false;
                    adsEnabled = (boolean)newValue;
                    enableAdvancedOptions = advancedOptionsPurchased || adsEnabled;
                    if ((!(boolean) newValue) && (!enableAdvancedOptions)) {
                        sharedPreferences.edit().putBoolean("pref_smartNotifications", false).apply();
                        customNotificationsPref.setEnabled(false);
                    } else {
                        customNotificationsPref.setEnabled(true);
                    }

                    return true;
                }
            });

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                this.findPreference(DND_KEY).setEnabled(false);
                this.findPreference(DND_KEY).setSummary("Android 6.0 (Marshmallow) and up required");
            }

            final Preference notificationDelay = this.findPreference(NOTIF_DELAY_KEY);
            notificationDelay.setSummary(sharedPreferences.getString(NOTIF_DELAY_KEY, "15") + " minute delay between sleep notifications");
            notificationDelay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notificationDelay.setSummary(newValue + " minute delay between sleep notifications");
                    return true;
                }
            });

            final Preference notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY);
            notificationAmount.setSummary(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "2") + " sleep reminders will be sent");
            notificationAmount.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notificationAmount.setSummary(newValue + " sleep reminders will be sent");
                    return true;
                }
            });
        } else {
            final Preference notification1 = this.findPreference(NOTIFICATION_1_KEY);
            final Preference notification2 = this.findPreference(NOTIFICATION_2_KEY);
            final Preference notification3 = this.findPreference(NOTIFICATION_3_KEY);
            final Preference notification4 = this.findPreference(NOTIFICATION_4_KEY);
            final Preference notification5 = this.findPreference(NOTIFICATION_5_KEY);

            notification1.setSummary(sharedPreferences.getString(NOTIFICATION_1_KEY, getString(R.string.notification1)));
            notification2.setSummary(sharedPreferences.getString(NOTIFICATION_2_KEY, getString(R.string.notification2)));
            notification3.setSummary(sharedPreferences.getString(NOTIFICATION_3_KEY, getString(R.string.notification3)));
            notification4.setSummary(sharedPreferences.getString(NOTIFICATION_4_KEY, getString(R.string.notification4)));
            notification5.setSummary(sharedPreferences.getString(NOTIFICATION_5_KEY, getString(R.string.notification5)));

            notification1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification1.setSummary((String) newValue);
                    return true;
                }
            });

            notification2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification2.setSummary((String) newValue);
                    return true;
                }
            });

            notification3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification3.setSummary((String) newValue);
                    return true;
                }
            });

            notification4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification4.setSummary((String) newValue);
                    return true;
                }
            });

            notification5.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    notification5.setSummary((String) newValue);
                    return true;
                }
            });



        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
        // or you can return the parent fragment if it's handling the screen navigation,
        // however, in that case you need to traverse to the implementing parent fragment
    }

    @Override
    public void onResume(){
        Log.d("settings", "onResume called!");
        super.onResume();
    }

}

