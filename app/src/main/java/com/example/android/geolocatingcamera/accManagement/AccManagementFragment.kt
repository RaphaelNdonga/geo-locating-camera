package com.example.android.geolocatingcamera.accManagement

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.geolocatingcamera.LoginActivity
import com.example.android.geolocatingcamera.R
import com.example.android.geolocatingcamera.databinding.AccManagementFragmentBinding
import com.example.android.geolocatingcamera.util.DEPARTMENT_ID
import com.example.android.geolocatingcamera.util.EMAIL_ADDRESS
import com.example.android.geolocatingcamera.util.IS_ADMIN
import com.example.android.geolocatingcamera.util.sharedPrefFile
import com.google.firebase.auth.FirebaseAuth

class AccManagementFragment : Fragment() {

    companion object {
        fun newInstance() = AccManagementFragment()
    }

    private lateinit var viewModel: AccManagementViewModel
    private lateinit var binding: AccManagementFragmentBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        binding = AccManagementFragmentBinding.inflate(layoutInflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val departmentId = sharedPreferences.getString(DEPARTMENT_ID, "")!!
        val isAdmin = sharedPreferences.getBoolean(IS_ADMIN, false)
        val emailAddress = sharedPreferences.getString(EMAIL_ADDRESS, "")!!

        binding.departmentIdDetail.text = departmentId
        binding.emailDetail.text = emailAddress
        val adminText = "Admin:$isAdmin"
        binding.isAdmin.text = adminText

        binding.logOutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.deleteBtn.setOnClickListener {
            showAlertDialog()
        }
        return binding.root
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Delete") { _, _ ->
                run {
                    auth.currentUser?.delete()
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(requireContext(),LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .setMessage("Are you sure you want to delete?")
            .create().show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AccManagementViewModel::class.java)
        // TODO: Use the ViewModel
    }

}