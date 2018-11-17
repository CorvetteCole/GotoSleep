package com.corvettecole.gotosleep;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private int clicked = 0;
    static final String EGG_KEY = "curiosity_killed_the_cat";
    private boolean egg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Settings);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        egg = settings.getBoolean(EGG_KEY, false);

        Element version = new Element();
        version.setTitle("Version " + BuildConfig.VERSION_NAME);
        version.setOnClickListener(view -> {
            if (clicked < 10) {
                if (!egg) {
                    switch (clicked) {
                        case 1:
                            Toast.makeText(getApplicationContext(), "maybe there is a secret here", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "perhaps", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getApplicationContext(), "getting warmer...", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(getApplicationContext(), "even warmer!", Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(getApplicationContext(), "oops sorry you missed it you were clicking too fast", Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(getApplicationContext(), "just kidding I guess", Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(getApplicationContext(), "curiosity killed the cat", Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(getApplicationContext(), "and satisfaction brought it back", Toast.LENGTH_SHORT).show();
                            break;
                        case 9:
                            Toast.makeText(getApplicationContext(), "oi you made it! Something in the app seems to have changed...", Toast.LENGTH_LONG).show();
                            settings.edit().putBoolean(EGG_KEY, true).apply();
                            break;
                    }
                } else {
                    switch (clicked) {
                        case 1:
                            Toast.makeText(getApplicationContext(), "you can turn it off if you click 10 times", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "lame", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getApplicationContext(), "you are lame", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(getApplicationContext(), "why do you hate me", Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(getApplicationContext(), "you hate fun", Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(getApplicationContext(), "lame", Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(getApplicationContext(), "lame", Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(getApplicationContext(), "laaaammmeeee", Toast.LENGTH_SHORT).show();
                            break;
                        case 9:
                            Toast.makeText(getApplicationContext(), "welcome to the lamezone your lameness. egg disabled", Toast.LENGTH_LONG).show();
                            settings.edit().putBoolean(EGG_KEY, false).apply();
                            break;
                    }
                }
                clicked++;
            }
        });


        Element github = new Element();
        github.setTitle("View the GitHub");
        github.setOnClickListener(view -> {
           Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CorvetteCole/GotoSleep"));
           startActivity(browserIntent);
        });

        Element playstore = new Element();
        playstore.setTitle("Rate the app");
        playstore.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.corvettecole.gotosleep"));
            startActivity(browserIntent);
        });

        Element donate = new Element();
        donate.setTitle("Donate to me")
                .setOnClickListener(v -> {
                    //#TODO launch new activity with donation options instead of opening web page
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/donate"));
                    Intent donateIntent = new Intent(this, DonateActivity.class);
                    startActivity(donateIntent);
                });
        //donate.setIconDrawable(R.drawable.ic_money);

        Element email = new Element();
        email.setTitle("Contact me");
        email.setOnClickListener(view -> {
            String subject = "Go to Sleep Feedback";
            String mailto = "mailto:corvettecole@gmail.com" +
                    "?subject=" + Uri.encode(subject);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No email app available", Toast.LENGTH_LONG).show();
            }
        });

        Element website = new Element();
        website.setTitle("Visit my website");
        website.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.corvettecole.com/"));
            startActivity(browserIntent);
        });

        Element credits = new Element();
        credits.setTitle("Credits");
        credits.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/credits"));
            startActivity(browserIntent);
        });

        Element privacy = new Element();
        privacy.setTitle("Privacy Policy");
        privacy.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/privacy"));
            startActivity(browserIntent);
        });

        Element license = new Element();
        license.setTitle("License");
        license.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/license"));
            startActivity(browserIntent);
        });


        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .addItem(version)
                .addItem(github)
                .addItem(playstore)
                .addItem(donate)
                .addItem(website)
                .addItem(email)
                .addItem(credits)
                .addItem(privacy)
                .addItem(license)
                .create();

        setContentView(aboutPage);
    }


}
