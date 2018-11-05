package com.corvettecole.gotosleep

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.CURRENT_NOTIFICATION_KEY
import com.corvettecole.gotosleep.BedtimeNotificationReceiver.Companion.ONE_DAY_MILLIS
import com.corvettecole.gotosleep.SettingsFragment.Companion.ADVANCED_PURCHASED_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.BEDTIME_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.BUTTON_HIDE_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_AMOUNT_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_DELAY_KEY
import com.corvettecole.gotosleep.SettingsFragment.Companion.NOTIF_ENABLE_KEY

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


import java.lang.Math.abs

class MainActivity : AppCompatActivity() {
    private var backPressed: Long = 0
    private var settingsButton: Button? = null
    private var feedBackButton: Button? = null
    private var editBedtimeButton: Button? = null

    private var bedtimeCal: Calendar? = null
    private var bedtime: IntArray? = null

    internal var _broadcastReceiver: BroadcastReceiver? = null
    private var hours: TextView? = null
    private var minutes: TextView? = null
    private var sleepMessage: TextView? = null
    private var contentMain: View? = null

    private var isFirstStart: Boolean = false
    private var isSecondStart: Boolean = false
    private val TAG = "MainActivity"
    private var notificationsEnabled: Boolean = false
    private var currentNotification: Int = 0
    private var numNotifications: Int = 0
    private var notificationDelay: Int = 0

    private var billingClient: BillingClient? = null
    private var advancedOptionsPurchased: Boolean = false

    public override fun onStart() {
        super.onStart()
        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0) {
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
        loadPreferences()
        updateCountdown()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        loadPreferences()

        val getPrefs = PreferenceManager
                .getDefaultSharedPreferences(baseContext)

        //  Create a new boolean and preference and set it to true
        isFirstStart = getPrefs.getBoolean("firstStart", true)
        isSecondStart = getPrefs.getBoolean("secondStart", true)

        //  If the activity has never started before...
        if (isFirstStart) {

            //  Launch app slide1
            val intro = Intent(this@MainActivity, IntroActivity::class.java)

            runOnUiThread { startActivity(intro) }

            //  Make a new preferences editor
            val e = getPrefs.edit()

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false)

            //  Apply changes
            e.apply()
            //this is needed to stop weird back button stuff
            finish()
        } else {
            window.navigationBarColor = resources.getColor(R.color.colorPrimary)
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
            setContentView(R.layout.activity_main)
            settingsButton = findViewById(R.id.settingsButton)
            editBedtimeButton = findViewById(R.id.bedtimeSetButton)
            feedBackButton = findViewById(R.id.feedbackButton)
            hours = findViewById(R.id.hours)
            minutes = findViewById(R.id.minutes)
            sleepMessage = findViewById(R.id.sleepMessage)
            contentMain = findViewById(R.id.content_main_layout)

            //runs when the intro slides launch mainActivity again
            val settings = Intent(this@MainActivity, SettingsActivity::class.java)

            if (isSecondStart) {
                editBedtimeButton!!.visibility = View.VISIBLE
                editBedtimeButton!!.setOnClickListener { startActivity(settings) }
                val e = getPrefs.edit()
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("secondStart", false)
                //  Apply changes
                e.apply()
            }


            settingsButton!!.setOnClickListener { startActivity(settings) }

            feedBackButton!!.setOnClickListener {
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

            contentMain!!.setOnClickListener {
                if (settingsButton!!.visibility == View.VISIBLE && buttonHide) {
                    settingsButton!!.visibility = View.INVISIBLE
                    feedBackButton!!.visibility = View.INVISIBLE
                } else {
                    settingsButton!!.visibility = View.VISIBLE
                    feedBackButton!!.visibility = View.VISIBLE
                }
            }
        }


    }

    private fun setNotifications() {
        if (notificationsEnabled) {
            val bedtimeCalendar = getBedtimeCal(bedtime!!)

            if (bedtimeCalendar.timeInMillis < System.currentTimeMillis()) {
                bedtimeCalendar.timeInMillis = bedtimeCalendar.timeInMillis + ONE_DAY_MILLIS
            }

            val errorMargin = 30
            if (currentNotification != 1) {
                if (abs(System.currentTimeMillis() - bedtimeCal!!.timeInMillis) > (notificationDelay * numNotifications + errorMargin) * 60000) {
                    PreferenceManager.getDefaultSharedPreferences(baseContext).edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply()
                    currentNotification = 1
                }
            }

            Log.d(TAG, "bedtime calendar: " + bedtimeCalendar.timeInMillis + " more: " + bedtimeCalendar.time)
            Log.d(TAG, "Current time: " + System.currentTimeMillis())
            Log.d(TAG, "Setting notification at: " + bedtimeCalendar.timeInMillis + " more: " + bedtimeCalendar.time)
            val intent1 = Intent(this, BedtimeNotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(this,
                    REQUEST_CODE_BEDTIME, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, bedtimeCalendar.timeInMillis, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, bedtimeCalendar.timeInMillis, pendingIntent)
            }
        } else {
            Log.d(TAG, "setNotifications: $notificationsEnabled")
        }
    }

