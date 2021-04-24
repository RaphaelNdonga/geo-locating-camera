package com.example.android.geolocatingcamera

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.android.geolocatingcamera.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.File
import java.io.IOException
import java.util.*

const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_LOCATION_PERMISSION = 2
const val REQUEST_CHECK_SETTINGS = 3

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        ).get(MainViewModel::class.java)

        viewModel.initializeGeocoder()

        viewModel.location.observe(this, { location ->
            try {
                stopLoading()
                val geoCoder = viewModel.getGeocoder()
                val addresses = geoCoder?.getFromLocation(location.latitude, location.longitude, 1)

                addresses?.let {
                    val address = it[0].getAddressLine(0) ?: ""

                    val text = "Location: $address"
                    binding.textView.text = text
                }
            } catch (ex: IOException) {
                Toast.makeText(
                    this,
                    "The geocoder has not been initialized properly. Please check your internet connection",
                    Toast.LENGTH_LONG
                ).show()
                binding.textView.text = ""
            }
        })

        binding.button.setOnClickListener {
            if (!binding.textView.text.isNullOrEmpty()) {
                takePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "The location needs to be detected before you can take a photo",
                    Toast.LENGTH_LONG
                ).show()
                startLocationUpdates()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLoading()
        val locationRequest = viewModel.getLocationRequest()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (ex: IntentSender.SendIntentException) {
                    //Ignored
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show()
        }
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }

    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile = try {
                    viewModel.createImageFile()
                } catch (e: IOException) {
                    Toast.makeText(this, "Failed to create image ", Toast.LENGTH_LONG).show()
                    null
                }
                photoFile?.let {
                    val photoUri =
                        FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                        )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        /**
         * Because we have to ask for permission, the fusedLocationProviderClient, locationRequest and
         * locationCallback have to be in the main activity
         */
        val locationCallback = viewModel.getLocationCallback()
        val locationRequest = viewModel.getLocationRequest()
        val fusedLocationProviderClient = viewModel.getFusedLocationProviderClient()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setPic() {
        val currentPhotoPath = viewModel.getCurrentPhotoPath()
        var photoFile: File? = null

        currentPhotoPath?.let {
            photoFile = File(it)
        }

        photoFile?.let { file ->
            val photoUri =
                FileProvider.getUriForFile(
                    this,
                    "com.example.android.fileprovider",
                    file
                )
            val imagesRef = viewModel.getImagesRef().child(file.name)

            startLoadingImage()

            val task = imagesRef.putFile(photoUri)

            task.addOnFailureListener { exception->
                Log.i("MainActivity", "$exception")
                Toast.makeText(this, "Upload failed. Please try again", Toast.LENGTH_LONG)
                    .show()
                stopLoadingImage()
            }.addOnSuccessListener {
                binding.imageView.apply {
                    setImageBitmap(viewModel.getCameraPhotoBitmap(height, width))
                }
                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_LONG)
                    .show()
                stopLoadingImage()
            }
        }
    }

    private fun startLoading() {
        binding.textView.visibility = View.GONE
        binding.button.visibility = View.GONE
        binding.imageView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.loadingTxt.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        binding.textView.visibility = View.VISIBLE
        binding.button.visibility = View.VISIBLE
        binding.imageView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.loadingTxt.visibility = View.GONE
    }

    private fun startLoadingImage(){
        binding.imageProgressBar.visibility = View.VISIBLE
        binding.button.isClickable = false
    }
    private fun stopLoadingImage(){
        binding.imageProgressBar.visibility = View.GONE
        binding.button.isClickable = true
    }

}