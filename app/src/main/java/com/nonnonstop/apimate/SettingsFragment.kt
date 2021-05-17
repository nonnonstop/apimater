package com.nonnonstop.apimate

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var scripts: Scripts

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scripts = Scripts(requireContext())
        val preparePreference = findPreference<Preference>("prepare")!!
        try {
            val configState = scripts.onCreateDatPreference(this, preparePreference)
            preparePreference.setOnPreferenceClickListener {
                try {
                    scripts.onClickDatPreference(this, configState)
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to execute script (onClickDatPreference)")
                    Snackbar.make(
                        requireView(),
                        R.string.prepare_click_failed,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                true
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to execute script (onCreateDatPreference)")
            Snackbar.make(
                requireView(),
                R.string.prepare_create_failed,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}