package com.nonnonstop.apimate.viewer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

class BuiltinViewer600 {
    fun view(
        activity: AppCompatActivity,
        url: String,
        sandUrl: String,
        delayMs: Long,
        clearFlag: Boolean
    ) {
        activity.finish()

        val sandIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sandUrl))
        sandIntent.setClassName(
            "jp.co.airfront.android.a2chMate",
            "jp.syoboi.a2chMate.activity.ResListActivity"
        )
        if (clearFlag) {
            sandIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity.startActivity(sandIntent)

        // Synchronous sleep for clear top activity
        try {
            Thread.sleep(delayMs)
        } catch (e: Exception) {
        }

        val viewIntent = Intent(sandIntent)
        viewIntent.data = Uri.parse(url)
        activity.startActivity(viewIntent)
    }
}