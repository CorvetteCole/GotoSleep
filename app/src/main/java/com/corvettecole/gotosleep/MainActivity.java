package com.corvettecole.gotosleep;

import android.animation.LayoutTransition;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.FIRST_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.ONE_DAY_MILLIS;
import static com.corvettecole.gotosleep.BedtimeNotificationReceiver.ONE_MINUTE_MILLIS;
import static com.corvettecole.gotosleep.SettingsFragment.ADS_ENABLED_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.ADVANCED_PURCHASED_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.BUTTON_HIDE_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.DND_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.INACTIVITY_TIMER_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_AMOUNT_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_DELAY_KEY;
import static com.corvettecole.gotosleep.SettingsFragment.NOTIF_ENABLE_KEY;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    static final String BEDTIME_CHANNEL_ID = "bedtimeNotifications";
    private static final int BACK_INTERVAL = 2000;
    private long backPressed;
    private Button settingsButton;
    private Button aboutButton;
    private Button editBedtimeButton;

    private Calendar bedtimeCal;
    private int[] bedtime;

    private BroadcastReceiver _broadcastReceiver;
    private TextView hours;
    private TextView minutes;
    private TextView sleepMessage;
    private Button enableSleepmodeButton;
    private View contentMain;

    private boolean isFirstStart;
    private boolean isSecondStart;
    private boolean adsEnabled;
    static int bedtimePastTrigger = 8;
    static boolean buttonHide = false;
    private final String TAG = "MainActivity";
    private boolean notificationsEnabled;

    static String[] notifications = new String[5];
    private int currentNotification;
    private int numNotifications;
    private int notificationDelay;

    private boolean advancedOptionsPurchased;
    private BillingProcessor bp;

    private NotificationManager notificationManager;
    private AdView adView;

    private boolean adsLoaded = false;
    private boolean isAutoDoNotDisturbEnabled;

    private Button rateYesButton;
    private Button rateNoButton;
    private TextView rateTextView;
    private ConstraintLayout rateLayout;

    private boolean isRequestingFeedback = false;
    private boolean isRequestingRating = false;

    final static String APP_LAUNCHED_KEY = "numLaunched";
    private int appLaunched;
    final static String RATING_PROMPT_SHOWN_KEY = "rateShown";
    private boolean ratingPromptShown;

    private SharedPreferences getPrefs;

    /*private UsageStatsManager usageStatsManager;
    private Button usageButton;
    private int userActiveMargin;
    */

    @Override
    public void onStart() {
        super.onStart();
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (Objects.requireNonNull(intent.getAction()).compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    updateCountdown();
                }
            }
        };

        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if (backPressed + BACK_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast toast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT);
            toast.show();
        }
        backPressed = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPreferences();

        setNotifications(); //Warning: takes a long time to execute (55ms!)

        updateCountdown();

        if (!adsLoaded) {
            enableDisableAds();
        }
        if (ratingPromptShown && rateLayout.getVisibility() == View.VISIBLE) {
            rateLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        //  Create a new boolean and preference and set it to true
        isFirstStart = getPrefs.getBoolean("firstStart", true);
        isSecondStart = getPrefs.getBoolean("secondStart", true);



        //  If the activity has never started before...
        if (isFirstStart) {

            //  Launch app slide1
            final Intent intro = new Intent(MainActivity.this, IntroActivity.class);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startActivity(intro);
                }
            });

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();
            //this is needed to stop weird back button stuff
            finish();
        } else {

            setContentView(R.layout.activity_main);
            adView = findViewById(R.id.adView);
            settingsButton = findViewById(R.id.settingsButton);
            editBedtimeButton = findViewById(R.id.bedtimeSetButton);
            aboutButton = findViewById(R.id.aboutButton);
            hours = findViewById(R.id.hours);
            minutes = findViewById(R.id.minutes);
            sleepMessage = findViewById(R.id.sleepMessage);
            contentMain = findViewById(R.id.content_main_layout);
            enableSleepmodeButton = findViewById(R.id.enableSleepModeButton);
            rateLayout = findViewById(R.id.rate_layout);
            rateNoButton = findViewById(R.id.rateNoButton);
            rateYesButton = findViewById(R.id.rateYesButton);
            rateTextView = findViewById(R.id.rateText);



            bp = new BillingProcessor(this, getResources().getString(R.string.license_key), this);
            bp.initialize();
            bp.loadOwnedPurchasesFromGoogle();
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            createNotificationChannel();

            loadPreferences();




            //#TODO add in additional parameter requiring an amount of time to have passed
            if (appLaunched < 10 && !ratingPromptShown) {
                rateLayout.setVisibility(View.GONE);
                Log.d(TAG, "appLaunched: " + appLaunched);
                getPrefs.edit().putInt(APP_LAUNCHED_KEY, appLaunched + 1).apply();
                //initiateRatingDialogue(getPrefs); //debug
            } else if (!ratingPromptShown) {
                Log.d(TAG, "initiating rating dialogue");
                initiateRatingDialogue();
            } else {
               rateLayout.setVisibility(View.GONE);
               //initiateRatingDialogue(getPrefs);  //debug
            }



            //runs when the intro slides launch mainActivity again
            final Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            final Intent about = new Intent(MainActivity.this, AboutActivity.class);

            if (isSecondStart) {
                editBedtimeButton.setVisibility(View.VISIBLE);
                editBedtimeButton.setOnClickListener(view -> startActivity(settings));
                SharedPreferences.Editor e = getPrefs.edit();
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("secondStart", false);
                //  Apply changes
                e.apply();
            }


            settingsButton.setOnClickListener(view -> startActivity(settings));

            enableSleepmodeButton.setOnClickListener(v -> {
                Intent snoozeIntent = new Intent(getApplicationContext(), AutoDoNotDisturbReceiver.class);
                PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 11, snoozeIntent, 0);
                try {
                    snoozePendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                enableSleepmodeButton.setVisibility(View.GONE);
            });

            aboutButton.setOnClickListener(view -> startActivity(about));




            contentMain.setOnClickListener(v -> {
                if (settingsButton.getVisibility() == View.VISIBLE && buttonHide) {
                    settingsButton.setVisibility(View.INVISIBLE);
                    aboutButton.setVisibility(View.INVISIBLE);
                } else {
                    settingsButton.setVisibility(View.VISIBLE);
                    aboutButton.setVisibility(View.VISIBLE);
                }
            });



            MobileAds.initialize(this, getResources().getString(R.string.admob_key));
            enableDisableAds();




        }
    }

    private void initiateRatingDialogue(){


        rateLayout.setVisibility(View.VISIBLE);
        rateLayout.invalidate();
        Log.d(TAG, "set rateLayout to visible");
        //initial state, TextView displays "Are you enjoying Go to Sleep?"

        ((ViewGroup) findViewById(R.id.rate_layout)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        rateNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRequestingFeedback && !isRequestingRating) {
                    isRequestingFeedback = true;
                    rateTextView.setText("Would you mind sending some feedback?");
                    rateNoButton.setText("No, thanks");
                    rateYesButton.setText("Ok, sure");
                } else {
                    rateLayout.setVisibility(View.GONE);
                }
                getPrefs.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply();
            }
        });

        rateYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRequestingRating && !isRequestingFeedback){
                    isRequestingRating = true;
                    rateTextView.setText("How about rating the app then?");
                    rateYesButton.setText("Ok, sure!");
                    rateNoButton.setText("No, thanks");
                } else if (isRequestingFeedback){
                    //#TODO user said yes to sending feedback
                    sendFeedback();

                } else {
                    //#TODO user said yes to rating the app
                    sendToPlayStore();
                }
                getPrefs.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply();
            }
        });


    }

    private void sendToPlayStore(){
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
        {
            startActivity(rateAppIntent);
        }
        else
        {
            /* handle your error case: the device has no way to handle market urls */
        }
    }

    private void sendFeedback(){
        String subject = "Go to Sleep Feedback";
        String bodyText = "Please explain your bug or feature suggestion thoroughly";
        String mailto = "mailto:corvettecole@gmail.com" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(bodyText);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
        }
    }

    private void setNotifications() {
        if (notificationsEnabled) {
            Calendar bedtimeCalendar = getBedtimeCal(bedtime);

            if (bedtimeCalendar.getTimeInMillis() < System.currentTimeMillis()){
                bedtimeCalendar.setTimeInMillis(bedtimeCalendar.getTimeInMillis() + ONE_DAY_MILLIS);
            }

            int errorMargin = 30;
            if (currentNotification != 1){
                if (abs(System.currentTimeMillis() - bedtimeCal.getTimeInMillis()) > ((notificationDelay * numNotifications + errorMargin) * 60000 )){
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                    currentNotification = 1;
                }
            }

            Intent intent1 = new Intent(this, BedtimeNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtimeCalendar.getTimeInMillis(), pendingIntent);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, bedtimeCalendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void loadPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.d("MainActivity", "Load Preferences Ran");
        bedtime = parseBedtime(settings.getString(BEDTIME_KEY, "19:35"));

        buttonHide = settings.getBoolean(BUTTON_HIDE_KEY, false);
        notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true);
        bedtimeCal = getBedtimeCal(bedtime);
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1);
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3 + ""));
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15 + ""));
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false);
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false);
        isAutoDoNotDisturbEnabled = settings.getBoolean(DND_KEY, false);
        if (isAutoDoNotDisturbEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings.edit().putBoolean(DND_KEY, notificationManager.isNotificationPolicyAccessGranted()).apply();
            }
        }
        advancedOptionsPurchased = bp.isPurchased("go_to_sleep_advanced");
        ratingPromptShown = settings.getBoolean(RATING_PROMPT_SHOWN_KEY, false);
        appLaunched = settings.getInt(APP_LAUNCHED_KEY, 0);

        settings.edit().putBoolean(ADVANCED_PURCHASED_KEY, advancedOptionsPurchased).apply();
    }



    static int[] parseBedtime(String bedtime){
        int bedtimeHour = Integer.parseInt(bedtime.substring(0, bedtime.indexOf(":")));
        int bedtimeMin = Integer.parseInt(bedtime.substring(bedtime.indexOf(":") + 1, bedtime.length()));
        return new int[]{bedtimeHour, bedtimeMin};
    }

    static Calendar getBedtimeCal (int[] bedtime){
        String TAG = "getBedtimeCal";
        Log.d(TAG, "bedtime[0], bedtime[1] " + bedtime[0] + "," + bedtime[1]);
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, bedtime[0]);
        calendar.set(Calendar.MINUTE, bedtime[1]);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(BEDTIME_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void enableDisableAds(){
        if (adsEnabled && adView.getVisibility() == View.GONE) {
            Log.d(TAG, "enableDisableAds initialized");
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                    .build();
            adView.loadAd(adRequest);

        } else if (adView.getVisibility() != View.GONE && !adsEnabled){

            adView.setVisibility(View.GONE);
        }
    }

    /*
    private void testUsageStats(){
        String TAG = "testUsageStats";
        userActiveMargin = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(INACTIVITY_TIMER_KEY, "5"));

        long startTime = System.currentTimeMillis() - notificationDelay * ONE_MINUTE_MILLIS;

        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, System.currentTimeMillis());

        UsageStats minUsageStat = queryUsageStats.get(0);

        long min = Long.MAX_VALUE;
        for (UsageStats usageStat : queryUsageStats){
            if (usageStat.getLastTimeUsed() < min && usageStat.getTotalTimeInForeground() > ONE_MINUTE_MILLIS){
                minUsageStat = usageStat;
            }
        }


        Log.d(TAG, "current time " + System.currentTimeMillis());
        Log.d(TAG, "last activity " + minUsageStat.getPackageName() + " time in foreground " + minUsageStat.getTotalTimeInForeground() + " time last used " + minUsageStat.getLastTimeStamp());

        long difference = System.currentTimeMillis() - minUsageStat.getLastTimeStamp();

        if (System.currentTimeMillis() - minUsageStat.getLastTimeStamp() <=  userActiveMargin * ONE_MINUTE_MILLIS){
            Log.d(TAG, "user is active, last activity " + difference/ONE_MINUTE_MILLIS + " minutes ago");
        } else {
            Log.d(TAG, "user is inactive, last activity " + difference/ONE_MINUTE_MILLIS + " minutes ago");
        }
    }
    */

    private void updateCountdown() {
        if (!isFirstStart){

            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(System.currentTimeMillis());

            Date endDate;
            Date startDate;
            boolean present = false;



            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

            Log.d("updateCountdown", current.get(Calendar.SECOND) + "");
            current.set(Calendar.SECOND, 0);

            startDate = bedtimeCal.getTime();
            endDate = current.getTime();

            Log.d("updateCountdown", bedtimeCal.getTime() + " bedtime");

            Log.d("updateCountdown", current.getTime() + " current time");


            long difference = endDate.getTime() - startDate.getTime();
            if (difference < 0)
            {
                try {
                    Date dateMax = simpleDateFormat.parse("24:00");
                    Date dateMin = simpleDateFormat.parse("00:00");
                    difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
                } catch (ParseException e){
                    Log.e("UpdateCountdown", e + "");
                }
            }
            int day = (int)(difference / (1000*60*60*24));
            int hour = (int)((difference - (1000*60*60*24*day)) / (1000*60*60));
            int min = Math.round((difference - (1000*60*60*24*day) - (1000*60*60*hour)) / (float)(1000*60));
            Log.i("updateCountdown","Days: " + day + " Hours: "+ hour+", Mins: "+ min);

            if (hour >= bedtimePastTrigger) {
                difference = (difference - 86400000) * -1;
                present = true;
                day = (int) (difference / (1000 * 60 * 60 * 24));
                hour = (int) ((difference - (1000 * 60 * 60 * 24 * day)) / (1000 * 60 * 60));
                min = Math.round((difference - (1000 * 60 * 60 * 24 * day) - (1000 * 60 * 60 * hour)) / (float) (1000 * 60));
                Log.i("updateCountdown", "Days: " + day + " Hours: " + hour + ", Mins: " + min);
            }

            if (min == 60){  //because minutes are being rounded for accuracy reasons, this is needed to correct for minor errors
                min = 0;
                hour++;
            }


            if (hour == 1){
                hours.setText(hour + " hour");

            } else {
                hours.setText(hour + " hours");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (present && editBedtimeButton.getVisibility() == View.GONE && ((hour * 60) + min) <= 90 && notificationManager.isNotificationPolicyAccessGranted() && notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS){
                    enableSleepmodeButton.setVisibility(View.VISIBLE);
                } else if (enableSleepmodeButton.getVisibility() != View.GONE) {
                    enableSleepmodeButton.setVisibility(View.GONE);
                }
            }

            if (present) {
                if (min == 1){
                    minutes.setText(min + " minute until bedtime");
                } else {
                    minutes.setText(min + " minutes until bedtime");
                }
                sleepMessage.setVisibility(View.INVISIBLE);
            } else {
                if (min == 1){
                    minutes.setText(min + " minute past bedtime");
                } else {
                    minutes.setText(min + " minutes past bedtime");
                }
                if (editBedtimeButton.getVisibility() != View.VISIBLE) {
                    sleepMessage.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
