package com.example.android.geolocatingcamera.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.geolocatingcamera.MainActivity
import com.example.android.geolocatingcamera.R
import com.example.android.geolocatingcamera.UserType
import com.example.android.geolocatingcamera.databinding.SignUpFragmentBinding
import com.example.android.geolocatingcamera.util.EventObserver
import com.example.android.geolocatingcamera.util.isValidMessagingTopic
import com.example.android.geolocatingcamera.util.isValidEmail
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SignUpFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: SignUpFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SignUpFragmentBinding.inflate(layoutInflater)

        val itemList = listOf<String>("Admin", "Regular")

        val adapter = ArrayAdapter(requireContext(), R.layout.user_type_list, itemList)

        (binding.userTypeTextField.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        val app = requireActivity().application
        viewModel =
            ViewModelProvider(this, SignUpViewModelFactory(app)).get(SignUpViewModel::class.java)

        viewModel.snackBarText.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.mainNavigator.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        })

        viewModel.stopLoading.observe(viewLifecycleOwner,EventObserver{
            stopLoading()
        })

        viewModel.loginNavigator.observe(viewLifecycleOwner,EventObserver{
            findNavController().navigateUp()
        })
        binding.signUpBtn.setOnClickListener {
            val departmentId = binding.departmentIdEditTxt.text.toString()
            val emailAddress = binding.emailEditTxt.text.toString()
            val password = binding.passwordEditTxt.text.toString()
            val userTypeString = binding.userTypeTextField.editText?.text.toString()

            if (departmentId.isEmpty() || emailAddress.isEmpty() || password.isEmpty() || userTypeString.isEmpty()) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.fill_blanks),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (departmentId.isValidMessagingTopic()) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.no_special_white_space),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (!emailAddress.isValidEmail()) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.valid_email_request),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.password_requirement),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val userTypeEnum = enumValueOf<UserType>(userTypeString.toUpperCase(Locale.ROOT))

            startLoading()
            viewModel.createUser(emailAddress, password, userTypeEnum, departmentId)

        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
    }

    private fun startLoading(){
        binding.userTypeTextField.visibility = View.GONE
        binding.signUpHeading.visibility = View.GONE
        binding.signUpBtn.visibility = View.GONE
        binding.emailTextField.visibility = View.GONE
        binding.departmentIdTextField.visibility = View.GONE
        binding.passwordTextField.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading(){
        binding.userTypeTextField.visibility = View.VISIBLE
        binding.signUpHeading.visibility = View.VISIBLE
        binding.signUpBtn.visibility = View.VISIBLE
        binding.emailTextField.visibility = View.VISIBLE
        binding.departmentIdTextField.visibility = View.VISIBLE
        binding.passwordTextField.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

}