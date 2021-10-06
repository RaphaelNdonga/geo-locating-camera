package com.example.android.geolocatingcamera

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class UserType : Parcelable {
    ADMIN,
    REGULAR
}