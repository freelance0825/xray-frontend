package com.example.xray_frontend.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.data.models.AuthDoctorRequest
import com.example.xray_frontend.data.models.AuthDoctorResponse
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import kotlinx.coroutines.launch
import com.example.xray_frontend.ui.utils.Result

class SignUpViewModel(private val thunderscopeRepository: ThunderscopeRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _registrationResult = MutableLiveData<AuthDoctorResponse>(null)
    val registrationResult: LiveData<AuthDoctorResponse> get() = _registrationResult

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> get() = _errorMessage

    fun registerDoctor(authDoctorRequest: AuthDoctorRequest) {
        viewModelScope.launch {
            thunderscopeRepository.registerDoctor(authDoctorRequest).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _registrationResult.value = result.data
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.error
                    }
                }
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
