package com.raphael.android.geolocatingcamera.images

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raphael.android.geolocatingcamera.GeoLocatingData
import com.raphael.android.geolocatingcamera.util.DEPARTMENT_ID
import com.raphael.android.geolocatingcamera.util.sharedPrefFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ImagesViewModel(private val app: Application) : AndroidViewModel(app) {
    private val firestore = FirebaseFirestore.getInstance()
    private val sharedPreferences = app.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
    private val departmentId = sharedPreferences.getString(DEPARTMENT_ID, "")!!
    private val departmentCollection = firestore.collection(departmentId)

    private val _geoLocatingList = MutableLiveData<List<GeoLocatingData>>()
    val geoLocatingData: LiveData<List<GeoLocatingData>> = _geoLocatingList

    fun addSnapshotListener(): ListenerRegistration {
        return departmentCollection.orderBy(
            "timeStamp",
            Query.Direction.DESCENDING
        ).addSnapshotListener { querySnapshot, error ->
            _geoLocatingList.value = querySnapshot?.toObjects(GeoLocatingData::class.java)
            error?.let {
                Log.e("ImagesViewModel", "Error occurred $it")
                Toast.makeText(app, "An error occurred while fetching data", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


}