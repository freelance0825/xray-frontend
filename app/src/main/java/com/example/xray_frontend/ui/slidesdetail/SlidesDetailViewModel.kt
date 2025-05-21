package com.example.xray_frontend.ui.slidesdetail

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.AnnotationItem
import com.example.xray_frontend.data.models.AnnotationResponse
import com.example.xray_frontend.data.models.PostTestReviewPayload
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.data.models.SlidesItemWithAnnotationResponse
import com.example.xray_frontend.data.models.toSlidesItemWithAnnotationResponse
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.ui.slidesdetail.customview.ShapeType
import com.example.xray_frontend.ui.utils.helpers.Base64Helper
import com.example.xray_frontend.ui.utils.helpers.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.addAll
import kotlin.text.orEmpty
import kotlin.text.toMutableList

class SlidesDetailViewModel(
    private val thunderscopeRepository: ThunderscopeRepository,
    val slideIdList: MutableList<Long> = arrayListOf()
) : ViewModel() {
    val isLoading = MutableLiveData(false)
    val isSuccessfullySavingAnnotation = MutableLiveData(false)
    val errorMessage = MutableLiveData("")
    val annotationSuccessMessage = MutableLiveData("")

    //    val slideItems: LiveData<List<SlidesItem>> by lazy {
//        thunderscopeRepository.getAllSlidesFromDB().asLiveData()
//    }
    val slideItems: MutableLiveData<List<SlidesItem>> = MutableLiveData(arrayListOf())

    val currentlySelectedSlides = MediatorLiveData<SlidesItem?>().apply {
//        addSource(slideItems) { slides ->
//            if (!slides.isNullOrEmpty()) {
//                value = slides[0]
//            }
//        }
    }

    private val networkAnnotationList = MediatorLiveData<MutableList<AnnotationResponse>>()
    private val localAnnotationList = MediatorLiveData<MutableList<AnnotationResponse>>()

    val allAnnotationList = MediatorLiveData<MutableList<AnnotationResponse>>().apply {
        addSource(localAnnotationList) { localList ->
            val combined = (localList.orEmpty() + networkAnnotationList.value.orEmpty()).toMutableList()
            value = combined
        }
        addSource(networkAnnotationList) { networkList ->
            val combined = (localAnnotationList.value.orEmpty() + networkList.orEmpty()).toMutableList()
            value = combined
        }
    }

    val currentlySelectedSlidesItemWithAnnotationResponse =
        MediatorLiveData<SlidesItemWithAnnotationResponse?>(null)
    val currentlySelectedSlidesId = MediatorLiveData(slideIdList[0])

    val signatureImageFile = MutableLiveData<File>(null)

    val selectedMenuOptions = MutableLiveData(SelectedMenu.SELECT)
    val selectedAnnotationShape = MutableLiveData(ShapeType.RECTANGLE)
    val selectedPaintColor = MutableLiveData(0)

    val selectedViewSettings = MutableLiveData(SelectedViewSettings.ORIGINAL)
    val selectedSegmentationSettings = MutableLiveData(SelectedSegmentationSettings.ODM)

    val isEditingDiagnosis = MutableLiveData(false)

    // IMAGE SETTINGS OPTIONS
    val gamma = MutableLiveData(1.0)
    val brightness = MutableLiveData(0.0)
    val contrast = MutableLiveData(1.0)
    val redAdjust = MutableLiveData(1.0)
    val greenAdjust = MutableLiveData(1.0)
    val blueAdjust = MutableLiveData(1.0)

    init {
        getSlidesById(slideIdList[0])
    }

    fun getSlidesById(slidesId: Long?) {
        viewModelScope.launch {
            thunderscopeRepository.getSlidesWithAnnotations(slidesId ?: 0).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        errorMessage.value = ""
                        // Handle loading here
                    }

                    is Result.Success -> {
//                        val slideWithAnnotation= result.data.toSlidesItemWithAnnotationResponse()

                        errorMessage.value = ""
                        currentlySelectedSlidesItemWithAnnotationResponse.value = result.data
                        networkAnnotationList.value = result.data.slidesAnnotationList
                    }

                    is Result.Error -> {
                        errorMessage.value = result.error
                        // Handle error here
                    }
                }
            }
        }
    }

    fun saveLocalAnnotationToNetwork() {
        if (localAnnotationList.value.isNullOrEmpty()) {
            return
        }

        val mappedAnnotationImages = localAnnotationList.value?.map { it.annotatedImage ?: "" } ?: listOf()
        val mappedAnnotationLabels = localAnnotationList.value?.map { it.label ?: " "} ?: listOf()

        viewModelScope.launch {
            thunderscopeRepository.uploadAnnotationBatch(
                currentlySelectedSlidesId.value ?: 0,
                mappedAnnotationImages,
                mappedAnnotationLabels
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                        Log.d("SlidesDetailViewModel", "Saving annotation: Loading")
                        errorMessage.value = ""
                        annotationSuccessMessage.value = ""
                    }
                    is Result.Success -> {
                        // Handle success here
                        Log.d("SlidesDetailViewModel", "Saving annotation: Success ${result.data}")
                        errorMessage.value = ""
                        annotationSuccessMessage.value = "Successfully Saving Local Annotations!"
                        isSuccessfullySavingAnnotation.value = true
                    }
                    is Result.Error -> {
                        // Handle error here
                        Log.d("SlidesDetailViewModel", "Saving annotation: Error ${result.error}")
                        annotationSuccessMessage.value = ""
                        errorMessage.value = result.error
                    }
                }
            }
        }
    }

    fun updateSlide(id: Long, payload: PostTestReviewPayload): Flow<Result<SlidesItem>> {
        return thunderscopeRepository.updateSlide(id, payload)
    }

    fun updateSelectedSlide(slideId: Long) {
        localAnnotationList.value?.clear()
        selectedMenuOptions.value = SelectedMenu.SELECT
        isEditingDiagnosis.value = false
        currentlySelectedSlidesId.value = slideId
    }

    fun storeNewLocalAnnotation(bitmap: Bitmap, label: String) {
        val newLocalAnnotation = AnnotationResponse().apply {
            this.label = label
            this.annotatedImage = Base64Helper.bitmapToBase64(bitmap)
        }

        val updatedList = mutableListOf<AnnotationResponse>().apply {
            add(newLocalAnnotation)
            localAnnotationList.value?.let { addAll(it) }
        }

        localAnnotationList.value = updatedList
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

    enum class SelectedViewSettings {
        ORIGINAL,
        SEGMENTATION,
    }

    enum class SelectedSegmentationSettings {
        ODM,
        OCM,
        ODC,
        FC,
        MM
    }


    // DELETED LATER AFTER THE DEMO
    fun generateDummyAnnotationItem() = mutableListOf(
        AnnotationItem(
            id = 1,
            label = "Annotation 1",
            date = "10/03/2025",
            dummyImageRes = R.drawable.asset_image_annotate_1
        ),
        AnnotationItem(
            id = 2,
            label = "Annotation 2",
            date = "11/03/2025",
            dummyImageRes = R.drawable.asset_image_annotate_2
        )
    )

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context,
        private val slideArrayList: ArrayList<SlidesItem> = arrayListOf()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SlidesDetailViewModel::class.java)) {
                val slideIdList = slideArrayList.map { it.id ?: 0 }.toMutableList()

                return SlidesDetailViewModel(ThunderscopeRepository(context), slideIdList) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}