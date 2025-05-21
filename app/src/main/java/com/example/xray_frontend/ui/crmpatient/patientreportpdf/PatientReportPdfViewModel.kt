package com.example.xray_frontend.ui.crmpatient.patientreportpdf

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.ui.utils.helpers.Result
import kotlinx.coroutines.launch

class PatientReportPdfViewModel(private val caseId: Long, private val thunderscopeRepository: ThunderscopeRepository) : ViewModel() {

    private val _currentlySelectedCaseRecord = MutableLiveData<SlidesItem?>()
    val currentlySelectedCaseRecord: LiveData<SlidesItem?> = _currentlySelectedCaseRecord

    init {
        fetchSlideByCaseId()
    }

    private fun fetchSlideByCaseId() {
        viewModelScope.launch {
            thunderscopeRepository.getSlideByCaseId(caseId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                    }

                    is Result.Success -> {
                        Log.d("PatientReport", "Data fetched successfully: ${result.data}")
                        _currentlySelectedCaseRecord.value = result.data
                    }

                    is Result.Error -> {
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context, private val caseId: Long) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientReportPdfViewModel::class.java)) {
                return PatientReportPdfViewModel(caseId, ThunderscopeRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

