package com.example.thunderscope_frontend.ui.patientreport

import androidx.lifecycle.*
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatientReportViewModel(private val repository: ThunderscopeRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _caseRecordsLiveData = MutableLiveData<MutableList<CaseRecordResponse>>(mutableListOf())
    val caseRecordsLiveData: LiveData<MutableList<CaseRecordResponse>> get() = _caseRecordsLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    private val _pageInfoLiveData = MutableLiveData<Pair<Int, Int>>() // startIndex, endIndex
    val pageInfoLiveData: LiveData<Pair<Int, Int>> get() = _pageInfoLiveData

    private val allRecords = mutableListOf<CaseRecordResponse>()
    private var currentPage = 0
    private val _recordsPerPage = 10

    val recordsPerPage: Int get() = _recordsPerPage
    val totalRecords: Int get() = allRecords.size

    fun fetchCaseRecordReports() {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = true
            repository.getAllCaseRecordByCompletedStatus().collect { result ->
                when (result) {
                    is Result.Loading -> _isLoading.value = true

                    is Result.Success -> {
                        _isLoading.value = false
                        allRecords.clear()
                        val sortedRecords = result.data.sortedByDescending { it.updatedAt }
                        allRecords.addAll(sortedRecords)
                        currentPage = 0
                        _caseRecordsLiveData.value = mutableListOf()
                        loadNextPage()
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorLiveData.value = result.error
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value == true) return

        val start = currentPage * _recordsPerPage
        val end = minOf(start + _recordsPerPage, allRecords.size)

        if (start >= allRecords.size) return

        val currentList = _caseRecordsLiveData.value ?: mutableListOf()
        val nextPageItems = allRecords.subList(start, end)

        currentList.addAll(nextPageItems)
        _caseRecordsLiveData.value = currentList

        // Emit the new page range (1-based indexing)
        _pageInfoLiveData.value = Pair(start + 1, end)

        currentPage++
    }

    fun archiveOrUnarchiveCaseRecord(caseRecordId: Long, isArchived: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            repository.archiveOrUnarchiveCaseRecord(caseRecordId, isArchived).collect { result ->
                when (result) {
                    is Result.Loading -> _isLoading.value = true

                    is Result.Success -> {
                        _isLoading.value = false
                        fetchCaseRecordReports()
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorLiveData.value = result.error
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ThunderscopeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientReportViewModel::class.java)) {
                return PatientReportViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
