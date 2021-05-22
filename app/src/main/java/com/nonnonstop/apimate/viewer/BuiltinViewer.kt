package com.nonnonstop.apimate.viewer

import android.app.ActivityManager
import androidx.appcompat.app.AppCompatActivity

class BuiltinViewer {
    fun view(activity: AppCompatActivity) {
        activity
            .getSystemService(ActivityManager::class.java)
            .killBackgroundProcesses("jp.co.airfront.android.a2chMate")
        activity.finish()
    }
}