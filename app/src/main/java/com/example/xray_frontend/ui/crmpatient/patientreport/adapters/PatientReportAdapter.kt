package com.example.xray_frontend.ui.crmpatient.patientreport.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.CaseRecordResponse
import com.example.xray_frontend.databinding.ItemPatientReportRowBinding
import com.example.xray_frontend.ui.crmpatient.patientreportpdf.PatientReportPdfActivity
import com.example.xray_frontend.ui.utils.enums.PatientStatus
import com.google.android.material.snackbar.BaseTransientBottomBar
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
                status.apply {
                    text = PatientStatus.getTranslatedStringValue(record.status ?: "")?.uppercase()
                        ?: "UNKNOWN"
                    background = root.resources.getDrawable(R.drawable.bg_status_completed, null)
                    setTextColor(root.resources.getColor(R.color.white, null))
                }
                type.text = record.type?.trim()
            }

            binding.btnArchive.setOnClickListener {
                // Store the current list BEFORE removing the item
                currentListBeforeUndo = currentList.toList()

                // Archive the item
                onArchive(record, true)

                // Temporarily remove the item from the list
                val updatedList = currentList.filter { it != record }
                submitList(updatedList)

                // Show Snackbar with undo option
                Snackbar.make(binding.root, "Item archived", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        // Undo the archiving: restore the item back to the list
                        val restoredList = currentListBeforeUndo.toMutableList()
                        restoredList.add(record)
                        submitList(restoredList.distinctBy { it.id }) // Ensure no duplicates
                        onArchive(record, false) // Unarchive the record
                    }
                    duration = 2000
                }.show()
            }

            binding.btnReportPdf.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PatientReportPdfActivity::class.java).apply {
                    putExtra(PatientReportPdfActivity.EXTRA_CASE_ID, record.id?.toLong() ?: -1L)
                }
                context.startActivity(intent)
            }

        }

    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CaseRecordResponse>() {
            override fun areItemsTheSame(
                oldItem: CaseRecordResponse,
                newItem: CaseRecordResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CaseRecordResponse,
                newItem: CaseRecordResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
