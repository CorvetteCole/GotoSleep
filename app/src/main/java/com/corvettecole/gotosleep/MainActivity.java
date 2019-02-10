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

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.WorkManager;

import static com.corvettecole.gotosleep.AboutActivity.EGG_KEY;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.corvettecole.gotosleep.utilities.BedtimeUtilities.parseBedtime;
import static com.corvettecole.gotosleep.utilities.Constants.ADS_ENABLED_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.ADVANCED_PURCHASED_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.APP_LAUNCHED_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.BACK_INTERVAL;
import static com.corvettecole.gotosleep.utilities.Constants.BEDTIME_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.BUTTON_HIDE_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.CURRENT_NOTIFICATION_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.DND_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.LOCALIZATION_PROMPT_SHOWN_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_AMOUNT_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_DELAY_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.NOTIF_ENABLE_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.PURCHASE_PROMPT_SHOWN_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.RATING_PROMPT_SHOWN_KEY;
import static com.corvettecole.gotosleep.utilities.Constants.supportedLanguages;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.cancelNextNotification;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.createNotificationChannel;
import static com.corvettecole.gotosleep.utilities.NotificationUtilites.setNotifications;

public class MainActivity extends AppCompatActivity implements NativeDialogPrompt.OnFragmentInteractionListener {

    private boolean workManagerInitialized = false;


    private long backPressed;
    private Button settingsButton;
    private Button aboutButton;
    private Button editBedtimeButton;

    private Calendar bedtimeCal;
    private int[] bedtime;

    private BroadcastReceiver _broadcastReceiver;
    private ImageView moon;
    private TextView countdownHoursTextView;
    private TextView countdownMinutesTextView;
    private TextView sleepMessage;
    private Button enableSleepmodeButton;
    private View contentMain;

    private boolean isFirstStart;
    private boolean isSecondStart;
    private boolean adsEnabled;

    private final String TAG = "MainActivity";
    private boolean notificationsEnabled;

    private String[] notifications = new String[5];
    private int currentNotification;
    private int numNotifications;
    private int notificationDelay;

    private boolean advancedOptionsPurchased;

    private NotificationManager notificationManager;

    private boolean adsLoaded = false;
    private boolean adsInitialized = false;
    private boolean isAutoDoNotDisturbEnabled;

    private FrameLayout nativeDialogFrame;

    private boolean isRequestingFeedback = false;
    private boolean isRequestingRating = false;


    private int appLaunchedPortrait;

    static int bedtimePastTrigger = 8;
    static boolean buttonHide = false;

    private boolean ratingPromptShown;
    private boolean localizationPromptShown;
    private boolean purchasePromptShown;


    private SharedPreferences getPrefs;

    static boolean shouldUpdateConsent = false;

    private boolean sleepModeEnabled = false;

    private boolean editBedtimeClicked = false;

    private boolean egg = false;

    private int colorFadeDuration = 6000;

    private ArrayList<ValueAnimator> colorAnimations = new ArrayList<>();

