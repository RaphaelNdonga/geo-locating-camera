package com.example.android.geolocatingcamera

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.android.geolocatingcamera.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_LOCATION_PERMISSION = 2

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.button.setOnClickListener {
            if (fusedLocationProviderClient != null) {
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_internet_request),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        ).get(MainViewModel::class.java)


        viewModel.location.observe(this, {
            val text = "The latitude is ${it.latitude} and the longitude is ${it.longitude}"
            binding.textView.text = text
        })
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
            getLastLocation()
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

    private fun setPic() {
        binding.imageView.apply {
            setImageBitmap(viewModel.getCameraPhotoBitmap(height, width))
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location->
            if(location == null){
                Toast.makeText(this,R.string.location_internet_request,Toast.LENGTH_LONG).show()
            }else{
                viewModel.setLocation(location)
                //only after we have successfully obtained the location can the picture be taken
                takePictureIntent()
            }

        }
    }
}