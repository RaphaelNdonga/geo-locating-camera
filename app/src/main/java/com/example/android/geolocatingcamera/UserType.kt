package com.example.android.geolocatingcamera

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class UserType : Parcelable {
    ADMIN,
    REGULAR
}