package com.raphael.android.geolocatingcamera.util

import android.util.Patterns

fun String.containsWhiteSpace() = contains("//s".toRegex())
fun String?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun String.isValidMessagingTopic() = contains("[^a-zA-Z0-9-_.~%]".toRegex())



