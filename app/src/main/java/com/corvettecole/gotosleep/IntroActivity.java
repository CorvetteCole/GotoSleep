package com.corvettecole.gotosleep;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();
        // Instead of fragments, you can also use our default slide.
        // Just create a `SliderPage` and provide title, description, background and image.
        // AppIntro will do the rest.
        SliderPage firstSlide = new SliderPage();
        firstSlide.setTitle("Welcome.");
        firstSlide.setDescription("It's time to fix your sleep schedule");
        firstSlide.setImageDrawable(R.drawable.ic_sleep);
        firstSlide.setBgColor(ContextCompat.getColor(this, R.color.firstSlide));
        firstSlide.setTitleTypefaceFontRes(R.font.product_sans_bold);
        firstSlide.setDescTypefaceFontRes(R.font.product_sans_regular);



        SliderPage secondSlide = new SliderPage();
        secondSlide.setTitle("Remind yourself.");
        secondSlide.setDescription("Set your bedtime and never lose track of time again");
        secondSlide.setImageDrawable(R.drawable.ic_alarm_clock);
        secondSlide.setBgColor(ContextCompat.getColor(this, R.color.secondSlide));
        secondSlide.setTitleTypefaceFontRes(R.font.product_sans_bold);
        secondSlide.setDescTypefaceFontRes(R.font.product_sans_regular);


        SliderPage thirdSlide = new SliderPage();
        thirdSlide.setTitle("Motivate yourself.");
        thirdSlide.setDescription("Set custom sleep reminders");
        thirdSlide.setImageDrawable(R.drawable.ic_copywriting);
        thirdSlide.setBgColor(ContextCompat.getColor(this, R.color.thirdSlide));
        thirdSlide.setTitleTypefaceFontRes(R.font.product_sans_bold);
        thirdSlide.setDescTypefaceFontRes(R.font.product_sans_regular);


        SliderPage fourthSlide = new SliderPage();
        fourthSlide.setTitle("Let's do this.");
        fourthSlide.setDescription("Your new sleep schedule starts now");
        fourthSlide.setImageDrawable(R.drawable.ic_bed);
        fourthSlide.setBgColor(ContextCompat.getColor(this, R.color.fourthSlide));
        fourthSlide.setTitleTypefaceFontRes(R.font.product_sans_bold);
        fourthSlide.setDescTypefaceFontRes(R.font.product_sans_regular);

        setDoneTextTypeface(R.font.product_sans_bold);
        setColorTransitionsEnabled(true);
        addSlide(AppIntroFragment.newInstance(firstSlide));
        addSlide(AppIntroFragment.newInstance(secondSlide));
        addSlide(AppIntroFragment.newInstance(thirdSlide));
        addSlide(AppIntroFragment.newInstance(fourthSlide));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        loadMainActivity();
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
