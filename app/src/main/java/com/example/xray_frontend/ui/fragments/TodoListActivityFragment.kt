package com.example.xray_frontend.ui.fragments

import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import com.example.xray_frontend.R
import com.example.xray_frontend.ui.slides.SlidesActivity
import com.example.xray_frontend.ui.utils.Base64Helper
import com.example.xray_frontend.viewmodel.CaseRecordUI
import com.example.xray_frontend.viewmodel.CaseRecordViewModel
import com.example.xray_frontend.viewmodel.SlidesRecordUI
import com.example.xray_frontend.viewmodel.SlidesRecordViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoListActivityFragment : Fragment() {

    // UI Components for Filter Section
    private lateinit var timePeriodFilter: Spinner
    private lateinit var statusFilter: Spinner
    private lateinit var typeFilter: Spinner
    private lateinit var genderFilter: Spinner
    private lateinit var ageFilter: Spinner

    // UI Components for Pagination Section
    private lateinit var todoCount: TextView
    private lateinit var textPagination: TextView
    private lateinit var btnNextPage: ImageButton
    private lateinit var btnPrevPage: ImageButton

    // UI Components for Assessment Images & Count
    private lateinit var assessmentImage1: ImageView
    private lateinit var assessmentImage2: ImageView
    private lateinit var assessmentImage3: ImageView
    private lateinit var assessmentImageCount: TextView

    // Case Record View Model
    private lateinit var caseRecordViewModel: CaseRecordViewModel

    // Slide Record View Model
    private lateinit var slidesRecordViewModel: SlidesRecordViewModel

    // Pagination variables
    private var currentPage = 0
    private val recordsPerPage = 5
    private var totalRecords = 0
    private var caseRecords: List<CaseRecordUI> = emptyList()
    private var filteredRecords: List<CaseRecordUI> = emptyList()
    private var slidesRecords: List<SlidesRecordUI> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.todo_list_activity_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components For Filter Section
        timePeriodFilter = view.findViewById(R.id.spinnerTimePeriod)
        statusFilter = view.findViewById(R.id.spinnerStatus)
        typeFilter = view.findViewById(R.id.spinnerType)
        genderFilter = view.findViewById(R.id.spinnerGender)
        ageFilter = view.findViewById(R.id.spinnerAge)

        // Initialize UI components For Pagination Section + Count
        todoCount = view.findViewById(R.id.todo_count)
        textPagination = view.findViewById(R.id.textPagination)
        btnNextPage = view.findViewById(R.id.btnNextPage)
        btnPrevPage = view.findViewById(R.id.btnPrevPage)

        // Initialize UI components for Assessment Images & Count
        assessmentImage1 = view.findViewById(R.id.assessmentImage1)
        assessmentImage2 = view.findViewById(R.id.assessmentImage2)
        assessmentImage3 = view.findViewById(R.id.assessmentImage3)
        assessmentImageCount = view.findViewById(R.id.assessmentImageCount)

        // Initialize ViewModels at the start
        caseRecordViewModel = ViewModelProvider(requireActivity()).get(CaseRecordViewModel::class.java)
        slidesRecordViewModel = ViewModelProvider(requireActivity()).get(SlidesRecordViewModel::class.java)

        // Observe case records
        caseRecordViewModel.caseRecordsLiveData.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                Log.d("FilterDebug", "No case records available. Skipping filter.")
                return@observe
            }

            Log.d("FilterDebug", "Case records loaded: ${records.size}")
            caseRecords = records
            applyFilters()

            // Fetch slides for each caseRecordId
            caseRecords.forEach { record ->
                Log.d("SlidesDebug", "Fetching slides for caseRecordId: ${record.caseRecordId}")
                slidesRecordViewModel.fetchSlidesRecords(record.caseRecordId)
            }
        }

        // Observe slides records
        slidesRecordViewModel.slidesRecordsLiveData.observe(viewLifecycleOwner) { slides ->
            if (slides.isNullOrEmpty()) {
                Log.d("SlidesDebug", "No slides available.")
                return@observe
            }

            Log.d("SlidesDebug", "Slides loaded: ${slides.size}")
            slidesRecords = slides
            updateFilteredTable() // Refresh the table to show slides
        }

        // Observe error messages
        caseRecordViewModel.errorLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

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
        val timePeriodAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, timePeriodOptions)
        timePeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodFilter.adapter = timePeriodAdapter

        // Case Record Status Spinner Setup
        val statusOptions = resources.getStringArray(R.array.case_record_status_options)
        val statusAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusFilter.adapter = statusAdapter

        // Type Spinner Setup
        val typeOptions = resources.getStringArray(R.array.type_options)
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeFilter.adapter = typeAdapter

        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_filter_options)
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderFilter.adapter = genderAdapter

        // Age Spinner Setup
        val ageOptions = resources.getStringArray(R.array.age_options)
        val ageAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, ageOptions)
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ageFilter.adapter = ageAdapter

    }

    private fun applyFilters() {
        if (caseRecords.isEmpty()) {
            Log.d("FilterDebug", "No case records available yet. Skipping filter.")
            return // Avoid applying filters on empty data
        }

        val selectedStatus = statusFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all status"
        val selectedTimePeriod = timePeriodFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all time"
        val selectedType = typeFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all type"
        val selectedGender = genderFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all gender"
        val selectedAge = ageFilter.selectedItem?.toString()?.trim()?.lowercase() ?: "all age"

        filteredRecords = caseRecords.filter { record ->

            // Status Filter
            val recordStatus = record.status.trim().lowercase()
            val statusMatch = when (selectedStatus) {
                "all status" -> true
                "finished" -> recordStatus == "completed"
                else -> recordStatus == selectedStatus
            }

            // Time Period Filter
            val timeMatch = when (selectedTimePeriod) {
                "all time" -> true
                else -> filterByTime(
                    record.lastUpdateDate,
                    record.lastUpdateTime,
                    selectedTimePeriod
                )
            }

            // Type Filter
            val recordType = record.type.trim().lowercase()
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

        Log.d("FilterDebug", "Total Case Records: ${caseRecords.size}")
        Log.d("FilterDebug", "Filtered Records: ${filteredRecords.size}")

        updateFilteredTable()
    }

    private fun updateFilteredTable() {
        val tableLayout = view?.findViewById<TableLayout>(R.id.tableTodoLayoutHeader) ?: return
        Log.d("Filter", "Updating table with ${filteredRecords.size} records")

        // Clear old rows (keep header)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        // Update the total case record count in the UI
        todoCount.text = "$totalRecords"

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
                R.layout.todolist_activity_data_row,
                tableLayout,
                false
            ) as TableRow

            newRow.findViewById<TextView>(R.id.caseRecordId).text = record.caseRecordId.toString()
            newRow.findViewById<TextView>(R.id.caseRecordPatientId).text = record.patientId.toString()
            newRow.findViewById<TextView>(R.id.todo).text = record.todo
            newRow.findViewById<TextView>(R.id.patientName).text = record.patientName
            newRow.findViewById<TextView>(R.id.patientBirthdate).text = record.patientBirthdate
            newRow.findViewById<TextView>(R.id.patientAge).text = "(${record.patientAge}yo)"
            newRow.findViewById<TextView>(R.id.patientGender).text = record.patientGender
            newRow.findViewById<TextView>(R.id.lastUpdateDate).text = record.lastUpdateDate
            newRow.findViewById<TextView>(R.id.lastUpdateTime).text = record.lastUpdateTime

            newRow.findViewById<TextView>(R.id.status).apply {
                text = record.status.uppercase()
                background = when (record.status.lowercase().trim()) {
                    "completed" -> resources.getDrawable(R.drawable.bg_status_completed, null)
                    "for review" -> resources.getDrawable(R.drawable.bg_status_for_review, null)
                    else -> resources.getDrawable(R.drawable.bg_status_default, null)
                }
                setTextColor(
                    when (record.status.lowercase().trim()) {
                        "completed" -> resources.getColor(R.color.white, null)
                        "for review" -> resources.getColor(R.color.blue_text, null)
                        else -> resources.getColor(R.color.blue_text, null)
                    }
                )
            }

            // Get all slide records matching the current caseRecordId
            val matchingSlideRecords = slidesRecords.filter { it.caseRecordId == record.caseRecordId }

            // Extract all image URLs from the matching records
            val slideImages = matchingSlideRecords
                .flatMap { it.mainImage.split(",") } // Split each record's mainImage by comma
                .map { it.trim() }                   // Trim spaces
                .filter { it.isNotEmpty() }          // Remove empty strings

            Log.d("SlidesDebug", "CaseRecordId: ${record.caseRecordId}, Found ${matchingSlideRecords.size} records")
            Log.d("SlidesDebug", "Extracted Images: $slideImages")

            // Image views
            val image1 = newRow.findViewById<ImageView>(R.id.assessmentImage1)
            val image2 = newRow.findViewById<ImageView>(R.id.assessmentImage2)
            val image3 = newRow.findViewById<ImageView>(R.id.assessmentImage3)
            val imageCountText = newRow.findViewById<TextView>(R.id.assessmentImageCount)

            // Always reset images before loading new ones
            image1.setImageResource(R.drawable.placeholder_image)
            image2.setImageResource(R.drawable.placeholder_image)
            image3.setImageResource(R.drawable.placeholder_image)

            // Load images properly if available
            if (slideImages.isNotEmpty()) {
                Log.e("FTEST", "updateFilteredTable: ${slideImages.getOrNull(0)}", )

                slideImages.getOrNull(0)?.let {
                    image1.setImageBitmap(Base64Helper.convertToBitmap(it))
                }

                slideImages.getOrNull(1)?.let {
                    image2.setImageBitmap(Base64Helper.convertToBitmap(it))
                }

                slideImages.getOrNull(2)?.let {
                    image3.setImageBitmap(Base64Helper.convertToBitmap(it))
                }
            }

            //Show extra image count
            imageCountText.text = when {
                slideImages.isEmpty() -> "0" // No images
                slideImages.size <= 3 -> slideImages.size.toString() // Show total if 3 or less
                else -> "+${slideImages.size - 3}" // Show "+extra count" if more than 3
            }
            Log.d("ImageDebug", "Total images: ${slideImages.size}")
            Log.d("ImageDebug", "imageCountText: ${imageCountText.text}")

            // Click Listener to Open SlidesActivity.kt
            val openSlidesActivity = View.OnClickListener {
                val intent = Intent(requireContext(), SlidesActivity::class.java)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD, record) // Pass the case record ID
                startActivity(intent)
            }

            // Set click listeners for all images and count
            image1.setOnClickListener(openSlidesActivity)
            image2.setOnClickListener(openSlidesActivity)
            image3.setOnClickListener(openSlidesActivity)
            imageCountText.setOnClickListener(openSlidesActivity)

            tableLayout.addView(newRow)
        }

        // Update pagination text and button states
        textPagination.text = "${start + 1}-$end of $totalRecords"
        btnPrevPage.isEnabled = currentPage > 0
        btnNextPage.isEnabled = end < totalRecords
    }

    private fun filterByTime(
        recordDate: String,
        recordTime: String,
        selectedTimePeriod: String
    ): Boolean {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Matches database format
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US) // Matches database format

        return try {
            // Parse date and time separately
            val parsedDate = dateFormatter.parse(recordDate) ?: return false
            val parsedTime = timeFormatter.parse(recordTime) ?: return false

            // Combine Date + Time into one Date object
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            val timeCalendar = Calendar.getInstance()
            timeCalendar.time = parsedTime

            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

            val recordDateTime = calendar.time // Final merged date-time

            // Get current time
            val now = Calendar.getInstance().time

            when (selectedTimePeriod.lowercase()) {
                "last 24 hours" -> {
                    val last24Hours =
                        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }
                    recordDateTime.after(last24Hours.time) // Check if within last 24 hours
                }

                "weekly" -> {
                    val last7Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                    recordDateTime.after(last7Days.time) // Check if within last 7 days
                }

                "monthly" -> {
                    val recordCalendar = Calendar.getInstance().apply { time = recordDateTime }
                    val currentCalendar = Calendar.getInstance()

                    recordCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            recordCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) // Same month & year
                }

                "all time" -> true // Show everything
                else -> true // Default case
            }
        } catch (e: ParseException) {
            Log.e("FilterError", "Date parsing failed: $recordDate $recordTime", e)
            false
        }
    }

}