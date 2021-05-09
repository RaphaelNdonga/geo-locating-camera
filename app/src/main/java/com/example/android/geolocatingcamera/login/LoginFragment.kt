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
import com.example.android.geolocatingcamera.databinding.LoginFragmentBinding
import com.example.android.geolocatingcamera.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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
        val departmentId = sharedPreferences.getString(DEPARTMENT_ID,"")!!
        if (auth.currentUser != null && departmentId.isNotEmpty()) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding = LoginFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        viewModel.mainNavigator.observe(viewLifecycleOwner,EventObserver{
            goToMainActivity()
        })

        viewModel.stopLoading.observe(viewLifecycleOwner,EventObserver{
            stopLoading()
        })


        val createAccBtn = binding.createAccBtn

        createAccBtn.setOnClickListener {
            findNavController().navigate(
                R.id.signUpFragment
            )
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEditTxt.text.toString()
            val password = binding.passwordEditTxt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(requireView(), R.string.fill_blanks, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startLoading()
            viewModel.loginUser(email,password)
        }

        return binding.root
    }

    private fun goToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun startLoading(){
        binding.appLogo.visibility = View.GONE
        binding.appName.visibility = View.GONE
        binding.emailTextField.visibility = View.GONE
        binding.passwordTextField.visibility = View.GONE
        binding.createAccBtn.visibility = View.GONE
        binding.signUpTxt.visibility = View.GONE
        binding.loginBtn.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading(){
        binding.appLogo.visibility = View.VISIBLE
        binding.appName.visibility = View.VISIBLE
        binding.emailTextField.visibility = View.VISIBLE
        binding.passwordTextField.visibility = View.VISIBLE
        binding.createAccBtn.visibility = View.VISIBLE
        binding.signUpTxt.visibility = View.VISIBLE
        binding.loginBtn.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

}