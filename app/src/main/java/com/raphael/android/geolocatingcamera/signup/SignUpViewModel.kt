package com.raphael.android.geolocatingcamera.signup

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raphael.android.geolocatingcamera.UserType
import com.raphael.android.geolocatingcamera.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase

class SignUpViewModel(private val app: Application) : AndroidViewModel(app) {
    private val auth = Firebase.auth
    private val functions = FirebaseFunctions.getInstance()
    private val sharedPreferences = app.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)


    private val _snackBarText = MutableLiveData<Event<String>>()
    val snackBarText: LiveData<Event<String>> = _snackBarText

    private val _mainNavigator = MutableLiveData<Event<Unit>>()
    val mainNavigator: LiveData<Event<Unit>> = _mainNavigator

    private val _loginNavigator = MutableLiveData<Event<Unit>>()
    val loginNavigator: LiveData<Event<Unit>> = _loginNavigator

    private val _stopLoading = MutableLiveData<Event<Unit>>()
    val stopLoading:LiveData<Event<Unit>> = _stopLoading

    fun createUser(email: String, password: String, userType: UserType, departmentId: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                when (userType) {
                    UserType.ADMIN -> {
                        createAdminUser(email,password, departmentId)
                    }
                    UserType.REGULAR -> {
                        createRegularUser(email,password, departmentId)
                    }
                }
            } else {
                _snackBarText.value = Event("Failed to create user. Please try again")
                _stopLoading.value = Event(Unit)
                Log.d("SignUpViewModel", task.exception.toString())
            }
        }
    }

    private fun goToMainActivity() {
        _mainNavigator.value = Event(Unit)
    }

    private fun createAdminUser(email: String,password: String, departmentId: String) {
        val data = hashMapOf("email" to email, "departmentId" to departmentId)
        functions.getHttpsCallable("addAdminCourseId").call(data).continueWith {
            loginUser(email,password)
        }.addOnFailureListener {
            auth.currentUser?.delete()
            _snackBarText.value = Event("Failed to create user. Please try again")
            _stopLoading.value = Event(Unit)
        }
    }

    private fun createRegularUser(email: String,password: String, departmentId: String) {
        val data = hashMapOf("email" to email, "departmentId" to departmentId)
        functions.getHttpsCallable("addCourseId").call(data).continueWith {
            loginUser(email,password)
        }.addOnFailureListener {
            auth.currentUser?.delete()
            _snackBarText.value = Event("Failed to create user. Please try again")
            _stopLoading.value = Event(Unit)
        }
    }

    private fun getIdToken() {
        val user = auth.currentUser
        user?.getIdToken(false)?.addOnSuccessListener { result ->
            val isAdmin = result.claims["admin"] as Boolean?
            val departmentId = result.claims["departmentId"] as String?
            val userEmail = user.email

            Log.i("SignUpViewModel","isAdmin is $isAdmin")
            Log.i("SignUpViewModel","departmentId is $departmentId")
            Log.i("SignUpViewModel","useremail is $isAdmin")


            if (departmentId == null) {
                Toast.makeText(
                    app,
                    "An error occurred while saving your details. Try logging in directly",
                    Toast.LENGTH_LONG
                ).show()
                _loginNavigator.value = Event(Unit)
                return@addOnSuccessListener
            }
            sharedPreferences.edit().putString(DEPARTMENT_ID, departmentId).apply()
            sharedPreferences.edit().putString(EMAIL_ADDRESS,userEmail).apply()
            if (isAdmin != null) {
                sharedPreferences.edit().putBoolean(IS_ADMIN, isAdmin).apply()
            }
            goToMainActivity()
        }?.addOnFailureListener {
            _loginNavigator.value = Event(Unit)
            _snackBarText.value = Event("An error occurred while saving your details. Try logging in directly")
        }
    }
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            getIdToken()
        }.addOnFailureListener {
            val exception = it.message
            _stopLoading.value = Event(Unit)
            Toast.makeText(app.applicationContext, exception, Toast.LENGTH_LONG).show()
        }
    }
}