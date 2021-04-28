package com.example.android.geolocatingcamera

import java.util.*

data class GeoLocatingData (
    val id:String = UUID.randomUUID().toString(),
    val downloadUri:String,
    val location:String,
    val timeStamp:Long
)