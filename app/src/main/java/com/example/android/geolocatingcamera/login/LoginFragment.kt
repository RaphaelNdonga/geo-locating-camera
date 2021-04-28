package com.example.android.geolocatingcamera.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.android.geolocatingcamera.MainActivity
import com.example.android.geolocatingcamera.R
import com.example.android.geolocatingcamera.UserType
import com.example.android.geolocatingcamera.databinding.LoginFragmentBinding
import com.example.android.geolocatingcamera.util.DEPARTMENT_ID
import com.example.android.geolocatingcamera.util.IS_ADMIN
import com.example.android.geolocatingcamera.util.isValidEmail
import com.example.android.geolocatingcamera.util.sharedPrefFile
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var binding: LoginFragmentBinding

    private lateinit var viewModel: LoginViewModel

    private lateinit var auth: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        auth = Firebase.auth
        binding = LoginFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        val adminSignUp = binding.adminSignUpBtn
        val regularSignUp = binding.regularSignUpBtn

        adminSignUp.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment(
                    UserType.ADMIN
                )
            )
        }

        regularSignUp.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment(
                    UserType.REGULAR
                )
            )
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEditTxt.text.toString()
            val password = binding.passwordEditTxt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(requireView(), R.string.fill_blanks, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                getIdToken()
            }.addOnFailureListener {
                val exception = it.message
                Toast.makeText(context, exception, Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun goToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun getIdToken() {
        val user = auth.currentUser
        user?.getIdToken(false)?.addOnSuccessListener { result ->
            val isAdmin = result.claims["admin"] as Boolean?
            val departmentId = result.claims["departmentId"] as String?

            departmentId?.let {
                sharedPreferences.edit().putString(DEPARTMENT_ID, it).apply()
            }
            isAdmin?.let {
                sharedPreferences.edit().putBoolean(IS_ADMIN, it).apply()
            }
            goToMainActivity()
        }?.addOnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }

}