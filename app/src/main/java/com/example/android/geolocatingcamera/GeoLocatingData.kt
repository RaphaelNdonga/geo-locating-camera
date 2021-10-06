package com.example.android.geolocatingcamera

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class GeoLocatingData (
    val id:String = UUID.randomUUID().toString(),
    var downloadUri:String = "",
    var location:String = "",
    var timeStamp:Long = 0L
):Parcelable