package com.example.thunderscope_frontend.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.models.DoctorRequest
import com.example.thunderscope_frontend.data.models.DoctorResponse
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import kotlinx.coroutines.launch

class SignUpViewModel(private val thunderscopeRepository: ThunderscopeRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Result<DoctorResponse>>()
    val registrationResult: LiveData<Result<DoctorResponse>> get() = _registrationResult

    fun registerDoctor(doctorRequest: DoctorRequest) {
        viewModelScope.launch {
            try {
                val response = thunderscopeRepository.registerDoctor(doctorRequest)
                _registrationResult.value = Result.success(response)
            } catch (e: Exception) {
                _registrationResult.value = Result.failure(e)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ThunderscopeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                return SignUpViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
