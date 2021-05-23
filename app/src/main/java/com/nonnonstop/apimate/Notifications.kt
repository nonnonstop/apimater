package com.nonnonstop.apimate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notifications {
    enum class NotificationId(val value: Int) {
        UPGRADE(0),
    }

    enum class ChannelId(val value: String) {
        UPGRADE("upgrade"),
    }

    fun notifyUpgrade(context: Context, version: String, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManagerCompat.IMPORTANCE_LOW
            val channel = NotificationChannelCompat.Builder(ChannelId.UPGRADE.value, importance)
                .setName(context.getString(R.string.upgrade_channel_name))
                .setDescription(context.getString(R.string.upgrade_channel_description))
                .build()
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notification = NotificationCompat.Builder(context, ChannelId.UPGRADE.value)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.upgrade_notification_text, version))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        with(NotificationManagerCompat.from(context)) {
            notify(NotificationId.UPGRADE.value, notification)
        }
    }
}