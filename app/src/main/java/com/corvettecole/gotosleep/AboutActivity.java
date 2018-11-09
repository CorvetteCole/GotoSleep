package com.corvettecole.gotosleep;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element versionElement = new Element();
        versionElement.setTitle("Version " + BuildConfig.VERSION_NAME);

        /*Element donate = new Element();
        versionElement.setTitle("Buy me a coffee and support my future open-source projects")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });*/

        /*Element credits = new Element();
        credits.setTitle("Icon Credits");
        credits.*/




        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("I created this open-source, mostly ad-free app not for money but because I saw a problem that needed solving. I encourage you to look at the source code of this app, available on my GitHub linked below. I would be thrilled if even one person compiled my code from scratch and tried to contribute to this project. Thank you for downloading Go to Sleep and I hope you have a beautiful sleep schedule from here on out! ")
                .addItem(versionElement)
                .addPlayStore(BuildConfig.APPLICATION_ID, "Rate the app")

                .addEmail("corvettecole@gmail.com", "Contact me")
                .addWebsite("https://corvettecole.com", "Visit my website")
                .addGitHub("corvettecole", "View my GitHub")
                .addWebsite("https://sleep.corvettecole.com/privacy/", "Privacy Policy")
                .create();

        setContentView(aboutPage);








        //setContentView something
    }


}
