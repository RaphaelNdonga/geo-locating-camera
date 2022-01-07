package com.raphael.android.geolocatingcamera.login

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raphael.android.geolocatingcamera.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginViewModel(private val app: Application) : AndroidViewModel(app) {
    private val auth = Firebase.auth

    private val _mainNavigator = MutableLiveData<Event<Unit>>()
    val mainNavigator: LiveData<Event<Unit>> = _mainNavigator

    private val _stopLoading = MutableLiveData<Event<Unit>>()
    val stopLoading: LiveData<Event<Unit>> = _stopLoading

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            getIdToken()
        }.addOnFailureListener {
            val exception = it.message
            _stopLoading.value = Event(Unit)
            Toast.makeText(app.applicationContext, exception, Toast.LENGTH_LONG).show()
        }
    }

    private fun getIdToken() {
        val user = auth.currentUser
        val sharedPreferences = app.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        user?.getIdToken(false)?.addOnSuccessListener { result ->
            val isAdmin = result.claims["admin"] as Boolean?
            val departmentId = result.claims["departmentId"] as String?

            val userEmail = user.email

            if (userEmail.isNullOrEmpty() || departmentId == null) {
                Toast.makeText(
                    app,
                    "An error occurred while saving your details. Try logging in again",
                    Toast.LENGTH_LONG
                ).show()
                _stopLoading.value = Event(Unit)
                return@addOnSuccessListener
            }
            sharedPreferences.edit().putString(DEPARTMENT_ID, departmentId).apply()
            if (isAdmin != null) {
                sharedPreferences.edit().putBoolean(IS_ADMIN, isAdmin).apply()
            }
            sharedPreferences.edit().putString(EMAIL_ADDRESS, userEmail).apply()
            _mainNavigator.value = Event(Unit)
        }?.addOnFailureListener {
            _stopLoading.value = Event(Unit)
            Toast.makeText(
                app.applicationContext, "Failed to create user. Please try again", Toast.LENGTH_LONG
            ).show()
        }
    }
}