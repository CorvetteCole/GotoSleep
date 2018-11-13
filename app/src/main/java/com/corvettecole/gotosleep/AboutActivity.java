package com.corvettecole.gotosleep;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Settings);

        Element versionElement = new Element();
        versionElement.setTitle("Version " + BuildConfig.VERSION_NAME);

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
