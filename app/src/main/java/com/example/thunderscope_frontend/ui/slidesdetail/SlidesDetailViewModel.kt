package com.example.thunderscope_frontend.ui.slidesdetail

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.slidesdetail.customview.Shape
import com.example.thunderscope_frontend.ui.slidesdetail.customview.ShapeType

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

    val selectedMenuOptions = MutableLiveData(SelectedMenu.SELECT)
    val selectedAnnotationShape = MutableLiveData(ShapeType.RECTANGLE)
    val selectedPaintColor = MutableLiveData(0)

    // IMAGE SETTINGS OPTIONS
    val gamma = MutableLiveData(1.0)
    val brightness = MutableLiveData(0.0)
    val contrast = MutableLiveData(1.0)
    val redAdjust = MutableLiveData(1.0)
    val greenAdjust = MutableLiveData(1.0)
    val blueAdjust = MutableLiveData(1.0)

    fun updateSelectedSlide(slide: SlidesItem?) {
        currentlySelectedSlides.value = slide
    }

    fun updateGamma(value: Int) {
        gamma.value = 0.1 + (value / 10.0)
    }

    fun updateBrightness(value: Int) {
        brightness.value = value - 100.0
    }

    fun updateContrast(value: Int) {
        contrast.value = 0.5 + (value / 100.0)
    }

    fun updateRed(value: Int) {
        redAdjust.value = value / 100.0
    }

    fun updateGreen(value: Int) {
        greenAdjust.value = value / 100.0
    }

    fun updateBlue(value: Int) {
        blueAdjust.value = value / 100.0
    }

    fun resetFilters() {
        gamma.value = 1.0
        brightness.value = 0.0
        contrast.value = 1.0
        redAdjust.value = 1.0
        greenAdjust.value = 1.0
        blueAdjust.value = 1.0
    }

    enum class SelectedMenu {
        SELECT,
        ANNOTATE,
        ANNOTATE_COLOR,
        IMAGE_SETTINGS
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