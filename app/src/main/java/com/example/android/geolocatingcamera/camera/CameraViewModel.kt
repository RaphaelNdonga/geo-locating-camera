package com.example.android.geolocatingcamera.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraViewModel(private val app:Application): AndroidViewModel(app) {
    private val firebaseStorage = Firebase.storage
    private val imagesRef = firebaseStorage.reference.child("images")
    private var currentPhotoPath: String? = null

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.i("CameraViewModel", "The call back has been called")

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

        Log.i("CameraViewModel", "imageViewHeight is $imageViewHeight")
        Log.i("CameraViewModel", "imageViewWidth is $imageViewWidth")

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoH = outHeight
            val photoW = outWidth

            Log.i("CameraViewModel", "decodedHeight is $photoH")
            Log.i("CameraViewModel", "decodedWidth is $photoW")


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
    fun getImagesRef() = imagesRef
    fun getCurrentPhotoPath() = currentPhotoPath
}