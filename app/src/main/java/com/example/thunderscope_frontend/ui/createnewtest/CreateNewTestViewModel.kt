package com.example.thunderscope_frontend.ui.createnewtest

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import kotlinx.coroutines.launch

class CreateNewTestViewModel(
    private val thunderscopeRepository: ThunderscopeRepository
) : ViewModel() {
    val isStateChanged = MutableLiveData(false)

    fun generateDummySlidesToDatabaseForMVPPurpose() {
        viewModelScope.launch {
            thunderscopeRepository.generateDummySlidesToDatabaseForMVPPurpose()
        }
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
