package com.raphael.android.geolocatingcamera

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.raphael.android.geolocatingcamera.util.DEPARTMENT_ID
import com.raphael.android.geolocatingcamera.util.sharedPrefFile
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var customToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var firebaseMessaging:FirebaseMessaging
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseMessaging = FirebaseMessaging.getInstance()

        customToolbar = findViewById(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        navController = findNavController(R.id.main_nav_host_fragment)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        customToolbar.setupWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        createNotificationChannel(
            getString(R.string.channel_photo_alert_name),
            getString(R.string.channel_photo_alert_id)
        )

        subscribeToTopic()
    }

    override fun onNavigateUp(): Boolean {
        return navigateUp(navController, drawerLayout)
    }

    private fun createNotificationChannel(channelName: String, channelId: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = channelName
            }

            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun subscribeToTopic(){
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val departmentId = sharedPreferences.getString(DEPARTMENT_ID,"")!!
        firebaseMessaging.subscribeToTopic(departmentId)
    }

    override fun onBackPressed() {
        if(navController.currentDestination?.id == R.id.cameraFragment){
            super.onBackPressed()
        }
        navController.navigateUp()
    }
}