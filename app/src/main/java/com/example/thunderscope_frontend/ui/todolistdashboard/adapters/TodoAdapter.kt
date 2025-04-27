package com.example.thunderscope_frontend.ui.todolistdashboard.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.databinding.ItemCaseRecordRowBinding
import com.example.thunderscope_frontend.databinding.ItemTodoListRecordRowBinding
import com.example.thunderscope_frontend.ui.slides.SlidesActivity
import com.example.thunderscope_frontend.ui.utils.Base64Helper

class TodoAdapter : ListAdapter<CaseRecordResponse, TodoAdapter.CaseViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((CaseRecordResponse) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding = ItemTodoListRecordRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CaseViewHolder(private val binding: ItemTodoListRecordRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: CaseRecordResponse) {
            binding.apply {
                caseRecordId.text = record.id.toString()
                caseRecordPatientId.text = record.patient?.id.toString()
                todo.text = record.todo.toString()
                patientName.text = record.patient?.name.toString()
                patientBirthdate.text = record.patient?.dateOfBirth.toString()
                patientAge.text = StringBuilder("(${record.patient?.age} yo)")
                patientGender.text = record.patient?.gender
                lastUpdateDate.text = record.date
                lastUpdateTime.text = record.time

                status.apply {
                    text = record.status?.uppercase()
                    background = when (record.status?.lowercase()?.trim()) {
                        "completed" -> resources.getDrawable(R.drawable.bg_status_completed, null)
                        "for review" -> resources.getDrawable(R.drawable.bg_status_for_review, null)
                        else -> resources.getDrawable(R.drawable.bg_status_default, null)
                    }
                    setTextColor(
                        when (record.status?.lowercase()?.trim()) {
                            "completed" -> resources.getColor(R.color.white, null)
                            else -> resources.getColor(R.color.blue_text, null)
                        }
                    )
                }

                assessmentImage1.setImageResource(R.drawable.placeholder_image)
                assessmentImage2.setImageResource(R.drawable.placeholder_image)
                assessmentImage3.setImageResource(R.drawable.placeholder_image)

                // Load images properly if available
                if (record.slides.isNotEmpty()) {
                    record.slides.getOrNull(0)?.let {
                        assessmentImage1.setImageBitmap(Base64Helper.convertToBitmap(it.mainImage))
                    }

                    record.slides.getOrNull(1)?.let {
                        assessmentImage2.setImageBitmap(Base64Helper.convertToBitmap(it.mainImage))
                    }

                    record.slides.getOrNull(2)?.let {
                        assessmentImage3.setImageBitmap(Base64Helper.convertToBitmap(it.mainImage))
                    }
                }

                assessmentImageCount.text = StringBuilder("+${record.slides.size}")

                assessmentImageContainer.setOnClickListener {
                    onItemClick?.invoke(record)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CaseRecordResponse>() {
            override fun areItemsTheSame(oldItem: CaseRecordResponse, newItem: CaseRecordResponse): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CaseRecordResponse, newItem: CaseRecordResponse): Boolean {
                return oldItem == newItem
            }
        }
    }
}
