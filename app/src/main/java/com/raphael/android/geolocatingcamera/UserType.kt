package com.raphael.android.geolocatingcamera

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class UserType : Parcelable {
    ADMIN,
    REGULAR
}