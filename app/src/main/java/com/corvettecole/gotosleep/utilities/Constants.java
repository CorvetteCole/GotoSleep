package com.corvettecole.gotosleep.utilities;

public class Constants {

    public static final String BEDTIME_CHANNEL_ID = "bedtimeReminders";
    public static final int BACK_INTERVAL = 2000;
    public static final String[] supportedLanguages = {"en", "pl", "es", "de", "it", "fi", "fr", "cs", "el", "fi", "pt", "sv"};

    public static final String APP_LAUNCHED_KEY = "numLaunched";
    public static final String RATING_PROMPT_SHOWN_KEY = "rate_shown";
    public static final String PURCHASE_PROMPT_SHOWN_KEY = "purchase_prompt_shown";
    public static final String LOCALIZATION_PROMPT_SHOWN_KEY = "localization_prompt_shown";

    public static final int FIRST_NOTIFICATION_ALARM_REQUEST_CODE = 1;
    public static final int NOTIFICATION_REQUEST_CODE = 2;
    public static final int DO_NOT_DISTURB_REQUEST_CODE = 3;
    public static final int DO_NOT_DISTURB_ALARM_REQUEST_CODE = 4;
    public static final int NEXT_NOTIFICATION_ALARM_REQUEST_CODE = 5;
    public static final int LAUNCH_APP_REQUEST_CODE = 6;
    public static final String LAST_NOTIFICATION_KEY = "lastNotificationTime";
    public static final long ONE_MINUTE_MILLIS = 60000;
    public static final long ONE_DAY_MILLIS = 86400000;
    public static final long ONE_HOUR_MILLIS = 3600000;
    public static final String CURRENT_NOTIFICATION_KEY = "current_notification";

    public static final String NOTIF_DELAY_KEY = "pref_notificationDelay";
    public static final String NOTIF_AMOUNT_KEY = "pref_numNotifications";
    public static final String NOTIF_ENABLE_KEY = "pref_notificationsEnabled";
    public static final String BEDTIME_KEY = "pref_bedtime";
    public static final String DND_KEY = "pref_autoDoNotDisturb";
    public static final String BUTTON_HIDE_KEY = "pref_buttonHide";
    public static final String NOTIFICATION_1_KEY = "pref_notification1";
    public static final String NOTIFICATION_2_KEY = "pref_notification2";
    public static final String NOTIFICATION_3_KEY = "pref_notification3";
    public static final String NOTIFICATION_4_KEY = "pref_notification4";
    public static final String NOTIFICATION_5_KEY = "pref_notification5";
    public static final String CUSTOM_NOTIFICATIONS_KEY = "pref_customNotifications_category";
    public static final String SMART_NOTIFICATIONS_KEY = "pref_smartNotifications";
    public static final String ADS_ENABLED_KEY = "pref_adsEnabled";
    public static final String ADVANCED_PURCHASED_KEY = "advanced_options_purchased";
    public static final String INACTIVITY_TIMER_KEY = "pref_activityMargin";
    public static final String GDPR_KEY = "pref_gdpr";
    public static final String DND_DELAY_KEY = "pref_dndDelay";
    public static final String NOTIFICATION_SOUND_KEY = "pref_notificationSound";
    public static final String ADDITIONAL_NOTIFICATION_SETTINGS_KEY = "pref_notificationChannel";
    public static final String SEND_ONE_NOTIFICATION = "pref_sendOneNotification";


}
