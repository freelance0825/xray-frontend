package com.example.thunderscope_frontend.ui.slidesdetail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.data.models.AnnotationItem
import com.example.thunderscope_frontend.data.models.AnnotationResponse
import com.example.thunderscope_frontend.databinding.ItemImageProcessingAnnotationBinding
import com.example.thunderscope_frontend.ui.utils.Base64Helper

class SavedAnnotationAdapter : ListAdapter<AnnotationResponse, SavedAnnotationAdapter.SavedAnnotationViewHolder>(DIFF_CALLBACK) {

    val onImageClick: ((annotationImageBase64: String) -> Unit)? = null

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

        fun bind(item: AnnotationResponse) {
            binding.ivAnnotation.setImageBitmap(Base64Helper.convertToBitmap(item.annotatedImage))
            binding.tvAnnotationName.text = item.label

            binding.root.setOnClickListener {
                onImageClick?.invoke(item.annotatedImage ?: "")
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AnnotationResponse>() {
            override fun areItemsTheSame(
                oldItem: AnnotationResponse,
                newItem: AnnotationResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: AnnotationResponse,
                newItem: AnnotationResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