    private fun updateCountdown() {
        if (!isFirstStart) {

            val current = Calendar.getInstance()
            current.timeInMillis = System.currentTimeMillis()

            val endDate: Date
            val startDate: Date
            var present = false
            val usedBedtime: Calendar


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
            Log.d("updateCountdown", ((difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()) / (1000 * 60)).toString() + " min")
            var min = (difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()).toInt() / (1000 * 60)
            Log.i("updateCountdown", "Days: $day Hours: $hour, Mins: $min")

            val currentMin = current.get(Calendar.MINUTE)
            val bedtimeMin = bedtimeCal!!.get(Calendar.MINUTE)

            val isCountdownCorrect: Boolean
            if (hour >= bedtimePastTrigger) {
                difference = (difference - 86400000) * -1
                present = true
                day = (difference / (1000 * 60 * 60 * 24)).toInt()
                hour = ((difference - 1000 * 60 * 60 * 24 * day) / (1000 * 60 * 60)).toInt()
                min = (difference - (1000 * 60 * 60 * 24 * day).toLong() - (1000 * 60 * 60 * hour).toLong()).toInt() / (1000 * 60)
                Log.i("updateCountdown", "Days: $day Hours: $hour, Mins: $min")


                //time debugging and jank code which probably isn't needed but I don't want to delete
                //in case I have to debug it again.
                if (min + currentMin < 60) {
                    isCountdownCorrect = min + currentMin == bedtimeMin
                } else if (min + currentMin == 60) {
                    isCountdownCorrect = min + currentMin == 60
                } else {
                    isCountdownCorrect = min + currentMin - 60 == bedtimeMin
                }
                if (isCountdownCorrect) {
                    Log.d("updateCountdown", "countdown min($min) + current min($currentMin) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                } else {
                    Log.e("updateCountdown", "countdown min($min) + current min($currentMin) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                }

            } else {
                if (currentMin - min >= 0) {
                    isCountdownCorrect = currentMin - min == bedtimeMin
                    Log.d("updateCountdown", "current min($currentMin) - countdown min($min) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                } else {
                    isCountdownCorrect = currentMin - min + 60 == bedtimeMin
                    Log.d("updateCountdown", "current min($currentMin) - countdown min($min) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                }
                if (isCountdownCorrect) {
                    Log.d("updateCountdown", "current min($currentMin) - countdown min($min) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                } else {
                    Log.e("updateCountdown", "current min($currentMin) - countdown min($min) = bedtime min($bedtimeMin)? $isCountdownCorrect")
                }
            }

            /*
            //Update, jank code may not be needed? It seems to be accurate

            else {  //this else statement is part of jank time fix

                //weird bug where it is always one minute behind almost exactly. Not sure what I did
                //wrong but this is a temp fix

            Update on weird time bug. It is only a minute behind when it is finding how far the
            current time is PAST the bedtime. Otherwise it seems to be spot on. WTF????

            Going to jank together some more fix
                min = min + 1;
                if (min == 60) {
                    min = 0;
                    hour = hour + 1;
                }
            }*/

            if (hour == 1) {
                hours!!.text = hour.toString() + " hour"
            } else {
                hours!!.text = hour.toString() + " hours"
            }

            if (present) {
                if (min == 1) {
                    minutes!!.text = min.toString() + " minute until bedtime"
                } else {
                    minutes!!.text = min.toString() + " minutes until bedtime"
                }
                sleepMessage!!.visibility = View.INVISIBLE
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
        if (!advancedOptionsPurchased) {
            checkInAppPurchases()
        }

        setNotifications()
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

    private fun checkInAppPurchases() {
        //some code

    }

    companion object {

        private val REQUEST_CODE_BEDTIME = 1
        internal var BEDTIME_CHANNEL_ID = "bedtimeNotifications"
        private val BACK_INTERVAL = 2000
        internal var bedtimePastTrigger = 8
        internal var buttonHide = false

        internal var notifications = arrayOfNulls<String>(5)


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
