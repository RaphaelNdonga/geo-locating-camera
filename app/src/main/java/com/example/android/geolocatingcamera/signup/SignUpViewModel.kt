package com.example.android.geolocatingcamera.signup

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.geolocatingcamera.UserType
import com.example.android.geolocatingcamera.util.Event
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase

class SignUpViewModel(private val userType: UserType) : ViewModel() {
    private val auth = Firebase.auth
    private val functions = FirebaseFunctions.getInstance()

    private val _snackBarText = MutableLiveData<Event<String>>()
    val snackBarText: LiveData<Event<String>> = _snackBarText

    private val _loginNavigator = MutableLiveData<Event<Unit>>()
    val loginNavigator: LiveData<Event<Unit>> = _loginNavigator

    fun createUser(email: String, password: String, departmentId: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                when(userType){
                    UserType.ADMIN ->{
                        createAdminUser(email,departmentId)
                    }
                    UserType.REGULAR -> {
                        createRegularUser(email,departmentId)
                    }
                }
            } else {
                _snackBarText.value = Event("Failed to create user. Please try again")
                Log.d("SignUpViewModel", task.exception.toString())
            }
        }
    }

    private fun navigateToLogin() {
        _loginNavigator.value = Event(Unit)
    }

    private fun createAdminUser(email:String,departmentId: String){
        val data = hashMapOf("email" to email,"departmentId" to departmentId)
        functions.getHttpsCallable("addAdminCourseId").call(data).continueWith {
            navigateToLogin()
        }
    }
    private fun createRegularUser(email: String,departmentId: String){
        val data = hashMapOf("email" to email,"departmentId" to departmentId)
        functions.getHttpsCallable("addCourseId").call(data).continueWith {
            navigateToLogin()
        }
    }
}