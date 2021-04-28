package com.example.android.geolocatingcamera.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.android.geolocatingcamera.util.IS_ADMIN
import com.example.android.geolocatingcamera.util.sendNotification
import com.example.android.geolocatingcamera.util.sharedPrefFile
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AdminFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("FirebaseMesService", "New token received $p0")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val isAdmin = sharedPreferences.getBoolean(IS_ADMIN,false)
        Log.i("FirebaseMesService", "Message received")

        if(isAdmin) {
            Log.i("FirebaseMesService", "Message received this user is an admin")

            val address = message.data["address"]

            address?.let {
                val notificationManager = ContextCompat.getSystemService(
                    this,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendNotification(this, it)
            }
        }
    }
}