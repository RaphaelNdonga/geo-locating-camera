package com.example.android.geolocatingcamera.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.geolocatingcamera.R
import com.example.android.geolocatingcamera.UserType
import com.example.android.geolocatingcamera.databinding.SignUpFragmentBinding
import com.example.android.geolocatingcamera.util.EventObserver
import com.example.android.geolocatingcamera.util.containsSpecialCharacters
import com.example.android.geolocatingcamera.util.containsWhiteSpace
import com.example.android.geolocatingcamera.util.isValidEmail
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: SignUpFragmentBinding
    private val arguments by navArgs<SignUpFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SignUpFragmentBinding.inflate(layoutInflater)
        arguments.userType.let {
            viewModel =
                ViewModelProvider(this, SignUpViewModelFactory(it)).get(SignUpViewModel::class.java)
            when (it) {
                UserType.ADMIN -> {
                    binding.signUpHeading.text =
                        context?.getString(R.string.adminHeading)

                }

                UserType.REGULAR -> {
                    binding.signUpHeading.text = context?.getString(R.string.regularHeading)
                }
            }
        }

        viewModel.snackBarText.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.loginNavigator.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigateUp()
        })
        binding.signUpBtn.setOnClickListener {
            val departmentId = binding.signUpDepartmentId.text.toString()
            val emailAddress = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()

            if (departmentId.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
                Snackbar.make(
                    requireView(),
                    "Please fill in all the blanks",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (departmentId.containsWhiteSpace()) {
                Snackbar.make(
                    requireView(),
                    "The department id cannot contain spaces",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (departmentId.containsSpecialCharacters()) {
                Snackbar.make(
                    requireView(),
                    "The department id cannot contain special characters or spaces",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (!emailAddress.isValidEmail()) {
                Snackbar.make(
                    requireView(),
                    "Please enter a valid email address",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Snackbar.make(
                    requireView(),
                    "The password has to be 8 characters or more",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewModel.createUser(emailAddress, password, departmentId)

        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        // TODO: Use the ViewModel
    }

}