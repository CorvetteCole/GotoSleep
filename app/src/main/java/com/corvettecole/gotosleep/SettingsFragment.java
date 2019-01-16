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

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import static com.corvettecole.gotosleep.MainActivity.BEDTIME_CHANNEL_ID;
import static com.corvettecole.gotosleep.MainActivity.parseBedtime;
import static com.corvettecole.gotosleep.MainActivity.setNotifications;
import static com.corvettecole.gotosleep.MainActivity.shouldUpdateConsent;


public class SettingsFragment extends BasePreferenceFragmentCompat implements BillingProcessor.IBillingHandler {

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
    final static String SMART_NOTIFICATIONS_KEY = "pref_smartNotifications";
    final static String ADS_ENABLED_KEY = "pref_adsEnabled";
    final static String ADVANCED_PURCHASED_KEY = "advanced_options_purchased";
    final static String INACTIVITY_TIMER_KEY = "pref_activityMargin";
    final static String GDPR_KEY = "pref_gdpr";
    final static String DND_DELAY_KEY = "pref_dndDelay";
    final static String NOTIFICATION_SOUND_KEY = "pref_notificationSound";
    final static String ADDITIONAL_NOTIFICATION_SETTINGS_KEY = "pref_notificationChannel";
    final static String SEND_ONE_NOTIFICATION = "pref_sendOneNotification";

    private boolean advancedOptionsPurchased;
    private boolean smartNotificationsEnabled;
    private boolean enableAdvancedOptions;
    private boolean adsEnabled;
    private BillingProcessor bp;
    private NotificationManager notificationManager;
    private UsageStatsManager usageStatsManager;
    private SharedPreferences sharedPreferences;
    private final String TAG = "SettingsFragment";
    private ConsentForm consentForm;

    private boolean desiredSmartNotificationValue = false;
    private boolean desiredDoNotDisturbValue = false;

    private String rootKey;

    private PreferenceScreen preferenceScreen;

