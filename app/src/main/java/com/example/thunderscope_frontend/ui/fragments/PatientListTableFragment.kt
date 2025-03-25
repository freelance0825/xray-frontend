package com.example.thunderscope_frontend.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI
import com.example.thunderscope_frontend.viewmodel.CaseRecordViewModel

class PatientListTableFragment : Fragment() {

    // UI Components for Filter Section
    private lateinit var timePeriodFilter: Spinner
    private lateinit var statusFilter: Spinner
    private lateinit var typeFilter: Spinner
    private lateinit var genderFilter: Spinner
    private lateinit var ageFilter: Spinner

    // Case Record View Model
    private lateinit var caseRecordViewModel: CaseRecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.patient_list_table_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components For Filter Section
        timePeriodFilter = view.findViewById(R.id.spinnerTimePeriod)
        statusFilter = view.findViewById(R.id.spinnerStatus)
        typeFilter = view.findViewById(R.id.spinnerType)
        genderFilter = view.findViewById(R.id.spinnerGender)
        ageFilter = view.findViewById(R.id.spinnerAge)

        // Initialize ViewModel Manually
        caseRecordViewModel = ViewModelProvider(requireActivity()).get(CaseRecordViewModel::class.java)

        // Observe case records data
        caseRecordViewModel.caseRecordsLiveData.observe(
            viewLifecycleOwner,
            Observer { caseRecords -> processCaseRecords(caseRecords) })

        // Observe error messages
        caseRecordViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        })

        // Fetch case records when the fragment is created
        caseRecordViewModel.fetchCaseRecords()
    }

    private fun processCaseRecords(caseRecords: List<CaseRecordUI>) {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayoutHeader) ?: return

        // Remove only the data rows, keeping the first row (header) intact
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        if (caseRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No case records found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show only the first two records
        val maxRows = 2
        for (i in 0 until minOf(caseRecords.size, maxRows)) {
            val record = caseRecords[i]

            // Inflate the existing table row layout
            val inflater = LayoutInflater.from(requireContext())
            val newRow = inflater.inflate(R.layout.patient_list_table_data_row, tableLayout, false) as TableRow

            // Bind data to views
            newRow.findViewById<TextView>(R.id.caseRecordId).text = record.caseRecordId.toString()
            newRow.findViewById<TextView>(R.id.caseRecordPatientId).text =
                record.patientId.toString()
            newRow.findViewById<TextView>(R.id.physicianName).text = record.physicianName
            newRow.findViewById<TextView>(R.id.patientName).text = record.patientName
            newRow.findViewById<TextView>(R.id.patientId).text = record.patientId.toString()
            newRow.findViewById<TextView>(R.id.patientBirthdate).text = record.patientBirthdate
            newRow.findViewById<TextView>(R.id.patientAge).text = "(${record.patientAge})"
            newRow.findViewById<TextView>(R.id.patientGender).text = record.patientGender
            newRow.findViewById<TextView>(R.id.lastUpdateDate).text = record.lastUpdateDate
            newRow.findViewById<TextView>(R.id.lastUpdateTime).text = record.lastUpdateTime
            newRow.findViewById<TextView>(R.id.status).text = record.status
            newRow.findViewById<TextView>(R.id.type).text = record.type

            // Add the new row to the TableLayout
            tableLayout.addView(newRow)
        }
    }
}