package com.example.thunderscope_frontend.ui.createnewtest

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.models.PatientResponse
import com.example.thunderscope_frontend.data.models.UpdatePatientRequest
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CreateNewTestViewModel(
    private val thunderscopeRepository: ThunderscopeRepository
) : ViewModel() {
    val isStateChanged = MutableLiveData(false)
    val isCreatingNewPatient = MutableLiveData(false)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccessAddingPatient = MutableLiveData(false)
    val isSuccessAddingPatient: LiveData<Boolean> = _isSuccessAddingPatient

    private val _patientRecordsLiveData =
        MutableLiveData<MutableList<PatientResponse>>(mutableListOf())
    val patientRecordsLiveData: LiveData<MutableList<PatientResponse>> = _patientRecordsLiveData

    private val _selectedPatient = MutableLiveData<PatientResponse?>(null)
    val selectedPatient: LiveData<PatientResponse?> = _selectedPatient

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val selectedPatientImage = MutableLiveData<File>(null)

    init {
        fetchPatientRecords()
    }

    fun generateDummySlidesToDatabaseForMVPPurpose() {
        viewModelScope.launch {
            thunderscopeRepository.generateDummySlidesToDatabaseForMVPPurpose()
        }
    }

    fun addPatient(updatePatientRequest: UpdatePatientRequest) {
        viewModelScope.launch {
            thunderscopeRepository.addPatient(updatePatientRequest).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _selectedPatient.value = result.data
                    }
                    is Result.Error -> {
                        _isLoading.value = false
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

    fun selectPatient(patient: PatientResponse?) {
        _selectedPatient.value = patient
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateNewTestViewModel::class.java)) {
                return CreateNewTestViewModel(ThunderscopeRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
