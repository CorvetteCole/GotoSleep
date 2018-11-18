package com.corvettecole.gotosleep

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Note here that we DO NOT use setContentView();
        // Instead of fragments, you can also use our default slide.
        // Just create a `SliderPage` and provide title, description, background and image.
        // AppIntro will do the rest.
        val firstSlide = SliderPage()
        firstSlide.title = "Welcome."
        firstSlide.description = "It's time to fix your sleep schedule"
        firstSlide.imageDrawable = R.drawable.ic_sleep
        firstSlide.bgColor = ContextCompat.getColor(this, R.color.firstSlide)
        firstSlide.titleTypefaceFontRes = R.font.product_sans_bold
        firstSlide.descTypefaceFontRes = R.font.product_sans_regular


        val secondSlide = SliderPage()
        secondSlide.title = "Remind yourself."
        secondSlide.description = "Set your bedtime and never lose track of time again"
        secondSlide.imageDrawable = R.drawable.ic_alarm_clock
        secondSlide.bgColor = ContextCompat.getColor(this, R.color.secondSlide)
        secondSlide.titleTypefaceFontRes = R.font.product_sans_bold
        secondSlide.descTypefaceFontRes = R.font.product_sans_regular


        val thirdSlide = SliderPage()
        thirdSlide.title = "Motivate yourself."
        thirdSlide.description = "Set custom sleep reminders"
        thirdSlide.imageDrawable = R.drawable.ic_copywriting
        thirdSlide.bgColor = ContextCompat.getColor(this, R.color.thirdSlide)
        thirdSlide.titleTypefaceFontRes = R.font.product_sans_bold
        thirdSlide.descTypefaceFontRes = R.font.product_sans_regular


        val fourthSlide = SliderPage()
        fourthSlide.title = "Let's do this."
        fourthSlide.description = "Your new sleep schedule starts now"
        fourthSlide.imageDrawable = R.drawable.ic_bed
        fourthSlide.bgColor = ContextCompat.getColor(this, R.color.fourthSlide)
        fourthSlide.titleTypefaceFontRes = R.font.product_sans_bold
        fourthSlide.descTypefaceFontRes = R.font.product_sans_regular

        setDoneTextTypeface(R.font.product_sans_bold)
        setColorTransitionsEnabled(true)
        addSlide(AppIntroFragment.newInstance(firstSlide))
        addSlide(AppIntroFragment.newInstance(secondSlide))
        addSlide(AppIntroFragment.newInstance(thirdSlide))
        addSlide(AppIntroFragment.newInstance(fourthSlide))

        // Hide Skip/Done button.
        showSkipButton(false)
        isProgressButtonEnabled = true

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Do something when users tap on Skip button.
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Do something when users tap on Done button.
        loadMainActivity()
        finish()

    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

    }

    private fun loadMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
