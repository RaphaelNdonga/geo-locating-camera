package com.example.android.geolocatingcamera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private var currentPhotoPath: String? = null

    private val _location = MutableLiveData<Location>()
    val location:LiveData<Location> = _location


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

    fun setLocation(location:Location){
        _location.value = location
    }

}