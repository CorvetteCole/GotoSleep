package com.corvettecole.gotosleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

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
            final Intent i = new Intent(MainActivity.this, IntroActivity.class);

            runOnUiThread(new Runnable() {
                @Override public void run() {
                    startActivity(i);
                }
            });

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();
        }
        setContentView(R.layout.activity_main);
    }
}