    private boolean usageSettingsOpened = false;
    private boolean doNotDisturbSettingsOpened = false;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.rootKey = rootKey;
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        notificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
        usageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);


        //COMPILE INSTRUCTIONS: Comment out the following block
        //Start
        bp = new BillingProcessor(Objects.requireNonNull(getContext()), getResources().getString(R.string.license_key), this);
        bp.loadOwnedPurchasesFromGoogle();
        bp.initialize();
        //End

        Log.d("PREFERENCES", rootKey + " ");
        if (rootKey == null){

            final Preference adsEnabledPref = this.findPreference(ADS_ENABLED_KEY);
            final Preference customNotificationsPref = this.findPreference(CUSTOM_NOTIFICATIONS_KEY);
            final Preference autoDnDPref = this.findPreference(DND_KEY);
            final Preference smartNotificationsPref = this.findPreference(SMART_NOTIFICATIONS_KEY);
            final Preference inactivityTimerPref = this.findPreference(INACTIVITY_TIMER_KEY);
            final Preference notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY);
            final Preference notificationDelay = this.findPreference(NOTIF_DELAY_KEY);
            final Preference GDPR = this.findPreference(GDPR_KEY);
            final Preference delayDnDPref = this.findPreference(DND_DELAY_KEY);
            final Preference notificationChannel = this.findPreference(ADDITIONAL_NOTIFICATION_SETTINGS_KEY);
            final Preference notificationSound = this.findPreference(NOTIFICATION_SOUND_KEY);


            advancedOptionsPurchased = sharedPreferences.getBoolean(ADVANCED_PURCHASED_KEY, false);
            adsEnabled = sharedPreferences.getBoolean(ADS_ENABLED_KEY, false);
            smartNotificationsEnabled = sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false);




            if (advancedOptionsPurchased) {
               adsEnabledPref.setEnabled(false);
               adsEnabledPref.setSummary(R.string.settingsAdsDisabledSummary);

               getPreferenceScreen().findPreference("pref_advanced_options").setEnabled(true);
               getPreferenceManager().getSharedPreferences().edit().putBoolean(ADS_ENABLED_KEY, false).apply();
               getPreferenceScreen().findPreference(CUSTOM_NOTIFICATIONS_KEY).setEnabled(true);
               delayDnDPref.setEnabled(sharedPreferences.getBoolean(DND_KEY, false));
               smartNotificationsPref.setEnabled(true);
               getPreferenceScreen().findPreference("pref_advanced_purchase").setSummary(R.string.settingsAdvancedPurchasedSummary);
            }
            //COMPILE INSTRUCTIONS: comment out the following code block
            //Start
            ConsentInformation consentInformation = ConsentInformation.getInstance(getContext());
            String[] publisherIds = {getContext().getResources().getString(R.string.admob_publisher_id)};
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    // User's consent status successfully updated.
                    if (consentInformation.isRequestLocationInEeaOrUnknown()){
                        GDPR.setOnPreferenceClickListener(preference -> {
                            consentInformation.setConsentStatus(ConsentStatus.UNKNOWN);
                            shouldUpdateConsent = true;
                            Toast.makeText(getContext(), getString(R.string.settingsConsentPrefsClearedToast), Toast.LENGTH_SHORT).show();
                            //toast here
                            return false;
                        });
                    }  else {
                        GDPR.setVisible(false);
                    }


                }

                @Override
                public void onFailedToUpdateConsentInfo(String errorDescription) {
                    // User's consent status failed to update.
                }
            });
            //End


            final Preference advancedPurchasePref = this.findPreference("pref_advanced_purchase");
            advancedPurchasePref.setOnPreferenceClickListener(preference -> {
                //COMPILE INSTRUCTIONS: comment out the following line and uncomment the one below it
                bp.purchase(getActivity(), "go_to_sleep_advanced");
                //advancedPurchased(sharedPreferences, getPreferenceScreen());




                return false;
            });





            enableAdvancedOptions = advancedOptionsPurchased || adsEnabled;
            if (enableAdvancedOptions) {
                notificationAmount.setEnabled(!smartNotificationsEnabled);
            }



            Preference bedtime = this.findPreference(BEDTIME_KEY);
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                Date time = simpleDateFormat.parse(sharedPreferences.getString(BEDTIME_KEY, "22:00"));
                bedtime.setSummary(String.format(Locale.US, getString(R.string.settingsBedtimeSummary), DateFormat.getTimeInstance(DateFormat.SHORT).format(time)));
            } catch (ParseException e) {
                e.printStackTrace();
                bedtime.setSummary(String.format(Locale.US, getString(R.string.settingsBedtimeSummary), sharedPreferences.getString(BEDTIME_KEY, "22:00")));
            }

            smartNotificationsPref.setEnabled(enableAdvancedOptions);
            delayDnDPref.setEnabled(enableAdvancedOptions);
            if (enableAdvancedOptions){
                smartNotificationsPref.setSummary(R.string.settingsSmartNotificationsSummaryEnabled);
            }
            customNotificationsPref.setEnabled(enableAdvancedOptions);


            //notification channel additional settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationSound.setVisible(false);
                notificationChannel.setVisible(true);
                notificationChannel.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                            .putExtra(Settings.EXTRA_CHANNEL_ID, BEDTIME_CHANNEL_ID);
                    startActivity(intent);
                    return true;
                });
            } else {
                notificationSound.setVisible(true);
                notificationChannel.setVisible(false);
            }


            adsEnabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                //if enable ads is switched off, set premium options to false;
                if ((!(boolean) newValue) && (!advancedOptionsPurchased)) {

                    smartNotificationsPref.setEnabled(false);
                    delayDnDPref.setEnabled(false);
                    customNotificationsPref.setEnabled(false);
                    adsEnabled = false;
                } else {
                    smartNotificationsPref.setEnabled(true);
                    if (sharedPreferences.getBoolean(DND_KEY, false)){
                        delayDnDPref.setEnabled(true);
                    }
                    customNotificationsPref.setEnabled(true);
                    adsEnabled = true;
                }

                return true;
            });

            int dnd_delay = Integer.parseInt(sharedPreferences.getString(DND_DELAY_KEY, "2"));
            if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                if (dnd_delay == 1) {
                    autoDnDPref.setSummary(R.string.settingsAutoDnDSummarySingular);
                    delayDnDPref.setSummary(R.string.settingsDelayDnDSummarySingular);
                } else if (dnd_delay >= 2 && dnd_delay <= 4){
                    autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryFunky), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                    delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryFunky), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                } else {
                    autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                    delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                }
            } else {
                if (dnd_delay == 1) {
                    autoDnDPref.setSummary(R.string.settingsAutoDnDSummarySingular);
                    delayDnDPref.setSummary(R.string.settingsDelayDnDSummarySingular);
                } else {
                    autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                    delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
                }
            }

            delayDnDPref.setOnPreferenceChangeListener((preference, newValue) -> {
                int value = Integer.parseInt((String)newValue);
                //if user is polish do the weird polish language stuff
                if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                    if (value == 1) {
                        autoDnDPref.setSummary(R.string.settingsAutoDnDSummarySingular);
                        delayDnDPref.setSummary(R.string.settingsDelayDnDSummarySingular);
                    } else if (value >= 2 && value <= 4){
                        autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryFunky), newValue));
                        delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryFunky), newValue));
                    } else {
                        autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryPlural), newValue));
                        delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryPlural), newValue));
                    }
                } else {
                    if (value == 1) {
                        autoDnDPref.setSummary(R.string.settingsAutoDnDSummarySingular);
                        delayDnDPref.setSummary(R.string.settingsDelayDnDSummarySingular);
                    } else {
                        autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryPlural), newValue));
                        delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryPlural), newValue));
                    }
                }
                return true;
            });


            smartNotificationsPref.setOnPreferenceChangeListener((preference, newValue) -> {

                if (!desiredSmartNotificationValue) {
                    if ((boolean) newValue) {
                        if (!isUsageAccessGranted(getContext())) {
                            usageSettingsOpened = true;
                            Intent usageSettings = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(usageSettings);
                            return false;
                        } else {
                            notificationAmount.setEnabled(false);
                        }
                        return true;
                    } else if (!(boolean) newValue) {
                        notificationAmount.setEnabled(true);
                        return true;
                    }
                } else if ((boolean) newValue) {
                    notificationAmount.setEnabled(false);
                    usageSettingsOpened = false;
                    return true;

                } else {
                    notificationAmount.setEnabled(true);
                    usageSettingsOpened = false;
                    desiredSmartNotificationValue = false;
                    return true;
                }
                return true;
            });
            if (advancedOptionsPurchased || adsEnabled){
                delayDnDPref.setEnabled(sharedPreferences.getBoolean(DND_KEY, false));
            } else {
                delayDnDPref.setEnabled(false);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                this.findPreference(DND_KEY).setEnabled(false);
                this.findPreference(DND_KEY).setSummary(R.string.settingsAutoDnDLowAndroidVersonMessage);
            } else if (android.os.Build.MANUFACTURER.equalsIgnoreCase("lg") && android.os.Build.MODEL.equalsIgnoreCase("g4")){
                this.findPreference(DND_KEY).setEnabled(false);
                this.findPreference(DND_KEY).setSummary(R.string.settingsAutoDnD_LG_G4_message);
            } else {
                autoDnDPref.setOnPreferenceChangeListener((preference, newValue) -> {


                    enableAdvancedOptions = advancedOptionsPurchased || adsEnabled;


                    if (!desiredDoNotDisturbValue) {
                        if ((boolean) newValue) {
                            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                                doNotDisturbSettingsOpened = true;
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                startActivity(intent);
                                return false;
                            } else if (enableAdvancedOptions) {
                                delayDnDPref.setEnabled(true);
                            }
                            return true;
                        } else if (!(boolean) newValue) {
                            delayDnDPref.setEnabled(false);
                            return true;
                        }
                    } else if ((boolean) newValue) {
                        if (enableAdvancedOptions) {
                            delayDnDPref.setEnabled(true);
                        }
                        doNotDisturbSettingsOpened = false;
                        return true;

                    } else {
                        if (enableAdvancedOptions) {
                            delayDnDPref.setEnabled(false);
                        }
                        doNotDisturbSettingsOpened = false;
                        desiredDoNotDisturbValue = false;
                        return true;
                    }
                    return true;



                });
            }

            notificationDelay.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationDelaySummary), sharedPreferences.getString(NOTIF_DELAY_KEY, "15")));
            notificationDelay.setOnPreferenceChangeListener((preference, newValue) -> {
                notificationDelay.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationDelaySummary), newValue));
                return true;
            });

            int notificationAmountTemp = Integer.parseInt(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3"));
            if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                if (notificationAmountTemp == 1) {
                    notificationAmount.setSummary(R.string.settingsNotificationAmountSingular);
                } else if (notificationAmountTemp >= 2 && notificationAmountTemp <= 4){
                    notificationAmount.setSummary(String.format(getString(R.string.settingsNotificationAmountFunky), sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
                } else {
                    notificationAmount.setSummary(String.format(getString(R.string.settingsNotificationAmountPlural), sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
                }
            } else {
                if (notificationAmountTemp == 1) {
                    notificationAmount.setSummary(R.string.settingsNotificationAmountSingular);
                } else {
                    notificationAmount.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationAmountPlural), sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
                }
            }

            notificationAmount.setOnPreferenceChangeListener((preference, newValue) -> {
                int value = Integer.parseInt((String)newValue);
                if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                    if (value == 1) {
                        notificationAmount.setSummary(R.string.settingsNotificationAmountSingular);
                    } else if (value >= 2 && value <= 4){
                        notificationAmount.setSummary(String.format(getString(R.string.settingsNotificationAmountFunky), sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
                    } else {
                        notificationAmount.setSummary(String.format(getString(R.string.settingsNotificationAmountPlural), sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
                    }
                } else {
                    if (value == 1) {
                        notificationAmount.setSummary(R.string.settingsNotificationAmountSingular);
                    } else {
                        notificationAmount.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationAmountPlural), newValue));
                    }
                }
                return true;
            });

            int inactivityTimer = Integer.parseInt(sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5"));
            if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                if (inactivityTimer == 1){
                    inactivityTimerPref.setSummary(R.string.settingsInactivityTimerSummarySingular);
                } else if (inactivityTimer >= 2 && inactivityTimer <= 4){
                    inactivityTimerPref.setSummary(String.format(getString(R.string.settingsInactivityTimerSummaryFunky), sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5")));
                } else {
                    inactivityTimerPref.setSummary(String.format(getString(R.string.settingsInactivityTimerSummaryPlural), sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5")));
                }
            } else {
                if (inactivityTimer == 1) {
                    inactivityTimerPref.setSummary(R.string.settingsInactivityTimerSummarySingular);
                } else {
                    inactivityTimerPref.setSummary(String.format(Locale.US, getString(R.string.settingsInactivityTimerSummaryPlural), sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5")));
                }
            }
            inactivityTimerPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                int temp = Integer.parseInt((String) newValue);
                if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                    if (temp == 1){
                        inactivityTimerPref.setSummary(R.string.settingsInactivityTimerSummarySingular);
                    } else if (temp >= 2 && temp <= 4){
                        inactivityTimerPref.setSummary(String.format(getString(R.string.settingsInactivityTimerSummaryFunky), newValue));
                    } else {
                        inactivityTimerPref.setSummary(String.format(getString(R.string.settingsInactivityTimerSummaryPlural), newValue));
                    }
                } else {
                    if (temp == 1) {
                        inactivityTimerPref.setSummary(R.string.settingsInactivityTimerSummarySingular);
                    } else {
                        inactivityTimerPref.setSummary(String.format(Locale.US, getString(R.string.settingsInactivityTimerSummaryPlural), newValue));
                    }
                }
                return true;
            }));

        } else {
           startCustomNotificationsScreen();
        }
    }

    private void initializeLanguageJunk(Context context){

    }



    public static boolean isUsageAccessGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void startCustomNotificationsScreen(){

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

    private void advancedPurchased(SharedPreferences sharedPreferences, PreferenceScreen preferenceScreen){
        advancedOptionsPurchased = true;
        sharedPreferences.edit().putBoolean(ADVANCED_PURCHASED_KEY, true).apply();
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setEnabled(false);
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setSummary(getResources().getString(R.string.settingsAdsDisabledSummary));
        preferenceScreen.findPreference("pref_advanced_options").setEnabled(true);
        sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply();
        preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).setEnabled(true);
        preferenceScreen.findPreference(SMART_NOTIFICATIONS_KEY).setEnabled(true);
        preferenceScreen.findPreference("pref_advanced_purchase").setSummary(getString(R.string.settingsAdvancedPurchasedSummary));
        if (sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false)){
            preferenceScreen.findPreference(NOTIF_AMOUNT_KEY).setEnabled(false);
        }
        if (sharedPreferences.getBoolean(DND_KEY, false)) {
            preferenceScreen.findPreference(DND_DELAY_KEY).setEnabled(true);
        }
    }

    private void advancedPurchasedError(SharedPreferences sharedPreferences, PreferenceScreen preferenceScreen){
        advancedOptionsPurchased = false;
        sharedPreferences.edit().putBoolean(ADVANCED_PURCHASED_KEY, false).apply();
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setEnabled(true);
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setSummary(R.string.settingsAdsEnabledSummary);
        preferenceScreen.findPreference("pref_advanced_purchase").setSummary(R.string.settingsAdvancedPurchaseSummary);
        if (!adsEnabled) {
            preferenceScreen.findPreference("pref_advanced_options").setEnabled(false);
            sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply();
            preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).setEnabled(false);
            preferenceScreen.findPreference(SMART_NOTIFICATIONS_KEY).setEnabled(false);
            preferenceScreen.findPreference(NOTIF_AMOUNT_KEY).setEnabled(true);
            preferenceScreen.findPreference(DND_DELAY_KEY).setEnabled(false);
        }
    }



    @Override
    public Fragment getCallbackFragment() {
        return this;
        // or you can return the parent fragment if it's handling the screen navigation,
        // however, in that case you need to traverse to the implementing parent fragment
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onResume(){
        Log.d("settings", "onResume called!");
        //COMPILE INSTRUCTIONS: comment out the following line
        bp.loadOwnedPurchasesFromGoogle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sharedPreferences.getBoolean(DND_KEY, false) && !notificationManager.isNotificationPolicyAccessGranted()){
                sharedPreferences.edit().putBoolean(DND_KEY, false).apply();
                desiredDoNotDisturbValue = false;
                Toast.makeText(getContext(), getString(R.string.settingsDnDPermDeniedToast), Toast.LENGTH_LONG).show();
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            } else if (notificationManager.isNotificationPolicyAccessGranted() && doNotDisturbSettingsOpened){
                desiredDoNotDisturbValue = true;
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            }
        }

        if (sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false) && !isUsageAccessGranted(getContext())) {
            sharedPreferences.edit().putBoolean(SMART_NOTIFICATIONS_KEY, false).apply();
            desiredSmartNotificationValue = false;
            Toast.makeText(getContext(), getString(R.string.settingsUsagePermDeniedToast), Toast.LENGTH_LONG).show();
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        } else if (isUsageAccessGranted(getContext()) && usageSettingsOpened){
            desiredSmartNotificationValue = true;
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        }

        super.onResume();
    }

    @Override
    public void onPause(){
        setNotifications(false, sharedPreferences.getBoolean(NOTIF_ENABLE_KEY, false),
                parseBedtime(sharedPreferences.getString(BEDTIME_KEY, "22:00")),
                Integer.parseInt(sharedPreferences.getString(NOTIF_DELAY_KEY, "10")),
                Integer.parseInt(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "1")), getContext());
        super.onPause();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (productId.equals("go_to_sleep_advanced")) {
            Log.d("productPurchased", "go to sleep advanced purchased");
            advancedPurchased(sharedPreferences, getPreferenceScreen());
        }

    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (bp.isPurchased("go_to_sleep_advanced")) {
            advancedPurchased(sharedPreferences, getPreferenceScreen());
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        advancedPurchasedError(sharedPreferences, getPreferenceScreen());
    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

}

