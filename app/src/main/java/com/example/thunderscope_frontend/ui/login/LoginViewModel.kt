package com.example.thunderscope_frontend.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.local.datastore.AuthDataStore
import com.example.thunderscope_frontend.data.models.AuthDoctorResponse
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import kotlinx.coroutines.launch
import com.example.thunderscope_frontend.ui.utils.Result

class LoginViewModel(private val thunderscopeRepository: ThunderscopeRepository) : ViewModel() {

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _loginResult = MutableLiveData<AuthDoctorResponse?>(null)
    val loginResult: LiveData<AuthDoctorResponse?> get() = _loginResult

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        getToken()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            thunderscopeRepository.loginDoctor(email, password).collect { result ->
                when (result) {
                    is Result.Loading -> _isLoading.value = true
                    is Result.Success -> {
                        _isLoading.value = false
                        _loginResult.value = result.data
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.error
                    }
                }
            }
        }
    }

    private fun getToken() {
       viewModelScope.launch {
           thunderscopeRepository.getToken().collect {
               _isLoggedIn.value = it != AuthDataStore.preferencesDefaultValue
           }
       }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ThunderscopeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
