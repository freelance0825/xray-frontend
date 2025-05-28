package com.example.xray_frontend.ui.slides.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.databinding.ItemSlidesColumnBinding
import com.example.xray_frontend.ui.utils.helpers.Base64Helper

class SlidesAdapter : ListAdapter<SlidesItem, SlidesAdapter.SlidesViewHolder>(DIFF_CALLBACK) {
    var onItemClick: ((SlidesItem, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlidesViewHolder {
        val binding = ItemSlidesColumnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlidesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlidesViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class SlidesViewHolder(private val binding: ItemSlidesColumnBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SlidesItem, position: Int) {
            binding.apply {
                cbSlides.isChecked = item.isActive

                ivSlides.setImageBitmap(Base64Helper.convertToBitmap(item.mainImage))
                tvSlidesId.text = item.id.toString()
                tvQrCode.text = item.qrCode

                root.setOnClickListener {
                    onItemClick?.invoke(item, position)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SlidesItem>() {
            override fun areItemsTheSame(oldItem: SlidesItem, newItem: SlidesItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SlidesItem, newItem: SlidesItem) = oldItem == newItem
        }
    }
}
