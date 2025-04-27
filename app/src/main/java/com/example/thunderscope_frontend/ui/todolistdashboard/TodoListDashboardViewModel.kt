package com.example.thunderscope_frontend.ui.todolistdashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.models.AuthDoctorResponse
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.models.PatientResponse
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.login.LoginViewModel
import com.example.thunderscope_frontend.ui.utils.Result
import com.example.thunderscope_frontend.viewmodel.SlidesRecordUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoListDashboardViewModel(
    private val repository: ThunderscopeRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _patientRecordsLiveData =
        MutableLiveData<MutableList<PatientResponse>>(mutableListOf())
    val patientRecordsLiveData: LiveData<MutableList<PatientResponse>> = _patientRecordsLiveData

    private val _caseRecordsLiveData =
        MutableLiveData<MutableList<CaseRecordResponse>>(mutableListOf())
    val caseRecordsLiveData: LiveData<MutableList<CaseRecordResponse>> = _caseRecordsLiveData

    private val _slidesRecordsLiveData = MutableLiveData<MutableList<SlidesItem>>(mutableListOf())
    val slidesRecordsLiveData: LiveData<MutableList<SlidesItem>> get() = _slidesRecordsLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    // Searching
    val selectedPatientId = MutableLiveData<Int>(null)
    val selectedDoctorId = MutableLiveData<Int>(null)


    // PAGINATION
    private var currentPage = 0
    private val recordsPerPage = 6

    private var allRecords: List<CaseRecordResponse> = emptyList()

    val filteredRecordsLiveData = MutableLiveData<List<CaseRecordResponse>>()

    var startIndex = 0
    var endIndex = 0
    var totalRecords = 0

    init {
        fetchPatientRecords()
        fetchCaseRecords()
    }

    private fun fetchPatientRecords() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getAllPatients().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _patientRecordsLiveData.value = result.data.toMutableList()
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorLiveData.value =
                            result.error
                    }
                }
            }
        }
    }

    fun fetchCaseRecords() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getAllCases().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _caseRecordsLiveData.value = result.data.toMutableList()

                        result.data.forEach { record ->
                            getSlidesByCaseID(record.id ?: 0)
                        }
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorLiveData.value =
                            result.error
                    }
                }
            }
        }
    }

    fun fetchCaseRecordsFilterId(patientId: Int? = null, doctorId: Int? = null) {
        if (patientId == null && doctorId == null) {
            fetchCaseRecords()
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            repository.getAllCasesFilterId(patientId, doctorId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        Log.e("FTEST", "fetchCaseRecordsFilterId: LOADING")
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _caseRecordsLiveData.value = result.data.toMutableList()

                        result.data.forEach { record ->
                            getSlidesByCaseID(record.id ?: 0)
                        }
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorLiveData.value = result.error
                        Log.e("FTEST", "fetchCaseRecordsFilterId: ERROR ${result.error}")
                    }
                }
                Log.e("FTEST", "fetchCaseRecordsFilterId: FINISH")
            }
        }
    }

    fun getSlidesByCaseID(caseId: Int) {
        viewModelScope.launch {

            repository.getAllSlides(caseId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                    }

                    is Result.Success -> {
                        if (result.data.isNotEmpty()) {
                            _caseRecordsLiveData.value =
                                _caseRecordsLiveData.value?.map { caseRecord ->
                                    if (caseRecord.id == caseId) {
                                        caseRecord.copy(slides = result.data.toMutableList())
                                    } else {
                                        caseRecord
                                    }
                                }?.toMutableList()
                        }
                    }

                    is Result.Error -> {
                        _errorLiveData.value = result.error
                        // Handle error here
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // PAGINATION FUNCTION
    fun applyFilters(
        selectedStatus: String,
        selectedTimePeriod: String,
        selectedType: String,
        selectedGender: String,
        selectedAge: String
    ) {
        allRecords = caseRecordsLiveData.value.orEmpty()

        val filtered = allRecords.filter { record ->
            // Same filter logic like you already have
            val statusMatch = when (selectedStatus.trim().lowercase()) {
                "all status" -> true
                "finished" -> record.status?.trim()?.lowercase() == "completed"
                else -> record.status?.trim()?.lowercase() == selectedStatus.trim().lowercase()
            }

            val typeMatch = selectedType.trim().lowercase() == "all type" || record.type?.trim()
                ?.lowercase() == selectedType.trim().lowercase()
            val genderMatch =
                selectedGender.trim().lowercase() == "all gender" || record.patient?.gender?.trim()
                    ?.lowercase() == selectedGender.trim().lowercase()

            // Time Period Filter
            val timeMatch = when (selectedTimePeriod) {
                "all time" -> true
                else -> filterByTime(
                    record.date ?: "",
                    record.time ?: "",
                    selectedTimePeriod
                )
            }

            // age parsing
            val recordAge = record.patient?.age?.toIntOrNull() ?: -1
            val ageMatch = when (selectedAge.trim().lowercase()) {
                "all age" -> true
                "0-12 (children)" -> recordAge in 0..12
                "13-17 (teens)" -> recordAge in 13..17
                "18-64 (adults)" -> recordAge in 18..64
                "65+ (seniors)" -> recordAge >= 65
                else -> true
            }

            statusMatch && timeMatch && typeMatch && genderMatch && ageMatch
        }

        totalRecords = filtered.size
        currentPage = 0

        updatePaginatedList(filtered)
    }

    private fun updatePaginatedList(records: List<CaseRecordResponse>) {
        val start = currentPage * recordsPerPage
        val end = minOf(start + recordsPerPage, records.size)

        startIndex = start
        endIndex = end

        filteredRecordsLiveData.postValue(records.subList(start, end))
    }

    fun nextPage() {
        if ((currentPage + 1) * recordsPerPage < totalRecords) {
            currentPage++
            applyFiltersForCurrentPage()
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
            applyFiltersForCurrentPage()
        }
    }

    private fun applyFiltersForCurrentPage() {
        allRecords.takeIf { it.isNotEmpty() }?.let {
            updatePaginatedList(it)
        }
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ThunderscopeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoListDashboardViewModel::class.java)) {
                return TodoListDashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
