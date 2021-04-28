package com.example.android.geolocatingcamera.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.geolocatingcamera.util.*
import com.example.android.geolocatingcamera.databinding.CameraFragmentBinding
import com.example.android.geolocatingcamera.util.DEPARTMENT_ID
import com.example.android.geolocatingcamera.util.IS_ADMIN
import com.example.android.geolocatingcamera.util.sharedPrefFile
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import java.io.File
import java.io.IOException

class CameraFragment : Fragment() {
    private lateinit var binding: CameraFragmentBinding
    private lateinit var viewModel: CameraViewModel

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var departmentId: String
    private var isAdmin = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CameraFragmentBinding.inflate(layoutInflater)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        departmentId = sharedPreferences.getString(DEPARTMENT_ID, "")!!
        isAdmin = sharedPreferences.getBoolean(IS_ADMIN, false)

        Toast.makeText(requireContext(), departmentId, Toast.LENGTH_LONG).show()

        val app = requireActivity().application
        viewModel =
            ViewModelProvider(this, CameraViewModelFactory(app)).get(CameraViewModel::class.java)

        viewModel.initializeGeocoder()

        viewModel.location.observe(viewLifecycleOwner, { location ->
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
                stopLoadingImage()
                Toast.makeText(
                    requireContext(),
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
                    requireContext(),
                    "The location needs to be detected before you can take a photo",
                    Toast.LENGTH_LONG
                ).show()
                startLocationUpdates()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.location.value != null) {
            return
        }
        startLoading()
        val locationRequest = viewModel.getLocationRequest()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
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
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_LONG)
                .show()
        }
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }

    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile = try {
                    viewModel.createImageFile()
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Failed to create image ", Toast.LENGTH_LONG)
                        .show()
                    null
                }
                photoFile?.let {
                    val photoUri =
                        FileProvider.getUriForFile(
                            requireContext(),
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            setPic()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
                    requireContext(),
                    "com.example.android.fileprovider",
                    file
                )
            val imagesRef = viewModel.createImagesRef(departmentId).child(file.name)

            startLoadingImage()

            val task = imagesRef.putFile(photoUri)

            task.addOnFailureListener { exception ->
                Log.i("MainActivity", "$exception")
                Toast.makeText(
                    requireContext(),
                    "Upload failed. Please try again",
                    Toast.LENGTH_LONG
                )
                    .show()
                stopLoadingImage()
            }.addOnSuccessListener {
                binding.imageView.apply {
                    setImageBitmap(viewModel.getCameraPhotoBitmap(height, width))
                }
                Toast.makeText(requireContext(), "Uploaded successfully", Toast.LENGTH_LONG)
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

    private fun startLoadingImage() {
        binding.imageProgressBar.visibility = View.VISIBLE
        binding.button.isClickable = false
    }

    private fun stopLoadingImage() {
        binding.imageProgressBar.visibility = View.GONE
        binding.button.isClickable = true
    }
}