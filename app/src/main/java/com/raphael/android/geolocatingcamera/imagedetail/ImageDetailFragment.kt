package com.raphael.android.geolocatingcamera.imagedetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.raphael.android.geolocatingcamera.R
import com.raphael.android.geolocatingcamera.databinding.ImageDetailFragmentBinding
import com.raphael.android.geolocatingcamera.util.formatTimestamp

class ImageDetailFragment : Fragment() {

    companion object {
        fun newInstance() = ImageDetailFragment()
    }

    private lateinit var viewModel: ImageDetailViewModel
    private lateinit var binding: ImageDetailFragmentBinding

    private val navArgs by navArgs<ImageDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImageDetailFragmentBinding.inflate(layoutInflater, container, false)

        val geoLocatingData = navArgs.geoLocatingData
        geoLocatingData?.let {
            binding.locationDetail.text = geoLocatingData.location
            binding.timeStampDetail.text = formatTimestamp(geoLocatingData.timeStamp)
            val downloadUri =
                geoLocatingData.downloadUri.toUri().buildUpon().scheme("https").build()
            Glide.with(this).load(downloadUri).apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_connection_error)
            ).into(binding.imageDetail)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ImageDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}