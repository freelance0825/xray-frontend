package com.example.thunderscope_frontend.ui.patientreport.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.databinding.ItemPatientReportRowBinding
import com.example.thunderscope_frontend.ui.utils.CaseRecordStatus
import com.example.thunderscope_frontend.ui.utils.PatientStatus
import com.google.android.material.snackbar.Snackbar

class PatientReportAdapter(private val onArchive: (CaseRecordResponse, Boolean) -> Unit) :
    ListAdapter<CaseRecordResponse, PatientReportAdapter.CaseViewHolder>(DIFF_CALLBACK) {

    // Track archived items temporarily before the undo action
    private var currentListBeforeUndo: List<CaseRecordResponse> = currentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding =
            ItemPatientReportRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CaseViewHolder(private val binding: ItemPatientReportRowBinding) :
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
                status.apply { text = PatientStatus.getTranslatedStringValue(record.status ?: "")?.uppercase() ?: "UNKNOWN"
                    background = root.resources.getDrawable(R.drawable.bg_status_completed, null)
                    setTextColor(root.resources.getColor(R.color.white, null))
                }
                type.text = record.type?.trim()
            }

            binding.btnArchive.setOnClickListener {
                // Archive the item
                onArchive(record, true)
                // Store the current list before modification (for Undo)
                currentListBeforeUndo = currentList

                // Temporarily remove the item from the list
                val updatedList = currentList.filter { it != record }
                submitList(updatedList)

                // Show Snackbar with undo option
                Snackbar.make(binding.root, "Item archived", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        // Undo the archiving: restore the item back to the list
                        val restoredList = currentListBeforeUndo.toMutableList()
                        restoredList.add(record) // Restore the item
                        submitList(restoredList) // Update the list with the restored item
                        onArchive(record, false) // Unarchive the record
                    }
                    // Set custom 2-second duration
                    duration = Snackbar.LENGTH_SHORT
                    setDuration(2000) // 2 seconds duration
                }.show()
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
