package com.corvettecole.gotosleep

import android.os.Bundle

import com.takisoft.preferencex.PreferenceFragmentCompat

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceScreen

class SettingsActivity : AppCompatActivity(), androidx.preference.PreferenceFragmentCompat.OnPreferenceStartScreenCallback{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        if (savedInstanceState == null) {
            val fragment = SettingsFragment()

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment, fragment)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onPreferenceStartScreen(caller: androidx.preference.PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
        val fragment = SettingsFragment()
        val args = Bundle()
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
        fragment.arguments = args

        val ft = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.replace(R.id.fragment, fragment, pref.key)
        ft.addToBackStack(pref.key)
        ft.commitAllowingStateLoss()

        return true
    }
}
