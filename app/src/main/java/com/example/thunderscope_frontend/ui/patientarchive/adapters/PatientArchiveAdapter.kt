package com.example.thunderscope_frontend.ui.patientarchive.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.databinding.ItemPatientArchiveRowBinding
import com.example.thunderscope_frontend.ui.utils.CaseRecordStatus

class PatientArchiveAdapter(private val onArchive: (CaseRecordResponse, Boolean) -> Unit) :
    ListAdapter<CaseRecordResponse, PatientArchiveAdapter.CaseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding = ItemPatientArchiveRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CaseViewHolder(private val binding: ItemPatientArchiveRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: CaseRecordResponse) {
            binding.apply {
                caseRecordId.text = record.id.toString()
                doctorId.text = record.doctor?.id.toString()
                patientId.text = record.patient?.id.toString()
                physicianName.text = record.doctor?.name.toString()
                patientName.text = record.patient?.name.toString()
                patientBirthdate.text = record.patient?.dateOfBirth.toString()
                patientAge.text = "(${record.patient?.age} yo)"
                patientGender.text = record.patient?.gender
                lastUpdateDate.text = record.date
                lastUpdateTime.text = record.time
                status.apply {
                    text = CaseRecordStatus.getTranslatedStringValue(record.status ?: "")?.uppercase() ?: "UNKNOWN"
                    background = root.resources.getDrawable(R.drawable.bg_status_completed, null)
                    setTextColor(root.resources.getColor(R.color.white, null))
                }
                type.text = record.type?.trim()

                btnArchive.setOnClickListener {
                    AlertDialog.Builder(binding.root.context)
                        .setTitle("Confirm Unarchive")
                        .setMessage("Are you sure you want to unarchive this record?")
                        .setPositiveButton("Yes") { _, _ ->
                            onArchive(record, false)
                        }
                        .setNegativeButton("No", null)
                        .show()
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
