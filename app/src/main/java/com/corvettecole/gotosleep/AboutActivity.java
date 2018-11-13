package com.corvettecole.gotosleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

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


        Element versionElement = new Element();
        versionElement.setTitle("Version " + BuildConfig.VERSION_NAME);
        versionElement.setOnClickListener(view -> {
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

        Element donate = new Element();
        donate.setTitle("Donate")
                .setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/donate"));
                    startActivity(browserIntent);
                });
        donate.setIconDrawable(R.drawable.ic_money);







        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("I created this open-source, mostly ad-free app not for money but because I saw a problem that needed solving. I encourage you to look at the source code of this app, available on my GitHub linked below. I would be thrilled if even one person compiled my code from scratch and tried to contribute to this project. Thank you for downloading Go to Sleep and I hope you have a beautiful sleep schedule from here on out! ")
                .addItem(versionElement)
                .addGitHub("corvettecole", "View my GitHub")
                .addPlayStore(BuildConfig.APPLICATION_ID, "Rate the app")
                .addItem(donate)
                .addEmail("corvettecole@gmail.com", "Contact me")
                .addWebsite("https://corvettecole.com", "Visit my website")
                .addWebsite("https://sleep.corvettecole.com/credits/", "Credits")
                .addWebsite("https://sleep.corvettecole.com/privacy/", "Privacy policy")
                .addWebsite("https://sleep.corvettecole.com/license/", "License")
                .create();


        setContentView(aboutPage);








        //setContentView something
    }


}
