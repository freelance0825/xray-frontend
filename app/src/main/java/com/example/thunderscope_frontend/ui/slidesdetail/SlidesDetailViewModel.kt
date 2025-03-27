package com.example.thunderscope_frontend.ui.slidesdetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository

class SlidesDetailViewModel(
    private val thunderscopeRepository: ThunderscopeRepository
) : ViewModel() {
    val slideItems: LiveData<List<SlidesItem>> by lazy {
        thunderscopeRepository.getAllSlidesFromDB().asLiveData()
    }

    val currentlySelectedSlides = MediatorLiveData<SlidesItem?>().apply {
        addSource(slideItems) { slides ->
            if (!slides.isNullOrEmpty()) {
                value = slides[0]
            }
        }
    }

    fun updateSelectedSlide(slide: SlidesItem?) {
        currentlySelectedSlides.value = slide
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SlidesDetailViewModel::class.java)) {
                return SlidesDetailViewModel(ThunderscopeRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}