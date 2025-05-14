package com.example.xray_frontend.ui.slides.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.data.models.AnnotationResponse
import com.example.xray_frontend.databinding.ItemAnnotationRowBinding
import com.example.xray_frontend.databinding.ItemUploadAnnotationHeaderBinding
import com.example.xray_frontend.ui.utils.Base64Helper
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class AnnotationAdapter : ListAdapter<AnnotationResponse, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onHeaderClick: (() -> Unit)? = null
    var onItemClick: ((AnnotationResponse) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1

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

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemUploadAnnotationHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }

            else -> {
                val binding = ItemAnnotationRowBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.binding.root.setOnClickListener { onHeaderClick?.invoke() }
            }

            is ItemViewHolder -> {
                val item = getItem(position - 1)
                holder.bind(item)
                holder.binding.root.setOnClickListener { onItemClick?.invoke(item) }
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size + 1
    }

    inner class HeaderViewHolder(val binding: ItemUploadAnnotationHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(val binding: ItemAnnotationRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnnotationResponse) {
            binding.ivAnnotation.setImageBitmap(Base64Helper.convertToBitmap(item.annotatedImage))
            binding.tvAnnotationName.text = item.label
            binding.tvAnnotationDate.text = convertDate(item.createdAt ?: "")
            binding.tvCaseStatusAnnotation.text = item.caseRecordStatus

        }
    }

    fun convertDate(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mma")
        val outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return dateTime.format(outputFormatter)
    }
}
