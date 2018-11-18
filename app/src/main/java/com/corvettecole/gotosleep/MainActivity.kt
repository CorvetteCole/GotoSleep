package com.corvettecole.gotosleep

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.corvettecole.gotosleep.AboutActivity.Companion.EGG_KEY
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.consent.DebugGeography
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.Objects

import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.CURRENT_NOTIFICATION_KEY
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.FIRST_NOTIFICATION_ALARM_REQUEST_CODE
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.NEXT_NOTIFICATION_ALARM_REQUEST_CODE
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.ONE_DAY_MILLIS

import com.corvettecole.gotosleep.SettingsFragment.Companion.ADS_ENABLED_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.ADVANCED_PURCHASED_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.BEDTIME_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.BUTTON_HIDE_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.DND_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_AMOUNT_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_DELAY_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_ENABLE_KEY
import java.lang.Math.abs
import java.lang.Math.min

class MainActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    private var backPressed: Long = 0
    private var settingsButton: Button? = null
    private var aboutButton: Button? = null
    private var editBedtimeButton: Button? = null

    private var bedtimeCal: Calendar? = null
    private var bedtime: IntArray? = null

    private var _broadcastReceiver: BroadcastReceiver? = null
    private var moon: ImageView? = null
    private var hours: TextView? = null
    private var minutes: TextView? = null
    private var sleepMessage: TextView? = null
    private var enableSleepmodeButton: Button? = null
    private var contentMain: View? = null

    private var isFirstStart: Boolean = false
    private var isSecondStart: Boolean = false
    private var adsEnabled: Boolean = false
    private val TAG = "MainActivity"
    private var notificationsEnabled: Boolean = false
    private var currentNotification: Int = 0
    private var numNotifications: Int = 0
    private var notificationDelay: Int = 0

    private var advancedOptionsPurchased: Boolean = false
    private var bp: BillingProcessor? = null

    private var notificationManager: NotificationManager? = null
    private var adView: AdView? = null

    private val adsLoaded = false
    private var adsInitialized = false
    private var isAutoDoNotDisturbEnabled: Boolean = false

    private var rateYesButton: Button? = null
    private var rateNoButton: Button? = null
    private var rateTextView: TextView? = null
    private var rateLayout: ConstraintLayout? = null

    private var isRequestingFeedback = false
    private var isRequestingRating = false
    private var appLaunched: Int = 0
    private var ratingPromptShown: Boolean = false

    private var consentForm: ConsentForm? = null

    private var getPrefs: SharedPreferences? = null

    private var sleepModeEnabled = false

    private var egg = false

    private val colorFadeDuration = 6000

    private val colorAnimations = ArrayList<ValueAnimator>()

    /*private UsageStatsManager usageStatsManager;
    private Button usageButton;
    private int userActiveMargin;
    */

    public override fun onStart() {
        super.onStart()

        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (Objects.requireNonNull(intent.action).compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    updateCountdown()
                }
            }
        }

        registerReceiver(_broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    public override fun onStop() {
        super.onStop()
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver)
    }

    override fun onBackPressed() {
        if (backPressed + BACK_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            val toast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT)
            toast.show()
        }
        backPressed = System.currentTimeMillis()
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called " + System.currentTimeMillis())

        loadPreferences()

        setNotifications(false) //Warning: takes a long time to execute (55ms!)

        updateCountdown()

        if (!egg) {
            clearEgg()
        } else {
            setEgg()
        }

        if (editBedtimeClicked) {
            editBedtimeButton!!.visibility = View.GONE
            editBedtimeClicked = false
        }

        if (!adsInitialized || shouldUpdateConsent) {
            enableDisableAds()
        }
        if (ratingPromptShown && rateLayout!!.visibility == View.VISIBLE) {
            rateLayout!!.visibility = View.GONE
            if (adView!!.visibility != View.VISIBLE) {
                Log.d(TAG, "re-enabling ads after rating prompt...")
                adsEnabled = true
                enableDisableAds()
            }
        }
        Log.d(TAG, "onResume finished " + System.currentTimeMillis())

    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        if (productId == "go_to_sleep_advanced") {
            Log.d("productPurchased", "go to sleep advanced purchased")
            advancedOptionsPurchased = true
            getPrefs!!.edit().putBoolean(ADVANCED_PURCHASED_KEY, true).apply()
        }
    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {

    }

    override fun onBillingInitialized() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public override fun onDestroy() {
        if (bp != null) {
            bp!!.release()
        }
        super.onDestroy()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called " + System.currentTimeMillis())

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)


        getPrefs = PreferenceManager
                .getDefaultSharedPreferences(baseContext)
        //  Create a new boolean and preference and set it to true
        isFirstStart = getPrefs!!.getBoolean("firstStart", true)
        isSecondStart = getPrefs!!.getBoolean("secondStart", true)


        //  If the activity has never started before...
        if (isFirstStart) {

            //  Launch app slide1
            val intro = Intent(this@MainActivity, IntroActivity::class.java)

            runOnUiThread { startActivity(intro) }

            //  Make a new preferences editor
            val e = getPrefs!!.edit()

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false)

            //  Apply changes
            e.apply()
            //this is needed to stop weird back button stuff
            finish()
        } else {


            setContentView(R.layout.activity_main)
            adView = findViewById(R.id.adView)
            settingsButton = findViewById(R.id.settingsButton)
            editBedtimeButton = findViewById(R.id.bedtimeSetButton)
            aboutButton = findViewById(R.id.aboutButton)
            moon = findViewById(R.id.moon)
            hours = findViewById(R.id.hours)
            minutes = findViewById(R.id.minutes)
            sleepMessage = findViewById(R.id.sleepMessage)
            contentMain = findViewById(R.id.content_main_layout)
            enableSleepmodeButton = findViewById(R.id.enableSleepModeButton)
            rateLayout = findViewById(R.id.rate_layout)
            rateNoButton = findViewById(R.id.rateNoButton)
            rateYesButton = findViewById(R.id.rateYesButton)
            rateTextView = findViewById(R.id.rateText)

            for (i in 0..9) {
                val colorFrom = resources.getColor(R.color.moonPrimary)
                val colorTo = resources.getColor(R.color.indigo)
                colorAnimations.add(ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo))
            }

            bp = BillingProcessor(this, resources.getString(R.string.license_key), this)

            bp!!.initialize()

            bp!!.loadOwnedPurchasesFromGoogle()


            notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel()


            loadPreferences()


            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                //#TODO add in additional parameter requiring an amount of time to have passed
                if (appLaunched < 8 && !ratingPromptShown) {
                    rateLayout!!.visibility = View.GONE
                    Log.d(TAG, "appLaunched: $appLaunched")
                    getPrefs!!.edit().putInt(APP_LAUNCHED_KEY, appLaunched + 1).apply()
                    //initiateRatingDialogue(getPrefs); //debug
                    enableDisableAds()
                } else if (!ratingPromptShown) {
                    Log.d(TAG, "initiating rating dialogue")
                    initiateRatingDialogue()
                } else {
                    rateLayout!!.visibility = View.GONE
                    enableDisableAds()
                    //initiateRatingDialogue(getPrefs);  //debug
                }
            }


            //runs when the intro slides launch mainActivity again
            val settings = Intent(this@MainActivity, SettingsActivity::class.java)
            val about = Intent(this@MainActivity, AboutActivity::class.java)

            if (isSecondStart) {
                editBedtimeButton!!.visibility = View.VISIBLE
                editBedtimeButton!!.setOnClickListener { view ->
                    startActivity(settings)
                    editBedtimeClicked = true
                }
                val e = getPrefs!!.edit()
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("secondStart", false)
                //  Apply changes
                e.apply()
            }


            settingsButton!!.setOnClickListener { view -> startActivity(settings) }

            enableSleepmodeButton!!.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (notificationManager!!.isNotificationPolicyAccessGranted && notificationManager!!.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                        val snoozeIntent = Intent(applicationContext, AutoDoNotDisturbReceiver::class.java)
                        val snoozePendingIntent = PendingIntent.getBroadcast(applicationContext, 11, snoozeIntent, 0)
                        try {
                            snoozePendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }

                    }
                }
                cancelNextNotification(this)
                setNotifications(true)
                Toast.makeText(this, "Set next bedtime notification to tomorrow", Toast.LENGTH_LONG).show()
                enableSleepmodeButton!!.visibility = View.GONE
            }

            aboutButton!!.setOnClickListener { view -> startActivity(about) }




            contentMain!!.setOnClickListener { v ->
                if (settingsButton!!.visibility == View.VISIBLE && buttonHide) {
                    settingsButton!!.visibility = View.INVISIBLE
                    aboutButton!!.visibility = View.INVISIBLE
                } else {
                    settingsButton!!.visibility = View.VISIBLE
                    aboutButton!!.visibility = View.VISIBLE
                }
            }

            Log.d(TAG, "onCreate finished " + System.currentTimeMillis())
        }
    }

    private fun clearEgg() {
        //figure out how to do this
        val temp = egg
        val eggCancel = !egg //if egg is enabled, eggCancel will be false
        egg = false
        moon!!.clearAnimation()
        for (colorAnimation in colorAnimations) {
            colorAnimation.end()
        }
        if (eggCancel) {
            moon!!.setColorFilter(resources.getColor(R.color.moonPrimary))
            moon!!.background = getDrawable(R.drawable.ic_moon_shadow)
        }
        egg = temp
    }

    private fun cancelNextNotification(context: Context) {
        val intent1 = Intent(context, BedtimeNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context,
                NEXT_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
    }

    private fun setEgg() {
        moon!!.background = getDrawable(R.color.transparent)
        val colorFrom = resources.getColor(R.color.moonPrimary)
        val colorTo = resources.getColor(R.color.indigo)
        clearEgg()
        val colorAnimation: ValueAnimator
        colorAnimations[0] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[0]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    indigoToViolet()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun redToOrange() {
        val colorFrom = resources.getColor(R.color.red)
        val colorTo = resources.getColor(R.color.orange)

        val colorAnimation: ValueAnimator
        colorAnimations[1] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[1]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    orangeToYellow()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun orangeToYellow() {
        val colorFrom = resources.getColor(R.color.orange)
        val colorTo = resources.getColor(R.color.yellow)

        val colorAnimation: ValueAnimator
        colorAnimations[2] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[2]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    yellowToGreen()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun yellowToGreen() {
        val colorFrom = resources.getColor(R.color.yellow)
        val colorTo = resources.getColor(R.color.green)

        val colorAnimation: ValueAnimator
        colorAnimations[3] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[3]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    greenToBlue()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun greenToBlue() {
        val colorFrom = resources.getColor(R.color.green)
        val colorTo = resources.getColor(R.color.blue)

        val colorAnimation: ValueAnimator
        colorAnimations[4] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[4]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    blueToIndigo()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun blueToIndigo() {
        val colorFrom = resources.getColor(R.color.blue)
        val colorTo = resources.getColor(R.color.indigo)

        val colorAnimation: ValueAnimator
        colorAnimations[5] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[5]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    indigoToViolet()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }

    private fun indigoToViolet() {
        val colorFrom = resources.getColor(R.color.indigo)
        val colorTo = resources.getColor(R.color.deep_purple)

        val colorAnimation: ValueAnimator
        colorAnimations[6] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[6]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    purpleToRed()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()

    }

    private fun purpleToRed() {
        val colorFrom = resources.getColor(R.color.deep_purple)
        val colorTo = resources.getColor(R.color.red)

        val colorAnimation: ValueAnimator
        colorAnimations[7] = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation = colorAnimations[7]

        colorAnimation.duration = colorFadeDuration.toLong() // milliseconds
        colorAnimation.addUpdateListener { animator -> moon!!.setColorFilter(animator.animatedValue as Int) }
        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                if (egg) {
                    redToOrange()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                animator.end()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        colorAnimation.start()
    }


    private fun initiateRatingDialogue() {
        rateLayout!!.visibility = View.VISIBLE
        rateLayout!!.invalidate()
        Log.d(TAG, "set rateLayout to visible")
        //initial state, TextView displays "Are you enjoying Go to Sleep?"

        (findViewById<View>(R.id.rate_layout) as ViewGroup).layoutTransition
                .enableTransitionType(LayoutTransition.CHANGING)

        rateNoButton!!.setOnClickListener { v ->
            if (!isRequestingFeedback && !isRequestingRating) {
                isRequestingFeedback = true
                rateTextView!!.text = getString(R.string.request_feedback)
                rateNoButton!!.text = getString(R.string.no_thanks)
                rateYesButton!!.text = getString(R.string.ok_sure)
            } else {
                rateLayout!!.visibility = View.GONE
                Log.d(TAG, "ads will re-enable after onResume called")
                adsInitialized = false
            }
            getPrefs!!.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply()
        }

        rateYesButton!!.setOnClickListener { v ->
            if (!isRequestingRating && !isRequestingFeedback) {
                isRequestingRating = true
                rateTextView!!.text = getString(R.string.rating_request)
                rateYesButton!!.text = getString(R.string.ok_sure)
                rateNoButton!!.text = getString(R.string.no_thanks)
            } else if (isRequestingFeedback) {
                sendFeedback()

            } else {
                sendToPlayStore()
            }
            getPrefs!!.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply()
        }


    }

    private fun sendToPlayStore() {
        val uri = Uri.parse("market://details?id=" + applicationContext.packageName)
        val rateAppIntent = Intent(Intent.ACTION_VIEW, uri)

        if (packageManager.queryIntentActivities(rateAppIntent, 0).size > 0) {
            startActivity(rateAppIntent)
        } else {
            /* handle your error case: the device has no way to handle market urls */
        }
    }

    private fun sendFeedback() {
        val subject = "Go to Sleep Feedback"
        val bodyText = "Please explain your bug or feature suggestion thoroughly"
        val mailto = "mailto:corvettecole@gmail.com" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(bodyText)

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse(mailto)
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            //TODO: Handle case where no email app is available
        }

    }

    private fun setNotifications(nextDay: Boolean) {
        if (notificationsEnabled) {
            val bedtimeCalendar = getBedtimeCal(bedtime!!)

            if (nextDay) {
                bedtimeCalendar.timeInMillis = bedtimeCalendar.timeInMillis + ONE_DAY_MILLIS
            } else if (bedtimeCalendar.timeInMillis < System.currentTimeMillis()) {
                bedtimeCalendar.timeInMillis = bedtimeCalendar.timeInMillis + ONE_DAY_MILLIS
            }

            val errorMargin = 30
            if (currentNotification != 1) {
                if (abs(System.currentTimeMillis() - bedtimeCal!!.timeInMillis) > (notificationDelay * numNotifications + errorMargin) * 60000) {
                    PreferenceManager.getDefaultSharedPreferences(baseContext).edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
                    currentNotification = 1
                }
            }

            val intent1 = Intent(this, BedtimeNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this,
                    FIRST_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtimeCalendar.timeInMillis, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, bedtimeCalendar.timeInMillis, pendingIntent)
            }
        }
    }

    private fun loadPreferences() {
        val settings = PreferenceManager.getDefaultSharedPreferences(baseContext)
        Log.d("MainActivity", "Load Preferences Ran")
        bedtime = parseBedtime(settings.getString(BEDTIME_KEY, "19:35")!!)

        buttonHide = settings.getBoolean(BUTTON_HIDE_KEY, false)
        notificationsEnabled = settings.getBoolean(NOTIF_ENABLE_KEY, true)
        bedtimeCal = getBedtimeCal(bedtime!!)
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1)
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3.toString() + "")!!)
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15.toString() + "")!!)
        advancedOptionsPurchased = settings.getBoolean(ADVANCED_PURCHASED_KEY, false)
        adsEnabled = settings.getBoolean(ADS_ENABLED_KEY, false)
        isAutoDoNotDisturbEnabled = settings.getBoolean(DND_KEY, false)
        if (isAutoDoNotDisturbEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings.edit().putBoolean(DND_KEY, notificationManager!!.isNotificationPolicyAccessGranted).apply()
            }
        }
        advancedOptionsPurchased = bp!!.isPurchased("go_to_sleep_advanced")
        ratingPromptShown = settings.getBoolean(RATING_PROMPT_SHOWN_KEY, false)
        appLaunched = settings.getInt(APP_LAUNCHED_KEY, 0)
        egg = settings.getBoolean(EGG_KEY, false)

        settings.edit().putBoolean(ADVANCED_PURCHASED_KEY, advancedOptionsPurchased).apply()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(BEDTIME_CHANNEL_ID, name, importance)
            channel.description = description
            channel.setSound(null, null)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun enableDisableAds() {
        if (adsEnabled && adView!!.visibility != View.VISIBLE && rateLayout!!.visibility != View.VISIBLE || shouldUpdateConsent) {
            Log.d(TAG, "enableDisableAds initialized")
            if (!adsInitialized) {
                //MobileAds.initialize(this, getResources().getString(R.string.admob_key));
                adsInitialized = true
            }
            adView!!.visibility = View.VISIBLE
            getAdConsentStatus(this)

        } else if (adView!!.visibility != View.GONE && !adsEnabled) {

            adView!!.visibility = View.GONE
        }
    }

    private fun getAdConsentStatus(context: Context) {
        val consentInformation = ConsentInformation.getInstance(context)
        val publisherIds = arrayOf(context.resources.getString(R.string.admob_publisher_id))
        //consentInformation.addTestDevice("36EB1E9DFC6D82630E576163C46AD12D");
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {

            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                if (consentInformation.isRequestLocationInEeaOrUnknown) {
                    if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
                        val extras = Bundle()
                        extras.putString("npa", "1")
                        val adRequest = AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                                //                      .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                                .build()
                        adView!!.loadAd(adRequest)
                    } else if (consentStatus == ConsentStatus.UNKNOWN) {
                        consentForm = makeConsentForm(context)
                        Log.d(TAG, "consent form loading")
                        consentForm!!.load()
                    } else {
                        val adRequest = AdRequest.Builder()
                                //                    .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                                .build()
                        adView!!.loadAd(adRequest)
                    }
                } else {
                    //US users
                    val adRequest = AdRequest.Builder()
                            //              .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                            .build()
                    adView!!.loadAd(adRequest)
                }


            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
            }
        })
        shouldUpdateConsent = false
    }

    private fun makeConsentForm(context: Context): ConsentForm {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL("https://sleep.corvettecole.com/privacy")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            // Handle error.
        }

        return ConsentForm.Builder(context, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "consent form loaded... showing")
                        consentForm!!.show()

                    }

                    override fun onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "consent form opened")
                    }

                    override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                        // Consent form was closed.
                        Log.d(TAG, "consent form closed")
                        if (userPrefersAdFree!!) {
                            Log.d(TAG, "initiating in-app purchase...")
                            bp!!.purchase(this@MainActivity, "go_to_sleep_advanced")

                        } else if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
                            val extras = Bundle()
                            extras.putString("npa", "1")
                            val adRequest = AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                                    .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                                    .build()
                            adView!!.loadAd(adRequest)
                        } else {
                            val adRequest = AdRequest.Builder()
                                    .addTestDevice("36EB1E9DFC6D82630E576163C46AD12D")
                                    .build()
                            adView!!.loadAd(adRequest)
                        }

                    }

                    override fun onConsentFormError(errorDescription: String?) {
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build()
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

    private fun updateCountdown() {
        if (!isFirstStart) {

            val current = Calendar.getInstance()
            current.timeInMillis = System.currentTimeMillis()

            val endDate: Date
            val startDate: Date
            var present = false


            val simpleDateFormat = SimpleDateFormat("HH:mm")

            Log.d("updateCountdown", current.get(Calendar.SECOND).toString() + "")
            current.set(Calendar.SECOND, 0)

            startDate = bedtimeCal!!.time
            endDate = current.time

            Log.d("updateCountdown", bedtimeCal!!.time.toString() + " bedtime")

            Log.d("updateCountdown", current.time.toString() + " current time")


            var difference = endDate.time - startDate.time
            if (difference < 0) {
                try {
                    val dateMax = simpleDateFormat.parse("24:00")
                    val dateMin = simpleDateFormat.parse("00:00")
                    difference = dateMax.time - startDate.time + (endDate.time - dateMin.time)
                } catch (e: ParseException) {
                    Log.e("UpdateCountdown", e.toString() + "")
                }

            }
            var day = (difference / (1000 * 60 * 60 * 24)).toInt()
            var hour = ((difference - 1000 * 60 * 60 * 24 * day) / (1000 * 60 * 60)).toInt()
            var min = Math.round((difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()) / (1000 * 60).toFloat())
            Log.i("updateCountdown", "Days: $day Hours: $hour, Mins: $min")

            if (hour >= bedtimePastTrigger) {
                difference = (difference - 86400000) * -1
                present = true
                day = (difference / (1000 * 60 * 60 * 24)).toInt()
                hour = ((difference - 1000 * 60 * 60 * 24 * day) / (1000 * 60 * 60)).toInt()
                min = Math.round((difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()) / (1000 * 60).toFloat())
                Log.i("updateCountdown", "Days: $day Hours: $hour, Mins: $min")
            }

            if (min == 60) {  //because minutes are being rounded for accuracy reasons, this is needed to correct for minor errors
                min = 0
                hour++
            }


            if (hour == 1) {
                hours!!.text = hour.toString() + " hour"

            } else {
                hours!!.text = hour.toString() + " hours"
            }

            if (editBedtimeButton!!.visibility == View.GONE && hour * 60 + min <= 120 && !sleepModeEnabled) {  //if within two hours of bedtime, show button
                Log.d(TAG, "enabling sleep mode button")
                enableSleepmodeButton!!.visibility = View.VISIBLE
                sleepModeEnabled = true
            }

            if (present) {
                if (min == 1) {
                    minutes!!.text = min.toString() + " minute until bedtime"
                } else {
                    minutes!!.text = min.toString() + " minutes until bedtime"
                }
                sleepMessage!!.visibility = View.GONE
            } else {
                if (min == 1) {
                    minutes!!.text = min.toString() + " minute past bedtime"
                } else {
                    minutes!!.text = min.toString() + " minutes past bedtime"
                }
                if (editBedtimeButton!!.visibility != View.VISIBLE) {
                    sleepMessage!!.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {

        internal val BEDTIME_CHANNEL_ID = "bedtimeNotifications"
        private val BACK_INTERVAL = 2000
        internal var bedtimePastTrigger = 8
        internal var buttonHide = false

        internal var notifications = arrayOfNulls<String>(5)

        internal val APP_LAUNCHED_KEY = "numLaunched"
        internal val RATING_PROMPT_SHOWN_KEY = "rateShown"

        internal var shouldUpdateConsent = false

        internal var editBedtimeClicked = false


        internal fun parseBedtime(bedtime: String): IntArray {
            val bedtimeHour = Integer.parseInt(bedtime.substring(0, bedtime.indexOf(":")))
            val bedtimeMin = Integer.parseInt(bedtime.substring(bedtime.indexOf(":") + 1, bedtime.length))
            return intArrayOf(bedtimeHour, bedtimeMin)
        }

        internal fun getBedtimeCal(bedtime: IntArray): Calendar {
            val TAG = "getBedtimeCal"
            Log.d(TAG, "bedtime[0], bedtime[1] " + bedtime[0] + "," + bedtime[1])
            val calendar = Calendar.getInstance()
            //calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, bedtime[0])
            calendar.set(Calendar.MINUTE, bedtime[1])
            calendar.set(Calendar.SECOND, 0)
            return calendar
        }
    }
}
