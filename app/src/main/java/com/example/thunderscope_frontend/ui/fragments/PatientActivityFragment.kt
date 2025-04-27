package com.example.thunderscope_frontend.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.viewmodel.PatientRecordUI
import com.example.thunderscope_frontend.viewmodel.PatientRecordViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PatientActivityFragment : Fragment() {

    // UI Components for Filter Section
    private lateinit var timePeriodFilter: Spinner
    private lateinit var statusFilter: Spinner
    private lateinit var typeFilter: Spinner
    private lateinit var genderFilter: Spinner
    private lateinit var ageFilter: Spinner

    // UI Components for Pagination Section + All Count
    private lateinit var patientAllCount: TextView
    private lateinit var textPagination: TextView
    private lateinit var btnNextPage: ImageButton
    private lateinit var btnPrevPage: ImageButton

    // PatientResponse Record View Model
    private lateinit var patientResponseRecordViewModel: PatientRecordViewModel

    // Pagination variables
    private var currentPage = 0
    private val recordsPerPage = 6
    private var totalRecords = 0
    private var patientRecords: List<PatientRecordUI> = emptyList()
    private var filteredRecords: List<PatientRecordUI> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.patient_activity_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components For Filter Section
        timePeriodFilter = view.findViewById(R.id.spinnerTimePeriod)
        statusFilter = view.findViewById(R.id.spinnerStatus)
        typeFilter = view.findViewById(R.id.spinnerType)
        genderFilter = view.findViewById(R.id.spinnerGender)
        ageFilter = view.findViewById(R.id.spinnerAge)

        // Initialize UI components For Pagination Section + All Count
        patientAllCount = view.findViewById(R.id.patient_all_count)
        textPagination = view.findViewById(R.id.textPagination)
        btnNextPage = view.findViewById(R.id.btnNextPage)
        btnPrevPage = view.findViewById(R.id.btnPrevPage)

        // Initialize ViewModel Manually
        patientResponseRecordViewModel =
            ViewModelProvider(requireActivity()).get(PatientRecordViewModel::class.java)


        // Observe case records data
        patientResponseRecordViewModel.patientRecordsLiveData.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                Log.d("FilterDebug", "No case records available. Skipping filter.")
                return@observe
            }

            Log.d("FilterDebug", "Case records loaded: ${records.size}")

            patientRecords = records // Only set non-null data
            applyFilters() //Apply filters only if records exist
        }

        // Observe error messages
        patientResponseRecordViewModel.errorLiveData.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        })

        // Fetch case records when the fragment is created
        patientResponseRecordViewModel.fetchCaseRecords()

        // Pagination button listeners
        btnNextPage.setOnClickListener {
            if ((currentPage + 1) * recordsPerPage < totalRecords) {
                currentPage++
                updateFilteredTable()
            }
        }

        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateFilteredTable()
            }
        }

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        timePeriodFilter.onItemSelectedListener = filterListener
        statusFilter.onItemSelectedListener = filterListener
        typeFilter.onItemSelectedListener = filterListener
        genderFilter.onItemSelectedListener = filterListener
        ageFilter.onItemSelectedListener = filterListener

        // Time Period Spinner Setup
        val timePeriodOptions = resources.getStringArray(R.array.time_period_options)
        val timePeriodAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, timePeriodOptions)
        timePeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodFilter.adapter = timePeriodAdapter

        // PatientResponse Status Spinner Setup
        val statusOptions = resources.getStringArray(R.array.patient_status_options)
        val statusAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusFilter.adapter = statusAdapter

        // Type Spinner Setup
        val typeOptions = resources.getStringArray(R.array.type_options)
        val typeAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeFilter.adapter = typeAdapter

        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_filter_options)
        val genderAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderFilter.adapter = genderAdapter

        // Age Spinner Setup
        val ageOptions = resources.getStringArray(R.array.age_options)
        val ageAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, ageOptions)
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ageFilter.adapter = ageAdapter

    }

    private fun applyFilters() {
        if (patientRecords.isEmpty()) {
            Log.d("FilterDebug", "No case records available yet. Skipping filter.")
            return // Avoid applying filters on empty data
        }

        val selectedStatus =
            statusFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all status"
        val selectedTimePeriod =
            timePeriodFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all time"
        val selectedType = typeFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all type"
        val selectedGender =
            genderFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all gender"
        val selectedAge = ageFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all age"

        filteredRecords = patientRecords.filter { record ->

            // Status Filter
            val recordStatus = record.patientStatus.trim().lowercase()
            val statusMatch = when (selectedStatus) {
                "all status" -> true
                "finished" -> recordStatus == "completed"
                else -> recordStatus == selectedStatus
            }

            // Time Period Filter
            val timeMatch = when (selectedTimePeriod) {
                "all time" -> true
                else -> filterByTime(record.patientLastUpdate, selectedTimePeriod)
            }

            // Type Filter
            val recordType = record.patientType.trim().lowercase()
            val typeMatch = when (selectedType) {
                "all type" -> true // Show all types
                else -> recordType == selectedType
            }

            // Gender Filter
            val recordGender = record.patientGender.trim().lowercase()
            val genderMatch = when (selectedGender) {
                "all gender" -> true // Show all genders
                else -> recordGender == selectedGender
            }

            // Age Filter (Convert to Int safely)
            val recordAge = record.patientAge?.toIntOrNull() ?: -1 // Default -1 if invalid
            val ageMatch = when (selectedAge) {
                "all age" -> true // Show all age groups
                "0-12 (children)" -> recordAge in 0..12
                "13-17 (teens)" -> recordAge in 13..17
                "18-64 (adults)" -> recordAge in 18..64
                "65+ (seniors)" -> recordAge >= 65
                else -> true // Default case, show all
            }

            statusMatch && timeMatch && typeMatch && genderMatch && ageMatch
        }

        totalRecords = filteredRecords.size
        currentPage = 0

        Log.d("FilterDebug", "Total Case Records: ${patientRecords.size}")
        Log.d("FilterDebug", "Filtered Records: ${filteredRecords.size}")

        updateFilteredTable()
    }

    private fun updateFilteredTable() {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableLayoutPatientHeader) ?: return
        Log.d("Filter", "Updating table with ${filteredRecords.size} records")

        // Clear old rows (keep header)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        // Update the total casedashboard count in the UI
        patientAllCount.text = "$totalRecords"

        if (filteredRecords.isEmpty()) {
            textPagination.text = "No records found"
            Toast.makeText(requireContext(), "No case records found.", Toast.LENGTH_SHORT).show()
            return
        }

        val start = currentPage * recordsPerPage
        val end = minOf(start + recordsPerPage, totalRecords)
        val paginatedRecords = filteredRecords.subList(start, end)

        for (record in paginatedRecords) {
            val inflater = LayoutInflater.from(requireContext())
            val newRow = inflater.inflate(
                R.layout.patient_activity_data_row,
                tableLayout,
                false
            ) as TableRow

            // Bind data to views
            newRow.findViewById<TextView>(R.id.patientId).text = record.patientId.toString()
            newRow.findViewById<TextView>(R.id.patientName).text = record.patientName
            newRow.findViewById<TextView>(R.id.patientBirthDate).text = record.patientBirthDate
            newRow.findViewById<TextView>(R.id.patientAge).text = "(${record.patientAge})"
            newRow.findViewById<TextView>(R.id.patientGender).text = record.patientGender
            newRow.findViewById<TextView>(R.id.patientPhoneNumber).text = record.patientPhoneNumber
            newRow.findViewById<TextView>(R.id.patientEmail).text = record.patientEmail
            newRow.findViewById<TextView>(R.id.patientAddress).text = record.patientAddress

            // Set on click listener for edit button
            val editButton = newRow.findViewById<ImageView>(R.id.editIcon)
            editButton.setOnClickListener {
                val dialog = EditPatientDialogFragment.newInstance(record.patientId.toString())
                dialog.show(parentFragmentManager, "EditPatientDialog")
            }

            // Set on click listener for delete button
            val deleteButton = newRow.findViewById<ImageView>(R.id.deleteIcon)
            deleteButton.setOnClickListener {
                confirmDeletePatient(record.patientId)
            }

            // Add row to table
            tableLayout.addView(newRow)
        }

        textPagination.text = "${start + 1}-$end of $totalRecords"
        btnPrevPage.isEnabled = currentPage > 0
        btnNextPage.isEnabled = end < totalRecords
    }


    private fun filterByTime(
        updatedAt: String,  // Only using updated_at now
        selectedTimePeriod: String
    ): Boolean {
        return try {
            // Backend format: "2025-03-26T19:40:14.464728"
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC") // Ensure correct timezone handling

            // Parse `updated_at` from backend
            val recordDateTime = isoFormatter.parse(updatedAt) ?: return false

            // Get current time
            val now = Calendar.getInstance().time

            when (selectedTimePeriod.lowercase()) {
                "last 24 hours" -> {
                    val last24Hours = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }
                    recordDateTime.after(last24Hours.time)
                }

                "weekly" -> {
                    val last7Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                    recordDateTime.after(last7Days.time)
                }

                "monthly" -> {
                    val recordCalendar = Calendar.getInstance().apply { time = recordDateTime }
                    val currentCalendar = Calendar.getInstance()

                    recordCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            recordCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                }

                "all time" -> true
                else -> true
            }
        } catch (e: ParseException) {
            Log.e("FilterError", "Failed to parse updated_at: $updatedAt", e)
            false
        }
    }

    private fun confirmDeletePatient(patientId: Int) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete PatientResponse")
        builder.setMessage("Are you sure you want to delete this casedashboard record? This action cannot be undone.")

        builder.setPositiveButton("Delete") { _, _ ->
            patientResponseRecordViewModel.deletePatient(patientId)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

}