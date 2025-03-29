package com.example.thunderscope_frontend.ui.slidesdetail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.data.models.AnnotationItem
import com.example.thunderscope_frontend.databinding.ItemImageProcessingAnnotationBinding

class SavedAnnotationAdapter : ListAdapter<AnnotationItem, SavedAnnotationAdapter.SavedAnnotationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedAnnotationViewHolder {
        val binding = ItemImageProcessingAnnotationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedAnnotationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedAnnotationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class SavedAnnotationViewHolder(private val binding: ItemImageProcessingAnnotationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnnotationItem) {
            binding.ivAnnotation.setImageResource(item.dummyImageRes)
            binding.tvAnnotationName.text = item.label
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AnnotationItem>() {
            override fun areItemsTheSame(
                oldItem: AnnotationItem,
                newItem: AnnotationItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: AnnotationItem,
                newItem: AnnotationItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
