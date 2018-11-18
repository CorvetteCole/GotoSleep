package com.corvettecole.gotosleep

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    private var clicked = 0
    private var egg: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_Settings)
        val settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        egg = settings.getBoolean(EGG_KEY, false)

        val version = Element()
        version.title = "Version " + BuildConfig.VERSION_NAME


        version.setOnClickListener(View.OnClickListener {
            if (clicked < 10) {
                if (!egg) {
                    when (clicked) {
                        1 -> Toast.makeText(applicationContext, "maybe there is a secret here", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(applicationContext, "perhaps", Toast.LENGTH_SHORT).show()
                        3 -> Toast.makeText(applicationContext, "getting warmer...", Toast.LENGTH_SHORT).show()
                        4 -> Toast.makeText(applicationContext, "even warmer!", Toast.LENGTH_SHORT).show()
                        5 -> Toast.makeText(applicationContext, "oops sorry you missed it you were clicking too fast", Toast.LENGTH_SHORT).show()
                        6 -> Toast.makeText(applicationContext, "just kidding I guess", Toast.LENGTH_SHORT).show()
                        7 -> Toast.makeText(applicationContext, "curiosity killed the cat", Toast.LENGTH_SHORT).show()
                        8 -> Toast.makeText(applicationContext, "and satisfaction brought it back", Toast.LENGTH_SHORT).show()
                        9 -> {
                            Toast.makeText(applicationContext, "oi you made it! Something in the app seems to have changed...", Toast.LENGTH_LONG).show()
                            settings.edit().putBoolean(EGG_KEY, true).apply()
                        }
                    }
                } else {
                    when (clicked) {
                        1 -> Toast.makeText(applicationContext, "you can turn it off if you click 10 times", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(applicationContext, "lame", Toast.LENGTH_SHORT).show()
                        3 -> Toast.makeText(applicationContext, "you are lame", Toast.LENGTH_SHORT).show()
                        4 -> Toast.makeText(applicationContext, "why do you hate me", Toast.LENGTH_SHORT).show()
                        5 -> Toast.makeText(applicationContext, "you hate fun", Toast.LENGTH_SHORT).show()
                        6 -> Toast.makeText(applicationContext, "lame", Toast.LENGTH_SHORT).show()
                        7 -> Toast.makeText(applicationContext, "lame", Toast.LENGTH_SHORT).show()
                        8 -> Toast.makeText(applicationContext, "laaaammmeeee", Toast.LENGTH_SHORT).show()
                        9 -> {
                            Toast.makeText(applicationContext, "welcome to the lamezone your lameness. egg disabled", Toast.LENGTH_LONG).show()
                            settings.edit().putBoolean(EGG_KEY, false).apply()
                        }
                    }
                }
                clicked++
            }
        })



        val github = Element()
        github.title = "View the GitHub"
        github.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CorvetteCole/GotoSleep"))
            startActivity(browserIntent)
        })

        val playstore = Element()
        playstore.title = "Rate the app"
        playstore.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.corvettecole.gotosleep"))
            startActivity(browserIntent)
        })

        val donate = Element()
        donate.setTitle("Donate to me")
                .setOnClickListener(View.OnClickListener {
                    //#TODO launch new activity with donation options instead of opening web page
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/donate"));
                    val donateIntent = Intent(this, DonateActivity::class.java)
                    startActivity(donateIntent)
                })
        //donate.setIconDrawable(R.drawable.ic_money);

        val email = Element()
        email.title = "Contact me"
        email.setOnClickListener(View.OnClickListener {
            val subject = "Go to Sleep Feedback"
            val mailto = "mailto:corvettecole@gmail.com" +
                    "?subject=" + Uri.encode(subject)

            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(mailto)
            try {
                startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app available", Toast.LENGTH_LONG).show()
            }
        })

        val website = Element()
        website.title = "Visit my website"
        website.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://corvettecole.com/"))
            startActivity(browserIntent)
        })

        val credits = Element()
        credits.title = "Credits"
        credits.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/credits"))
            startActivity(browserIntent)
        })

        val privacy = Element()
        privacy.title = "Privacy Policy"
        privacy.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/privacy"))
            startActivity(browserIntent)
        })

        val license = Element()
        license.title = "License"
        license.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sleep.corvettecole.com/license"))
            startActivity(browserIntent)
        })


        val aboutPage = AboutPage(this)
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
                .create()

        setContentView(aboutPage)
    }

    companion object {
        internal val EGG_KEY = "curiosity_killed_the_cat"
    }


}