    private NativeDialogPrompt nativeDialogPrompt;

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
            Toast toast = Toast.makeText(this, getString(R.string.back_pressed), Toast.LENGTH_SHORT);
            toast.show();
        }
        backPressed = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called " + System.currentTimeMillis());

        loadPreferences();

        try {
            androidx.work.Configuration configuration = new androidx.work.Configuration.Builder().build();
            WorkManager.initialize(this, configuration);
        } catch (IllegalStateException e) {
            Log.e("workManagerException", e.toString());
        }

        setNotifications(false, notificationsEnabled, bedtime, notificationDelay, numNotifications, this); //Warning: takes a long time to execute (55ms!)

        updateCountdown();

            if (!egg) {
                clearEgg();
            } else {
                setEgg();
            }

        if (editBedtimeClicked){
            editBedtimeButton.setVisibility(View.GONE);
            editBedtimeClicked = false;
        }

        if (!adsInitialized || shouldUpdateConsent) {
            enableDisableAds();
        }
        if ((ratingPromptShown || purchasePromptShown || localizationPromptShown) && nativeDialogFrame.getVisibility() == View.VISIBLE) {
            //#TODO nativeDialogFrame.setVisibility(View.GONE);
        }
        Log.d(TAG, "onResume finished " + System.currentTimeMillis());

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called " + System.currentTimeMillis());

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

            runOnUiThread(() -> startActivity(intro));

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();
            //this is needed to stop weird back button stuff
            finish();
        } else {
            //runs when the intro slides launch mainActivity again
            setContentView(R.layout.activity_main);
            settingsButton = findViewById(R.id.settingsButton);
            editBedtimeButton = findViewById(R.id.bedtimeSetButton);
            aboutButton = findViewById(R.id.aboutButton);
            moon = findViewById(R.id.moon);
            countdownHoursTextView = findViewById(R.id.hours);
            countdownMinutesTextView = findViewById(R.id.minutes);
            sleepMessage = findViewById(R.id.sleepMessage);
            contentMain = findViewById(R.id.content_main_layout);
            enableSleepmodeButton = findViewById(R.id.enableSleepModeButton);
            nativeDialogFrame = findViewById(R.id.native_dialog_frame);


            for (int i = 0; i < 10; i++){
                int colorFrom = getResources().getColor(R.color.moonPrimary);
                int colorTo = getResources().getColor(R.color.indigo);
                colorAnimations.add(ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
            }


            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            createNotificationChannel(getBaseContext());
            loadPreferences();

            initializeDialogs();
            enableDisableAds();

            final Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            final Intent about = new Intent(MainActivity.this, AboutActivity.class);

            if (isSecondStart) {
                editBedtimeButton.setVisibility(View.VISIBLE);
                editBedtimeButton.setOnClickListener(view -> {
                    startActivity(settings);
                    editBedtimeClicked = true;
                });
                SharedPreferences.Editor e = getPrefs.edit();
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("secondStart", false);
                //  Apply changes
                e.apply();
            }


            settingsButton.setOnClickListener(view -> startActivity(settings));

            enableSleepmodeButton.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (notificationManager.isNotificationPolicyAccessGranted() && notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                        Intent snoozeIntent = new Intent(getApplicationContext(), AutoDoNotDisturbReceiver.class);
                        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 11, snoozeIntent, 0);
                        try {
                            snoozePendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
                cancelNextNotification(this);
                setNotifications(true, notificationsEnabled, bedtime, notificationDelay, numNotifications, this);
                Toast.makeText(this, getString(R.string.sleepModeButtonToast), Toast.LENGTH_LONG).show();
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

            Log.d(TAG, "onCreate finished " + System.currentTimeMillis());
        }
    }

    private void initializeDialogs() {
        final String TAG = "initializeDialogs";
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            appLaunchedPortrait++;
            getPrefs.edit().putInt(APP_LAUNCHED_KEY, appLaunchedPortrait).apply();

            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            SharedPreferences.Editor e = getPrefs.edit();
            NativeDialogPrompt nativeDialogPrompt;
            // if statements to choose what kind of dialog to show
            Log.d(TAG, Locale.getDefault().getLanguage());
            if (!Arrays.asList(supportedLanguages).contains(Locale.getDefault().getLanguage()) && !localizationPromptShown){
                nativeDialogPrompt = NativeDialogPrompt.newInstance(
                        new String[][]{{"https://crowdin.com/project/go-to-sleep"}},
                        new String[][]{{"dismiss"}},
                        new String[][]{{getString(R.string.translation_request)}}
                );
                e.putBoolean(LOCALIZATION_PROMPT_SHOWN_KEY, true).apply();
            } else {
                nativeDialogFrame.setVisibility(View.GONE);
                e.apply();
                return;
            }
            this.nativeDialogPrompt = nativeDialogPrompt;
            loadPreferences();
            nativeDialogFrame.setVisibility(View.VISIBLE);
            Log.d(TAG,"launching nativeDialogPrompt");
            transaction.replace(R.id.native_dialog_frame, nativeDialogPrompt);
            transaction.commit();
        }
    }

    private void clearEgg(){
        //figure out how to do this
        boolean temp = egg;
        boolean eggCancel = !egg; //if egg is enabled, eggCancel will be false
        egg = false;
        moon.clearAnimation();
        for (ValueAnimator colorAnimation : colorAnimations) {
            colorAnimation.end();
        }
        if (eggCancel) {
            //moon.setColorFilter(getResources().getColor(R.color.moonPrimary));
        }
        egg = temp;
    }

    private void setEgg(){
        moon.setBackground(getDrawable(R.color.transparent));
        int colorFrom = getResources().getColor(R.color.moonPrimary);
        int colorTo = getResources().getColor(R.color.indigo);
        clearEgg();
        ValueAnimator colorAnimation;
        colorAnimations.set(0, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(0);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (egg) {
                    indigoToViolet();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void redToOrange(){
        int colorFrom = getResources().getColor(R.color.red);
        int colorTo = getResources().getColor(R.color.orange);

        ValueAnimator colorAnimation;
        colorAnimations.set(1, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(1);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
              if (egg) {
                  orangeToYellow();
              }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void orangeToYellow(){
        int colorFrom = getResources().getColor(R.color.orange);
        int colorTo = getResources().getColor(R.color.yellow);

        ValueAnimator colorAnimation;
        colorAnimations.set(2, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(2);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
               if (egg) {
                   yellowToGreen();
               }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void yellowToGreen(){
        int colorFrom = getResources().getColor(R.color.yellow);
        int colorTo = getResources().getColor(R.color.green);

        ValueAnimator colorAnimation;
        colorAnimations.set(3, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(3);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (egg) {
                    greenToBlue();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void greenToBlue(){
        int colorFrom = getResources().getColor(R.color.green);
        int colorTo = getResources().getColor(R.color.blue);

        ValueAnimator colorAnimation;
        colorAnimations.set(4, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(4);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (egg) {
                    blueToIndigo();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void blueToIndigo(){
        int colorFrom = getResources().getColor(R.color.blue);
        int colorTo = getResources().getColor(R.color.indigo);

        ValueAnimator colorAnimation;
        colorAnimations.set(5, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(5);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (egg) {
                    indigoToViolet();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void indigoToViolet(){
        int colorFrom = getResources().getColor(R.color.indigo);
        int colorTo = getResources().getColor(R.color.deep_purple);

        ValueAnimator colorAnimation;
        colorAnimations.set(6, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(6);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (egg) {
                    purpleToRed();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();

    }

    private void purpleToRed(){
        int colorFrom = getResources().getColor(R.color.deep_purple);
        int colorTo = getResources().getColor(R.color.red);

        ValueAnimator colorAnimation;
        colorAnimations.set(7, ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo));
        colorAnimation = colorAnimations.get(7);

        colorAnimation.setDuration(colorFadeDuration); // milliseconds
        colorAnimation.addUpdateListener(animator -> moon.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
               if (egg) {
                   redToOrange();
               }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.end();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        colorAnimation.start();
    }

    private void sendToPlayStore(){
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
        {
            startActivity(rateAppIntent);
            rateAppIntent.toString();
        }
        else
        {
            /* handle your error case: the device has no way to handle market urls */
        }
    }

    private String sendFeedback(){
        String subject = "Go to Sleep Feedback";
        String bodyText = getString(R.string.feedbackBodyText);
        String mailto = "mailto:corvettecole@gmail.com" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(bodyText);

        return mailto;
    }



    private void loadPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.d("MainActivity", "Load Preferences Ran");
        bedtime = parseBedtime(settings.getString(BEDTIME_KEY, "22:00"));

        buttonHide = settings.getBoolean(BUTTON_HIDE_KEY, false);
        notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true);
        bedtimeCal = getBedtimeCal(bedtime);
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1);
        try {
            numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3 + ""));
            notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15 + ""));
        } catch (NumberFormatException e){
            Log.e(TAG, e.toString());
            numNotifications = 3;
            notificationDelay = 15;
        }
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false);
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false);
        isAutoDoNotDisturbEnabled = settings.getBoolean(DND_KEY, false);
        if (isAutoDoNotDisturbEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings.edit().putBoolean(DND_KEY, notificationManager.isNotificationPolicyAccessGranted()).apply();
            }
        }

        purchasePromptShown = settings.getBoolean(PURCHASE_PROMPT_SHOWN_KEY, false);
        localizationPromptShown = settings.getBoolean(LOCALIZATION_PROMPT_SHOWN_KEY, false);
        ratingPromptShown = settings.getBoolean(RATING_PROMPT_SHOWN_KEY, false);
        appLaunchedPortrait = settings.getInt(APP_LAUNCHED_KEY, 0);
        egg = settings.getBoolean(EGG_KEY, false);

        settings.edit().putBoolean(ADVANCED_PURCHASED_KEY, advancedOptionsPurchased).apply();
    }







    private void enableDisableAds(){

    }

    private void getAdConsentStatus(Context context){

        shouldUpdateConsent = false;
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
            Log.d(TAG, "user is active, last activity " + difference/ONE_MINUTE_MILLIS + " countdownMinutesTextView ago");
        } else {
            Log.d(TAG, "user is inactive, last activity " + difference/ONE_MINUTE_MILLIS + " countdownMinutesTextView ago");
        }
    }
    */

    private void updateCountdown() {
        if (!isFirstStart) {

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
            if (difference < 0) {
                try {
                    Date dateMax = simpleDateFormat.parse("24:00");
                    Date dateMin = simpleDateFormat.parse("00:00");
                    difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
                } catch (ParseException e) {
                    Log.e("UpdateCountdown", e + "");
                }
            }
            int day = (int) (difference / (1000 * 60 * 60 * 24));
            int hour = (int) ((difference - (1000 * 60 * 60 * 24 * day)) / (1000 * 60 * 60));
            int minute = Math.round((difference - (1000 * 60 * 60 * 24 * day) - (1000 * 60 * 60 * hour)) / (float) (1000 * 60));
            Log.i("updateCountdown", "Days: " + day + " Hours: " + hour + ", Mins: " + minute);

            if (hour >= bedtimePastTrigger) {
                difference = (difference - 86400000) * -1;
                present = true;
                day = (int) (difference / (1000 * 60 * 60 * 24));
                hour = (int) ((difference - (1000 * 60 * 60 * 24 * day)) / (1000 * 60 * 60));
                minute = Math.round((difference - (1000 * 60 * 60 * 24 * day) - (1000 * 60 * 60 * hour)) / (float) (1000 * 60));
                Log.i("updateCountdown", "Days: " + day + " Hours: " + hour + ", Mins: " + minute);
                sleepMessage.setVisibility(View.GONE);
            } else if (editBedtimeButton.getVisibility() != View.VISIBLE) {
                sleepMessage.setVisibility(View.VISIBLE);
            }

            if (minute == 60) {  //because countdownMinutesTextView are being rounded for accuracy reasons, this is needed to correct for minor errors
                minute = 0;
                hour++;
            }

            if (editBedtimeButton.getVisibility() == View.GONE && ((hour * 60) + minute) <= 120 && !sleepModeEnabled) {  //if within two countdownHoursTextView of bedtime, show button
                Log.d(TAG, "enabling sleep mode button");
                enableSleepmodeButton.setVisibility(View.VISIBLE);
                sleepModeEnabled = true;
            }


            if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
                //polish language stuff
                if (hour == 1){
                    countdownHoursTextView.setText(String.format(getString(R.string.countDownHourSingular), hour));
                } else if (hour >= 2 && hour <= 4){
                    countdownHoursTextView.setText(String.format(getString(R.string.countdownHourFunky), hour));
                } else {
                    countdownHoursTextView.setText(String.format(getString(R.string.countdownHourPlural), hour));
                }

                if (present) {
                    if (minute == 1) {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinuteSingularFuture), minute));
                    } else if (minute >= 2 && minute <= 4) {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinuteFunkyFuture), minute));
                    } else {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinutePluralFuture), minute));
                    }
                } else {
                    if (minute == 1) {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinuteSingularPast), minute));
                    } else if (minute >= 2 && minute <= 4) {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinuteFunkyPast), minute));
                    } else {
                        countdownMinutesTextView.setText(String.format(getString(R.string.countdownMinutePluralPast), minute));
                    }
                }
            } else {
                if (hour == 1) {
                    countdownHoursTextView.setText(String.format(Locale.US, getString(R.string.countDownHourSingular), hour));

                } else {
                    countdownHoursTextView.setText(String.format(Locale.US, getString(R.string.countdownHourPlural), hour));
                }
                if (present) {
                    if (minute == 1) {
                        countdownMinutesTextView.setText(String.format(Locale.US, getString(R.string.countdownMinuteSingularFuture), minute));
                    } else {
                        countdownMinutesTextView.setText(String.format(Locale.US, getString(R.string.countdownMinutePluralFuture), minute));
                    }

                } else {
                    if (minute == 1) {
                        countdownMinutesTextView.setText(String.format(Locale.US, getString(R.string.countdownMinuteSingularPast), minute));
                    } else {
                        countdownMinutesTextView.setText(String.format(Locale.US, getString(R.string.countdownMinutePluralPast), minute));
                    }
                }
            }
        }
    }

    @Override
    public void onFragmentInteraction(String string) {
        if (string.equalsIgnoreCase("advanced")){
            // normally an action would be performed in the Play Store version.
        } else if (string.equalsIgnoreCase("dismissed")){
            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.remove(nativeDialogPrompt);
            transaction.commit();
        }
    }
}
