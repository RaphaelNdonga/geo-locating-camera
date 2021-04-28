package com.example.android.geolocatingcamera

import java.util.*

data class GeoLocatingData (
    val id:String = UUID.randomUUID().toString(),
    var downloadUri:String = "",
    var location:String = "",
    var timeStamp:Long = 0L
)