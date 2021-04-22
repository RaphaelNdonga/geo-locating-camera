package com.example.android.geolocatingcamera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.location.LocationProvider
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private var currentPhotoPath: String? = null

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i("MainActivity", "The call back has been called")

            val locationList = locationResult.locations

            var bestLocation: Location? = null
            locationList.forEach {
                if (bestLocation == null || it.accuracy > bestLocation!!.accuracy) {
                    bestLocation = it
                }
            }
            bestLocation?.let { setLocation(it) }
        }
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private var geoCoder: Geocoder? = null
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmSS").format(Date())
        val storageDir = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_$timeStamp", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getCameraPhotoBitmap(imageViewHeight: Int, imageViewWidth: Int): Bitmap {
        //We don't need to null check currentPhotoPath because it will have already been set.

        Log.i("MainActivity", "imageViewHeight is $imageViewHeight")
        Log.i("MainActivity", "imageViewWidth is $imageViewWidth")

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoH = outHeight
            val photoW = outWidth

            Log.i("MainActivity", "decodedHeight is $photoH")
            Log.i("MainActivity", "decodedWidth is $photoW")


            val scaleFactor =
                Math.max(1, Math.min(photoW / imageViewWidth, photoH / imageViewHeight))

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }

        return BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    }

    fun setLocation(location: Location) {
        _location.value = location
    }

    fun getLocationCallback() = locationCallback

    fun getLocationRequest(): LocationRequest = locationRequest

    fun initializeGeocoder(){
        geoCoder = Geocoder(app)
    }

    fun getFusedLocationProviderClient() = fusedLocationProviderClient
    fun getGeocoder() = geoCoder

}