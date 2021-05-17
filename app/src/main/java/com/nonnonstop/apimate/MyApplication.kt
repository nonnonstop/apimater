package com.nonnonstop.apimate

import android.app.Application
import androidx.preference.PreferenceManager
import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApplication : Application(), Thread.UncaughtExceptionHandler {
    private val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()
        val logDir = externalCacheDir
        if (logDir == null)
            Timber.plant(DebugTree())
        else
            Timber.plant(FileTree(logDir), DebugTree())
        Thread.setDefaultUncaughtExceptionHandler(this)
        upgradeData()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.e(e, "Unexpected error")
        defaultExceptionHandler?.uncaughtException(t, e)
    }

    private fun upgradeData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val savedVersion = pref.getInt("version", 100)
        val currentVersion = BuildConfig.VERSION_CODE
        if (savedVersion == currentVersion)
            return
        with(pref.edit()) {
            putInt("version", currentVersion)
            apply()
        }
        if (pref.getBoolean("revert_script_when_upgrade", true)) {
            Scripts(applicationContext).revertAllScripts()
        }
    }
}