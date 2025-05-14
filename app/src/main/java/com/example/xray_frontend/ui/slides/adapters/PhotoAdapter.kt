package com.example.xray_frontend.ui.slides.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.data.models.PhotoItem
import com.example.xray_frontend.databinding.ItemPhotoGalleryColumnsBinding

class PhotoAdapter : ListAdapter<PhotoItem, PhotoAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((PhotoItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoGalleryColumnsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoGalleryColumnsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotoItem) {
            binding.ivPhoto.setImageResource(item.imageRes)
            binding.tvPhotoName.text = item.name
            binding.tvPhotoHour.text = item.hour

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PhotoItem>() {
            override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
