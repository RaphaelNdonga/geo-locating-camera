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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.android.geolocatingcamera.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.IOException
import java.lang.NullPointerException
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
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        binding.button.setOnClickListener {
            //Check the settings whenever the button is clicked. This is intended behaviour

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client = LocationServices.getSettingsClient(this)
            val task = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                //only when location has been connected successfully are these initialized
                if (geoCoder == null) {
                    geoCoder = Geocoder(this)
                }

                if (fusedLocationProviderClient == null) {
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(this)
                }
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
            setText()
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
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
        try {
            val location = viewModel.location.value!!
            geoCoder?.getFromLocation(location.latitude, location.longitude, 1)
            takePictureIntent()

        } catch (ex: IOException) {
            //The geo coder is null
            Toast.makeText(this, R.string.internet_request, Toast.LENGTH_LONG).show()
        }catch (ex:NullPointerException){
            // The location has not been set
            Toast.makeText(this, R.string.internet_request, Toast.LENGTH_LONG).show()

        }
    }

    private fun setText() {

        val location = viewModel.location.value!!

        val addresses = geoCoder?.getFromLocation(location.latitude, location.longitude, 1)

        addresses?.let {
            val address = it[0].getAddressLine(0) ?: ""

            val text = "Taken from $address"
            binding.textView.text = text
        }
    }

    private fun setPic() {
        binding.imageView.apply {
            setImageBitmap(viewModel.getCameraPhotoBitmap(height, width))
        }
    }
}