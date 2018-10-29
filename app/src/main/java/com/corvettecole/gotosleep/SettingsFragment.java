package com.corvettecole.gotosleep;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.preference.Preference;


public class SettingsFragment extends BasePreferenceFragmentCompat{


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        /*#TODO check if premium unlock has been purchased. If so, enable premium category, set adsEnabled to false, and disable ads pref
        getPreferenceScreen().findPreference("pref_adsEnabled").setEnabled(false);
        getPreferenceScreen().findPreference("pref_premium").setEnabled(true);
        */

        final Preference adsEnabledPref = this.findPreference("pref_adsEnabled");

        adsEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //if enable ads is switched off, set premium options to false;
                if (!(boolean)newValue){
                    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                    sharedPreferences.edit().putBoolean("pref_smartNotifications", false).apply();
                }
                return true;
            }
        });

    }


}