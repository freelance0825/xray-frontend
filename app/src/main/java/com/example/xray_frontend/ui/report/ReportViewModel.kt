package com.example.xray_frontend.ui.report

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.ui.utils.Result
import kotlinx.coroutines.launch

class ReportViewModel(
    private val slideId: Long,
    private val thunderscopeRepository: ThunderscopeRepository
) : ViewModel() {

    val currentlySelectedSlide = MutableLiveData<SlidesItem?>(null)

    init {
        getSlideItem(slideId)
    }

    private fun getSlideItem(slideId: Long) {
        viewModelScope.launch {
            thunderscopeRepository.getSlideItem(slideId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                    }

                    is Result.Success -> {
                        currentlySelectedSlide.value = result.data
                    }

                    is Result.Error -> {
                        // Handle error here
                    }
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context,
        private val slideId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                return ReportViewModel(slideId, ThunderscopeRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}