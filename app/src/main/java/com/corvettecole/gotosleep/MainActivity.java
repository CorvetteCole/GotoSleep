package com.corvettecole.gotosleep;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import static com.corvettecole.gotosleep.SettingsFragment.BEDTIME_KEY;

public class MainActivity extends AppCompatActivity {

    private static final int BACK_INTERVAL = 2000;
    private long backPressed;
    private Button settingsButton;
    private Button feedBackButton;
    private Button editBedtimeButton;

    private Calendar oldBedtimeCal;
    private Calendar bedtimeCal;
    private int[] bedtime;

    BroadcastReceiver _broadcastReceiver;
    private TextView hours;
    private TextView minutes;
    private TextView sleepMessage;

    private boolean isFirstStart;
    static int bedtimePastTrigger = 8;

    @Override
    public void onStart() {
        super.onStart();
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
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
        if (isFirstStart){

        }
        updateCountdown();
    }

    private void updateCountdown() {
        if (!isFirstStart){

            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(System.currentTimeMillis());

            Date endDate;
            Date startDate;
            boolean present = false;
            Calendar usedBedtime;



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
            int day = (int) (difference / (1000*60*60*24));
            int hour = (int) ((difference - (1000*60*60*24*day)) / (1000*60*60));
            Log.d("updateCountdown", (difference - (1000*60*60*24*day) - (1000*60*60*hour)) / (1000*60) +" min");
            int min = (int) (difference - (1000*60*60*24*day) - (1000*60*60*hour)) / (1000*60);
            Log.i("updateCountdown","Days: " + day + " Hours: "+hour+", Mins: "+min);

            if (hour >= bedtimePastTrigger){
                difference = (difference - 86400000)*-1;
                present = true;
                day = (int) (difference / (1000*60*60*24));
                hour = (int) ((difference - (1000*60*60*24*day)) / (1000*60*60));
                min = (int) (difference - (1000*60*60*24*day) - (1000*60*60*hour)) / (1000*60);
                Log.i("updateCountdown","Days: " + day + " Hours: "+hour+", Mins: "+min);
            }

            //weird bug where it is always one minute behind almost exactly. Not sure what I did
            //wrong but this is a temp fix
            //#TODO figure out why this is the way it is
            min = min + 1;
            if (min == 60){
                min = 0;
                hour = hour + 1;
            }



            if (hour == 1){
                hours.setText(hour + " hour");
            } else {
                hours.setText(hour + " hours");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        loadPreferences();

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        //  Create a new boolean and preference and set it to true
        isFirstStart = getPrefs.getBoolean("firstStart", true);

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
            finish();
        } else {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            setContentView(R.layout.activity_main);
            settingsButton = findViewById(R.id.settingsButton);
            editBedtimeButton = findViewById(R.id.bedtimeSetButton);
            feedBackButton = findViewById(R.id.feedbackButton);
            hours = findViewById(R.id.hours);
            minutes = findViewById(R.id.minutes);
            sleepMessage = findViewById(R.id.sleepMessage);

            //runs when the intro slides launch mainActivity again
            boolean isSecondStart = getPrefs.getBoolean("secondStart", true);
            final Intent settings = new Intent(MainActivity.this, SettingsActivity.class);

            if (isSecondStart) {
                editBedtimeButton.setVisibility(View.VISIBLE);
                editBedtimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(settings);
                    }
                });
                SharedPreferences.Editor e = getPrefs.edit();
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("secondStart", false);
                //  Apply changes
                e.apply();
            } else {
                editBedtimeButton.setVisibility(View.GONE);
            }


            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(settings);
                }
            });

            feedBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String subject = "Go to Sleep Feedback";
                    String bodyText = "Please explain your bug or feature suggestion thoroughly";
                    String mailto = "mailto:corvettecole@gmail.com" +
                            "&subject=" + Uri.encode(subject) +
                            "&body=" + Uri.encode(bodyText);

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(mailto));
                    try {
                        startActivity(emailIntent);
                    } catch (ActivityNotFoundException e) {
                        //TODO: Handle case where no email app is available
                    }
                }
            });



        }



    }

    private void loadPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.d("MainActivity", "Load Preferences Ran");
        bedtime = parseBedtime(settings.getString(BEDTIME_KEY, "19:35"));
        setBedtimeCal();

    }

    private void setBedtimeCal() {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());
        bedtimeCal = getBedtimeCal(bedtime);
        bedtimeCal.set(Calendar.SECOND, 0);
    }


    private int[] parseBedtime(String bedtime){
        int bedtimeHour = Integer.parseInt(bedtime.substring(0, bedtime.indexOf(":")));
        int bedtimeMin = Integer.parseInt(bedtime.substring(bedtime.indexOf(":") + 1, bedtime.length()));
        return new int[]{bedtimeHour, bedtimeMin};
    }

    private Calendar getBedtimeCal (int[] bedtime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, bedtime[0]);
        calendar.set(Calendar.MINUTE, bedtime[1]);
        return calendar;
    }





}
