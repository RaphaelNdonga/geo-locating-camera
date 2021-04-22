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
import java.io.IOException
import java.util.*

const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_LOCATION_PERMISSION = 2
const val REQUEST_CHECK_SETTINGS = 3

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationCallback: LocationCallback
    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private var geoCoder: Geocoder? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        geoCoder = Geocoder(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        ).get(MainViewModel::class.java)

        locationCallback = object : LocationCallback() {
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
                bestLocation?.let { viewModel.setLocation(it) }
            }
        }

        viewModel.location.observe(this, { location ->
            try {
                stopLoading()
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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLoading()
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
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setPic() {
        binding.imageView.apply {
            setImageBitmap(viewModel.getCameraPhotoBitmap(height, width))
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

}