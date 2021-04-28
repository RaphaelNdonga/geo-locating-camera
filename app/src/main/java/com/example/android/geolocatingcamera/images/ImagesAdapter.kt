package com.example.android.geolocatingcamera.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.geolocatingcamera.GeoLocatingData
import com.example.android.geolocatingcamera.databinding.ImageItemBinding

class ImagesAdapter :
    ListAdapter<GeoLocatingData, ImagesAdapter.ImagesViewHolder>(DiffUtilCallback) {
    object DiffUtilCallback : DiffUtil.ItemCallback<GeoLocatingData>() {
        override fun areItemsTheSame(oldItem: GeoLocatingData, newItem: GeoLocatingData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: GeoLocatingData,
            newItem: GeoLocatingData
        ): Boolean {
            return oldItem.downloadUri == newItem.downloadUri
                    && oldItem.location == newItem.location
                    && oldItem.timeStamp == newItem.timeStamp
        }

    }

    class ImagesViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(geoLocatingData: GeoLocatingData) {
            val downloadUri =
                geoLocatingData.downloadUri.toUri().buildUpon().scheme("https").build()
            Glide.with(itemView).load(downloadUri).into(binding.gridViewImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ImageItemBinding.inflate(layoutInflater, parent, false)
        return ImagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val geoLocatingData = getItem(position)
        holder.bind(geoLocatingData)
    }
}