package com.example.xray_frontend.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.ui.utils.helpers.LocaleHelper
import kotlinx.coroutines.launch
import kotlin.jvm.java

class SettingsViewModel(private val repository: ThunderscopeRepository) : ViewModel() {
    val langCode = MutableLiveData("")

    init {
        getLanguage()
    }

    private fun getLanguage() {
        viewModelScope.launch {
            repository.getLanguage().collect {
                langCode.value = it
            }
        }
    }

    fun saveLanguage(language: String, context: Context) {
        viewModelScope.launch {
            repository.saveLanguage(language)
            LocaleHelper.applyLocale(context)
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
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
