package com.example.thunderscope_frontend.ui.todolistdashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.databinding.ItemTodoListRecordRowBinding
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import com.example.thunderscope_frontend.ui.utils.CaseRecordStatus

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
                doctorId.text = record.doctor?.id.toString()
                patientName.text = record.patient?.name.toString()
                patientId.text = record.patient?.id.toString()
                patientBirthdate.text = record.patient?.dateOfBirth.toString()
                patientAge.text = StringBuilder("(${record.patient?.age} yo)")
                patientGender.text = record.patient?.gender
                lastUpdateDate.text = record.date
                lastUpdateTime.text = record.time

                status.apply {
                    text = CaseRecordStatus.getTranslatedStringValue(record.status ?: "")?.uppercase() ?: "UNKNOWN"
                    background = when (record.status?.trim()) {
                        CaseRecordStatus.COMPLETED.name -> resources.getDrawable(R.drawable.bg_status_completed, null)
                        CaseRecordStatus.FOR_REVIEW.name -> resources.getDrawable(R.drawable.bg_status_for_review, null)
                        else -> resources.getDrawable(R.drawable.bg_status_default, null)
                    }
                    setTextColor(
                        when (record.status?.trim()) {
                            CaseRecordStatus.COMPLETED.name -> resources.getColor(R.color.white, null)
                            else -> resources.getColor(R.color.blue_text, null)
                        }
                    )
                }

                assessmentImage1.setImageResource(R.drawable.placeholder_image)
                assessmentImage2.setImageResource(R.drawable.placeholder_image)
                assessmentImage3.setImageResource(R.drawable.placeholder_image)

                // Load images properly if available
                if (record.slides.isNotEmpty()) {
                    record.slides.getOrNull(0)?.mainImage?.let {
                        assessmentImage1.setImageBitmap(Base64Helper.convertToBitmap(it))
                    }

                    record.slides.getOrNull(1)?.mainImage?.let {
                        assessmentImage2.setImageBitmap(Base64Helper.convertToBitmap(it))
                    }

                    record.slides.getOrNull(2)?.mainImage?.let {
                        assessmentImage3.setImageBitmap(Base64Helper.convertToBitmap(it))
                    }
                }

                assessmentImageCount.text = when {
                    record.slides.isEmpty() -> "0" // No images
                    record.slides.size <= 3 -> record.slides.size.toString() // Show total if 3 or less
                    else -> "+${record.slides.size - 3}" // Show "+extra count" if more than 3
                }

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
