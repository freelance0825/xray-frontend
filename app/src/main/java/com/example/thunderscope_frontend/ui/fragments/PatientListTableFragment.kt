package com.example.thunderscope_frontend.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.slides.SlidesActivity
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI
import com.example.thunderscope_frontend.viewmodel.CaseRecordViewModel

class PatientListTableFragment : Fragment() {

    // UI Components for Filter Section
    private lateinit var timePeriodFilter: Spinner
    private lateinit var statusFilter: Spinner
    private lateinit var typeFilter: Spinner
    private lateinit var genderFilter: Spinner
    private lateinit var ageFilter: Spinner

    // UI Components for Pagination Section
    private lateinit var textPagination: TextView
    private lateinit var btnNextPage: ImageButton
    private lateinit var btnPrevPage: ImageButton

    // Case Record View Model
    private lateinit var caseRecordViewModel: CaseRecordViewModel

    // Pagination variables
    private var currentPage = 0
    private val recordsPerPage = 5
    private var totalRecords = 0
    private var caseRecords: List<CaseRecordUI> = emptyList()

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

        // Initialize UI components For Pagination Section
        textPagination = view.findViewById(R.id.textPagination)
        btnNextPage = view.findViewById(R.id.btnNextPage)
        btnPrevPage = view.findViewById(R.id.btnPrevPage)

        // Initialize ViewModel Manually
        caseRecordViewModel =
            ViewModelProvider(requireActivity()).get(CaseRecordViewModel::class.java)

        // Observe case records data
        caseRecordViewModel.caseRecordsLiveData.observe(
            viewLifecycleOwner,
            Observer { records ->
                caseRecords = records
                totalRecords = caseRecords.size
                currentPage = 0 // Reset to first page
                updateTable()
            })

        // Observe error messages
        caseRecordViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        })

        // Fetch case records when the fragment is created
        caseRecordViewModel.fetchCaseRecords()
        // Pagination button listeners
        btnNextPage.setOnClickListener {
            if ((currentPage + 1) * recordsPerPage < totalRecords) {
                currentPage++
                updateTable()
            }
        }

        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateTable()
            }
        }
    }

    private fun updateTable() {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayoutHeader) ?: return

        // Remove only the data rows, keeping the first row (header) intact
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        if (caseRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No case records found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get paginated data
        val start = currentPage * recordsPerPage
        val end = minOf(start + recordsPerPage, totalRecords)
        val paginatedRecords = caseRecords.subList(start, end)

        for (record in paginatedRecords) {
            val inflater = LayoutInflater.from(requireContext())
            val newRow = inflater.inflate(R.layout.patient_list_table_data_row, tableLayout, false) as TableRow

            // Bind data to views
            newRow.findViewById<TextView>(R.id.caseRecordId).text = record.caseRecordId.toString()
            newRow.findViewById<TextView>(R.id.caseRecordPatientId).text = record.patientId.toString()
            newRow.findViewById<TextView>(R.id.physicianName).text = record.physicianName
            newRow.findViewById<TextView>(R.id.patientName).text = record.patientName
            newRow.findViewById<TextView>(R.id.patientId).text = record.patientId.toString()
            newRow.findViewById<TextView>(R.id.patientBirthdate).text = record.patientBirthdate
            newRow.findViewById<TextView>(R.id.patientAge).text = "(${record.patientAge})"
            newRow.findViewById<TextView>(R.id.patientGender).text = record.patientGender
            newRow.findViewById<TextView>(R.id.lastUpdateDate).text = record.lastUpdateDate
            newRow.findViewById<TextView>(R.id.lastUpdateTime).text = record.lastUpdateTime
            newRow.findViewById<TextView>(R.id.status).apply {
                text = record.status.uppercase() // Convert to uppercase for display in XML

                // Change background based on status (case-insensitive)
                background = when (record.status.uppercase()) {
                    "COMPLETED" -> resources.getDrawable(R.drawable.bg_status_completed, null)
                    "FOR REVIEW" -> resources.getDrawable(R.drawable.bg_status_for_review, null)
                    else -> resources.getDrawable(R.drawable.bg_status_for_review, null)
                }

                // Change text color based on status
                setTextColor(
                    when (record.status.uppercase()) {
                        "COMPLETED" -> resources.getColor(R.color.white, null)
                        else -> resources.getColor(R.color.blue, null)
                    }
                )
            }

            newRow.findViewById<TextView>(R.id.type).text = record.type

            newRow.setOnClickListener {
                val intent = Intent(requireContext(), SlidesActivity::class.java)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD, record)
                startActivity(intent)
            }

            // Add the new row to the TableLayout
            tableLayout.addView(newRow)
        }

        // Update pagination text
        textPagination.text = "${start + 1}-$end of $totalRecords"

        // Enable/disable buttons based on page
        btnPrevPage.isEnabled = currentPage > 0
        btnNextPage.isEnabled = end < totalRecords
    }
}