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
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

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
        bp = new BillingProcessor(Objects.requireNonNull(getContext()), getResources().getString(R.string.license_key), this);
        bp.loadOwnedPurchasesFromGoogle();
        bp.initialize();


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



            advancedOptionsPurchased = sharedPreferences.getBoolean(ADVANCED_PURCHASED_KEY, false);
            adsEnabled = sharedPreferences.getBoolean(ADS_ENABLED_KEY, false);
            smartNotificationsEnabled = sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false);


            if (advancedOptionsPurchased) {
               adsEnabledPref.setEnabled(false);
               adsEnabledPref.setSummary("Ads are disabled, thank you for your support.");

               getPreferenceScreen().findPreference("pref_advanced_options").setEnabled(true);
               getPreferenceManager().getSharedPreferences().edit().putBoolean(ADS_ENABLED_KEY, false).apply();
               getPreferenceScreen().findPreference(CUSTOM_NOTIFICATIONS_KEY).setEnabled(true);
               delayDnDPref.setEnabled(sharedPreferences.getBoolean(DND_KEY, false));
               smartNotificationsPref.setEnabled(true);
               getPreferenceScreen().findPreference("pref_advanced_purchase").setSummary("Thank you for supporting me!");
            }

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
                                Toast.makeText(getContext(), "Consent preferences cleared... Go back to main screen to edit", Toast.LENGTH_SHORT).show();
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


                final Preference advancedPurchasePref = this.findPreference("pref_advanced_purchase");
                advancedPurchasePref.setOnPreferenceClickListener(preference -> {
                    bp.purchase(getActivity(), "go_to_sleep_advanced");




                    return false;
                });





            enableAdvancedOptions = advancedOptionsPurchased || adsEnabled;
            if (enableAdvancedOptions) {
                notificationAmount.setEnabled(!smartNotificationsEnabled);
            }



            Preference bedtime = this.findPreference(BEDTIME_KEY);
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                Date time = simpleDateFormat.parse(sharedPreferences.getString(BEDTIME_KEY, "19:35"));
                bedtime.setSummary("Bedtime is " +  DateFormat.getTimeInstance(DateFormat.SHORT).format(time));
            } catch (ParseException e) {
                e.printStackTrace();
                bedtime.setSummary("Bedtime is " + sharedPreferences.getString(BEDTIME_KEY, "19:35"));
            }

            smartNotificationsPref.setEnabled(enableAdvancedOptions);
            delayDnDPref.setEnabled(enableAdvancedOptions);
            if (enableAdvancedOptions){
                smartNotificationsPref.setSummary("Send notifications until you stop using your phone");
            }
            customNotificationsPref.setEnabled(enableAdvancedOptions);


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

            if (sharedPreferences.getString(DND_DELAY_KEY, "2").equals("1")){
                autoDnDPref.setSummary("Automatically enable Do not Disturb 1 minute after the last bedtime reminder is sent");
                delayDnDPref.setSummary("Do not Disturb will be activated 1 minute after the last notification is sent");
            } else {
                autoDnDPref.setSummary("Automatically enable Do not Disturb " + sharedPreferences.getString(DND_DELAY_KEY, "2") + " minutes after the last bedtime reminder is sent");
                delayDnDPref.setSummary("Do not Disturb will be activated " + sharedPreferences.getString(DND_DELAY_KEY, "2") + " minutes after the last notification is sent");
            }

            delayDnDPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((newValue).equals("1")){
                    autoDnDPref.setSummary("Automatically enable Do not Disturb 1 minute after the last bedtime reminder is sent");
                    delayDnDPref.setSummary("Do not Disturb will be activated 1 minute after the last notification is sent");
                } else {
                    autoDnDPref.setSummary("Automatically enable Do not Disturb " + newValue + " minutes after the last bedtime reminder is sent");
                    delayDnDPref.setSummary("Do not Disturb will be activated " + newValue + " minutes after the last notification is sent");
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
                this.findPreference(DND_KEY).setSummary("Android 6.0 (Marshmallow) and up required");
            } else if (android.os.Build.MANUFACTURER.equalsIgnoreCase("lg") && android.os.Build.MODEL.equalsIgnoreCase("g4")){
                this.findPreference(DND_KEY).setEnabled(false);
                this.findPreference(DND_KEY).setSummary("LG G4 is not compatible (LG didn't implement the API correctly)");
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

            notificationDelay.setSummary(sharedPreferences.getString(NOTIF_DELAY_KEY, "15") + " minute delay between sleep notifications");
            notificationDelay.setOnPreferenceChangeListener((preference, newValue) -> {
                notificationDelay.setSummary(newValue + " minute delay between sleep notifications");
                return true;
            });

            if (sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3").equals("1")){
                notificationAmount.setSummary("1 sleep reminder will be sent");
            } else {
                notificationAmount.setSummary(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3") + " sleep reminders will be sent");
            }
            notificationAmount.setOnPreferenceChangeListener((preference, newValue) -> {
                if (((String) newValue).equals("1")){
                    notificationAmount.setSummary("1 sleep reminder will be sent");
                } else {
                    notificationAmount.setSummary(newValue + " sleep reminders will be sent");
                }
                return true;
            });

            if (Integer.parseInt(sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5")) == 1){
                inactivityTimerPref.setSummary("User must be inactive for 1 minute to be considered inactive");
            } else {
                inactivityTimerPref.setSummary("User must be inactive for " + sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5") + " minutes to be considered inactive");
            }
            inactivityTimerPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                if (Integer.parseInt((String) newValue) == 1){
                    inactivityTimerPref.setSummary("User must be inactive for 1 minute to be considered inactive");
                } else {
                    inactivityTimerPref.setSummary("User must be inactive for " + newValue + " minutes to be considered inactive");
                }
                return true;
            }));

        } else {
           startCustomNotificationsScreen();
        }
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
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setSummary("Ads are disabled, thank you for supporting me!");
        preferenceScreen.findPreference("pref_advanced_options").setEnabled(true);
        sharedPreferences.edit().putBoolean(ADS_ENABLED_KEY, false).apply();
        preferenceScreen.findPreference(CUSTOM_NOTIFICATIONS_KEY).setEnabled(true);
        preferenceScreen.findPreference(SMART_NOTIFICATIONS_KEY).setEnabled(true);
        preferenceScreen.findPreference("pref_advanced_purchase").setSummary("Thank you for supporting me!");
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
        preferenceScreen.findPreference(ADS_ENABLED_KEY).setSummary("Unlock advanced options without paying by showing ads (ads will never be shown outside of the app) ");
        preferenceScreen.findPreference("pref_advanced_purchase").setSummary("Support me and unlock all advanced options without advertisements for life");
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
        bp.loadOwnedPurchasesFromGoogle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sharedPreferences.getBoolean(DND_KEY, false) && !notificationManager.isNotificationPolicyAccessGranted()){
                sharedPreferences.edit().putBoolean(DND_KEY, false).apply();
                desiredDoNotDisturbValue = false;
                Toast.makeText(getContext(), "Do not disturb access not granted, toggle option to try again", Toast.LENGTH_LONG).show();
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            } else if (notificationManager.isNotificationPolicyAccessGranted() && doNotDisturbSettingsOpened){
                desiredDoNotDisturbValue = true;
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            }
        }

        if (sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false) && !isUsageAccessGranted(getContext())) {
            sharedPreferences.edit().putBoolean(SMART_NOTIFICATIONS_KEY, false).apply();
            desiredSmartNotificationValue = false;
            Toast.makeText(getContext(), "Usage access not granted, toggle option to try again", Toast.LENGTH_LONG).show();
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        } else if (isUsageAccessGranted(getContext()) && usageSettingsOpened){
            desiredSmartNotificationValue = true;
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        }

        super.onResume();
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

