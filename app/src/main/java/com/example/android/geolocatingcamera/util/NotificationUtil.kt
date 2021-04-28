package com.example.android.geolocatingcamera.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.android.geolocatingcamera.MainActivity
import com.example.android.geolocatingcamera.R

const val REQUEST_CODE = 1
const val NOTIFICATION_ID = 1
fun NotificationManager.sendNotification(context: Context, message: String) {

    val mainIntent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        mainIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notificationBuilder =
        NotificationCompat.Builder(context, context.getString(R.string.channel_photo_alert_id))
            .setContentIntent(pendingIntent)
            .setContentTitle("New photo has been taken")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_geo_locating_camera_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, notificationBuilder.build())
}