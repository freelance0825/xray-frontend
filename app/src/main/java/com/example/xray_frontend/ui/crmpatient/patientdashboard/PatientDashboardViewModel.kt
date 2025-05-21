package com.example.xray_frontend.ui.crmpatient.patientdashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.data.models.PatientResponse
import com.example.xray_frontend.data.models.UpdatePatientRequest
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.ui.utils.enums.PatientStatus
import com.example.xray_frontend.ui.utils.helpers.Result
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PatientDashboardViewModel(private val thunderscopeRepository: ThunderscopeRepository) : ViewModel() {

    private val _patientRecordsLiveData = MutableLiveData<List<PatientResponse>>()
    val patientRecordsLiveData: LiveData<List<PatientResponse>> = _patientRecordsLiveData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Edit Patient
    val selectedPatientImage = MutableLiveData<File>(null)
    val isUpdatePatientSuccessful = MutableLiveData(false)

    // PAGINATION
    private var currentPage = 0
    private val recordsPerPage = 6

    private var allRecords: List<PatientResponse> = emptyList()

    val filteredRecordsLiveData = MutableLiveData<List<PatientResponse>>()

    var startIndex = 0
    var endIndex = 0
    var totalRecords = 0

    init {
        fetchPatientRecords()
    }

    fun fetchPatientRecords() {
        viewModelScope.launch {
            thunderscopeRepository.getAllPatients().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        val sortedPatientByLatestUpdate =
                            result.data.sortedByDescending { it.updatedAt }
                        _patientRecordsLiveData.value = sortedPatientByLatestUpdate
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.error
                    }
                }
            }
        }
    }

    fun deletePatient(patientId: Int) {
        viewModelScope.launch {
            thunderscopeRepository.deletePatient(patientId).collect { result ->
                when (result) {
                    is Result.Loading -> _isLoading.value = true
                    is Result.Success -> {
                        _isLoading.value = false
                        // Remove deleted patientResponse from current list
                        _patientRecordsLiveData.value =
                            _patientRecordsLiveData.value?.filter { it.id?.toInt() != patientId }
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.error
                    }
                }
            }
        }
    }

    fun updatePatient(patientId: Int, updatePatientRequest: UpdatePatientRequest) {
        viewModelScope.launch {
            thunderscopeRepository.updatePatient(patientId, updatePatientRequest)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _isLoading.value = true
                            isUpdatePatientSuccessful.value = false
                        }

                        is Result.Success -> {
                            _isLoading.value = false
                            isUpdatePatientSuccessful.value = true
                            fetchPatientRecords() // Refresh the list after update
                            isUpdatePatientSuccessful.value = false // Reset flag
                        }

                        is Result.Error -> {
                            isUpdatePatientSuccessful.value = false
                            _isLoading.value = false
                            _errorMessage.value = result.error
                        }
                    }
                }
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
        allRecords = this._patientRecordsLiveData.value.orEmpty()

        val filtered = allRecords.filter { record ->

            // Status Filter
            val recordStatus = record.status?.trim()?.lowercase()
            val translated = PatientStatus.getTranslatedStringValue(recordStatus)
            val statusMatch = when (selectedStatus) {
                "All Status" -> true
                else -> translated?.lowercase()?.trim() == selectedStatus.lowercase().trim()
            }

            // Time Period Filter
            val timeMatch = when (selectedTimePeriod) {
                "All Time" -> true
                else -> filterByTime(record.updatedAt ?: "", selectedTimePeriod)
            }

            // Type Filter
            val recordType = record.type?.trim()?.lowercase()
            val typeMatch = when (selectedType) {
                "All Type" -> true // Show all types
                else -> recordType?.lowercase()?.trim() == selectedType.lowercase().trim()
            }

            // Gender Filter
            val recordGender = record.gender?.trim()?.lowercase()
            val genderMatch = when (selectedGender) {
                "All Gender" -> true // Show all genders
                else -> recordGender?.lowercase()?.trim() == selectedGender.lowercase().trim()
            }

            // Age Filter (Convert to Int safely)
            val recordAge = record.age?.toIntOrNull() ?: -1 // Default -1 if invalid
            val ageMatch = when (selectedAge) {
                "All Age" -> true // Show all age groups
                "0-12 (Children)" -> recordAge in 0..12
                "13-17 (Teens)" -> recordAge in 13..17
                "18-64 (Adults)" -> recordAge in 18..64
                "65+ (Seniors)" -> recordAge >= 65
                else -> true // Default case, show all
            }

            statusMatch && timeMatch && typeMatch && genderMatch && ageMatch
        }

        totalRecords = filtered.size
        currentPage = 0

        updatePaginatedList(filtered)
    }

    private fun updatePaginatedList(records: List<PatientResponse>) {
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
                    val last24Hours =
                        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }
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

    fun logout() {
        viewModelScope.launch {
            thunderscopeRepository.logout()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ThunderscopeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientDashboardViewModel::class.java)) {
                return PatientDashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
