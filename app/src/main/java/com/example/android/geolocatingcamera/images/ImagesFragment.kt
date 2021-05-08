package com.example.android.geolocatingcamera.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.geolocatingcamera.databinding.ImagesFragmentBinding
import com.google.firebase.firestore.ListenerRegistration

class ImagesFragment : Fragment() {
    private lateinit var binding: ImagesFragmentBinding
    private lateinit var viewModel: ImagesViewModel
    private lateinit var snapshotListener: ListenerRegistration
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImagesFragmentBinding.inflate(layoutInflater)
        val app = requireActivity().application
        viewModel =
            ViewModelProvider(this, ImagesViewModelFactory(app)).get(ImagesViewModel::class.java)

        val adapter = ImagesAdapter(ImagesAdapter.ImagesListener {
            findNavController().navigate(
                ImagesFragmentDirections.actionImagesFragmentToImageDetailFragment(
                    it
                )
            )
        })


        viewModel.geoLocatingData.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        binding.imagesRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        snapshotListener = viewModel.addSnapshotListener()
    }

    override fun onPause() {
        super.onPause()
        snapshotListener.remove()
    }
}