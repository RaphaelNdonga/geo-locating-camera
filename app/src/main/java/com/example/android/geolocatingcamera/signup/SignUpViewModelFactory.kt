package com.example.android.geolocatingcamera.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.geolocatingcamera.UserType

class SignUpViewModelFactory(private val userType: UserType):ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SignUpViewModel(userType) as T
    }
}