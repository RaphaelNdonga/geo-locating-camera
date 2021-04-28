package com.example.android.geolocatingcamera.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.geolocatingcamera.databinding.ImagesFragmentBinding

class ImagesFragment : Fragment() {
    private lateinit var binding: ImagesFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImagesFragmentBinding.inflate(layoutInflater)

        return binding.root
    }
}