package com.example.thunderscope_frontend.ui.createnewtest

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.models.CaseRecordRequest
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.models.PatientResponse
import com.example.thunderscope_frontend.data.models.UpdatePatientRequest
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.utils.CaseRecordStatus
import com.example.thunderscope_frontend.ui.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateNewTestViewModel(
    private val thunderscopeRepository: ThunderscopeRepository,
    private val doctorId: Int
) : ViewModel() {
    val isStateChanged = MutableLiveData(true)
    val isCreatingNewPatient = MutableLiveData(true)
    val successfullySubmittedPatient = MutableLiveData(false)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingPreparingTest = MutableLiveData(false)
    val isLoadingPreparingTest: LiveData<Boolean> = _isLoadingPreparingTest

    private val _isSuccessAddingPatient = MutableLiveData(false)
    val isSuccessAddingPatient: LiveData<Boolean> = _isSuccessAddingPatient

    private val _patientRecordsLiveData =
        MutableLiveData<MutableList<PatientResponse>>(mutableListOf())
    val patientRecordsLiveData: LiveData<MutableList<PatientResponse>> = _patientRecordsLiveData

    private val _selectedPatient = MutableLiveData<PatientResponse?>(null)
    val selectedPatient: LiveData<PatientResponse?> = _selectedPatient

    private val _caseRecordResponse = MutableLiveData<CaseRecordResponse?>(null)
    val caseRecordResponse: LiveData<CaseRecordResponse?> = _caseRecordResponse

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val selectedPatientImage = MutableLiveData<File>(null)

    init {
        fetchPatientRecords()
    }

//    fun generateDummySlidesToDatabaseForMVPPurpose() {
//        viewModelScope.launch {
//            thunderscopeRepository.generateDummySlidesToDatabaseForMVPPurpose()
//        }
//    }

    fun addPatient(updatePatientRequest: UpdatePatientRequest) {
        viewModelScope.launch {
            thunderscopeRepository.addPatient(updatePatientRequest).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _selectedPatient.value = null
                        _selectedPatient.value = result.data

                        successfullySubmittedPatient.value = true
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.error
                    }
                }
            }
        }
    }

    fun addCaseRecord() {
        val caseRecordRequest = CaseRecordRequest()

        _selectedPatient.value?.let {
            val now = Date()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // "2025-04-25"
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())    // "11:06 PM"
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())       // "2025"

            caseRecordRequest.doctorId = doctorId
            caseRecordRequest.patientId = it.id?.toInt()
            caseRecordRequest.date = dateFormat.format(now)
            caseRecordRequest.time = timeFormat.format(now)
            caseRecordRequest.year = yearFormat.format(now)
            caseRecordRequest.type = "Left"
            caseRecordRequest.status = CaseRecordStatus.IN_PREPARATIONS.name
        }

        viewModelScope.launch {

            // Later don't use DUMMY SLIDES
            thunderscopeRepository.addCaseRecordWithDummySlides(caseRecordRequest)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _isLoadingPreparingTest.value = true
                        }

                        is Result.Success -> {
                            _isLoadingPreparingTest.value = false
                            _caseRecordResponse.value = result.data
                        }

                        is Result.Error -> {
                            _isLoadingPreparingTest.value = false
                            _errorMessage.value = result.error
                        }
                    }
                }
        }
    }

    private fun fetchPatientRecords() {
        viewModelScope.launch(Dispatchers.Main) {
            thunderscopeRepository.getAllPatients().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _isSuccessAddingPatient.value = false
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _patientRecordsLiveData.value = result.data.toMutableList()
                        _isSuccessAddingPatient.value = true
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _isSuccessAddingPatient.value = false
                        _errorMessage.value =
                            result.error
                    }
                }
            }
        }
    }

    fun selectPatient(patientId: Int? = null) {
        val patient =
            if (patientId == null) null else _patientRecordsLiveData.value?.find { it.id?.toInt() == patientId }
        _selectedPatient.value = patient
    }

    fun searchPatient(patientId: Int): Boolean {
        return _patientRecordsLiveData.value?.any { it.id?.toInt() == patientId } ?: false
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context,
        private val doctorId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateNewTestViewModel::class.java)) {
                return CreateNewTestViewModel(ThunderscopeRepository(context), doctorId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
