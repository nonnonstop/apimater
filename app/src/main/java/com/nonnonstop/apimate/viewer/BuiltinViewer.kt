package com.nonnonstop.apimate.viewer

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

@Suppress("unused")
class BuiltinViewer {
    fun view(activity: AppCompatActivity) {
        val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses("jp.co.airfront.android.a2chMate")
        activity.finish()
    }
}