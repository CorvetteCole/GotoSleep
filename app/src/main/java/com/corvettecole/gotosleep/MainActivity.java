package com.corvettecole.gotosleep;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int BACK_INTERVAL = 2000;
    private long backPressed;
    private Button settingsButton;
    private Button feedBackButton;
    private Button editBedtimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        //  Create a new boolean and preference and set it to true
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

        //  If the activity has never started before...
        if (isFirstStart) {

            //  Launch app slide1
            final Intent intro = new Intent(MainActivity.this, IntroActivity.class);

            runOnUiThread(new Runnable() {
                @Override public void run() {
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

            //runs when the intro slides launch mainActivity again
            boolean isSecondStart = getPrefs.getBoolean("secondStart", true);
            if (isSecondStart){
                editBedtimeButton.setVisibility(View.VISIBLE);
                editBedtimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //#TODO open settings to option for this
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
                final Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                @Override
                public void onClick(View view) {
                    //#TODO something like this or make your own settings screen
                    startActivity(settings);
                }
            });

            feedBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String subject = "Go to Sleep Feedback";
                    String bodyText = "Please explain your bug or feature suggestion thoroughly";
                    String mailto = "mailto:bob@example.org" +
                            "?cc=" + "alice@example.com" +
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

    @Override
    public void onBackPressed(){
        if (backPressed + BACK_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast toast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT);
            toast.show();
        }
        backPressed = System.currentTimeMillis();
    }
}
