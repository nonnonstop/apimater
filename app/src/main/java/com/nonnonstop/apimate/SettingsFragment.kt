package com.nonnonstop.apimate

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
        onCreateDatPreference()
        onCreateOpenDefaultPreference()
        onCreateVersionPreference()
    }

    private fun onCreateDatPreference() {
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

    private fun onCreateOpenDefaultPreference() {
        val preference = findPreference<Preference>("open_default")!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            preference.isEnabled = false
            return
        }
        preference.setOnPreferenceClickListener {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            startActivity(intent)
            true
        }
    }

    private fun onCreateVersionPreference() {
        val versionPreference = findPreference<Preference>("version")!!
        versionPreference.summary = "${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}"
    }
}