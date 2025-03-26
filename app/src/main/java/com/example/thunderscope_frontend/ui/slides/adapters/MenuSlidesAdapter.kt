package com.example.thunderscope_frontend.ui.slides.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.databinding.ItemSlidesIndicatorColumnBinding

class MenuSlidesAdapter : ListAdapter<SlidesItem, MenuSlidesAdapter.SlidesViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlidesViewHolder {
        val binding = ItemSlidesIndicatorColumnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlidesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlidesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlidesViewHolder(private val binding: ItemSlidesIndicatorColumnBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SlidesItem) {
            binding.tvSlidesId.text = item.id.toString()

            if (item.isCurrentlySelected) {
                binding.tvSlidesId.setTypeface(null, Typeface.BOLD)
                binding.menuIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.blue))
            } else {
                binding.tvSlidesId.setTypeface(null, Typeface.NORMAL)
                binding.menuIndicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
