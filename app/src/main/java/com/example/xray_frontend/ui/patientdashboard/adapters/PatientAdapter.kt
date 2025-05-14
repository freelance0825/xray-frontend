package com.example.xray_frontend.ui.patientdashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.data.models.PatientResponse
import com.example.xray_frontend.databinding.ItemPatientRecordRowBinding

class PatientAdapter : ListAdapter<PatientResponse, PatientAdapter.CaseViewHolder>(DIFF_CALLBACK) {

    var onEditClick: ((PatientResponse) -> Unit)? = null
    var onDeleteClick: ((PatientResponse) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding = ItemPatientRecordRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CaseViewHolder(private val binding: ItemPatientRecordRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: PatientResponse) {
            binding.apply {
                binding.patientId.text = record.id.toString()
                binding.patientName.text = record.name
                binding.patientBirthDate.text = record.dateOfBirth
                binding.patientAge.text = StringBuilder("(${record.age} yo)")
                binding.patientGender.text = record.gender
                binding.patientPhoneNumber.text = record.phoneNumber
                binding.patientEmail.text = record.email
                binding.patientAddress.text = record.address

                editIcon.setOnClickListener {
                    onEditClick?.invoke(record)
                }

                deleteIcon.setOnClickListener {
                    onDeleteClick?.invoke(record)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PatientResponse>() {
            override fun areItemsTheSame(oldItem: PatientResponse, newItem: PatientResponse): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PatientResponse, newItem: PatientResponse): Boolean {
                return oldItem == newItem
            }
        }
    }
}
