package com.nonnonstop.apimate

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApplication : Application(), Thread.UncaughtExceptionHandler {
    private val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(FileTree(applicationContext), DebugTree())
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.e(e, "Unexpected error")
        defaultExceptionHandler?.uncaughtException(t, e)
    }
}