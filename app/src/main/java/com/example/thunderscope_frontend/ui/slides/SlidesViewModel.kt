package com.example.thunderscope_frontend.ui.slides

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.AnnotationItem
import com.example.thunderscope_frontend.data.models.AnnotationResponse
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.models.PhotoItem
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.ui.utils.Result
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlidesViewModel(
    private val caseRecordId: Int,
    private val thunderscopeRepository: ThunderscopeRepository
) : ViewModel() {

    val slidesItem = MutableLiveData<MutableList<SlidesItem>>(mutableListOf())
    val caseRecordResponse = MutableLiveData<CaseRecordResponse>(null)
    val activeSlidesItem = MutableLiveData<MutableList<SlidesItem>>(mutableListOf())
    val currentlySelectedSlide = MutableLiveData<SlidesItem?>(null)

    val selectedAnnotationListByActiveSlides =
        MutableLiveData<MutableList<AnnotationResponse>>(mutableListOf())

    val photoGalleryItems = MutableLiveData(this.getDummyPhotos())

    val isOpeningRightMenu = MutableLiveData(false)

    var isDraggingSlides = false
    var isDraggingPhotos = false

    init {
        getCaseById()
        getAllSlides()
    }

    fun getCaseById() {
        viewModelScope.launch {
            thunderscopeRepository.getCaseById(caseRecordId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                    }

                    is Result.Success -> {
                        caseRecordResponse.value = result.data
                    }

                    is Result.Error -> {
                        // Handle error here
                    }
                }
            }
        }
    }

    fun getAllSlides() {
        viewModelScope.launch {
            thunderscopeRepository.getAllSlides(caseRecordId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                    }

                    is Result.Success -> {
                        slidesItem.value = result.data.toMutableList()
                    }

                    is Result.Error -> {
                        // Handle error here
                    }
                }
            }
        }
    }

    fun toggleSlidesItem(slideItem: SlidesItem, isFromRightMenu: Boolean = false) {
        getAnnotationsBySlidesId(slideItem.id)

        val updatedList = slidesItem.value?.map { item ->
            if (item.id == slideItem.id) {
                item.copy(
                    isCurrentlySelected = true,
                    isActive = if (isFromRightMenu) item.isActive else !item.isActive
                )
            } else {
                item.copy(isCurrentlySelected = false)
            }
        } ?: return

        val activeList = updatedList.filter { it.isActive }

        val finalList = if (!isFromRightMenu && activeList.none { it.isCurrentlySelected }) {
            activeList.mapIndexed { index, item ->
                if (index == 0) item.copy(isCurrentlySelected = true) else item
            }
        } else activeList

        slidesItem.value = updatedList.toMutableList()
        activeSlidesItem.value = finalList.toMutableList()
        currentlySelectedSlide.value =
            finalList.find { it.isCurrentlySelected } ?: finalList.firstOrNull()
    }

    fun getAnnotationsBySlidesId(slidesId: Long?) {
        viewModelScope.launch {
            thunderscopeRepository.getAnnotationsBySlidesId(slidesId ?: 0).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Handle loading here
                    }

                    is Result.Success -> {
                        selectedAnnotationListByActiveSlides.value =
                            result.data.slidesAnnotationList
                    }

                    is Result.Error -> {
                        // Handle error here
                    }
                }
            }
        }
    }

    // DUMMY DATA - CHANGE LATER
    private fun getDummyPhotos(): MutableList<PhotoItem> {
        val hours = listOf(
            "10:00 AM", "10:15 AM", "10:30 AM", "10:45 AM", "11:00 AM",
            "11:15 AM", "11:30 AM", "11:45 AM", "12:00 PM", "12:15 PM",
            "12:30 PM", "12:45 PM", "01:00 PM", "01:15 PM", "01:30 PM",
            "01:45 PM", "02:00 PM", "02:15 PM", "02:30 PM", "02:45 PM",
            "03:00 PM", "03:15 PM", "03:30 PM", "03:45 PM", "04:00 PM",
            "04:15 PM", "04:30 PM", "04:45 PM", "05:00 PM", "05:15 PM"
        )

        return List(30) { index ->
            PhotoItem(
                id = index,
                imageRes = R.drawable.asset_image_annotate_2,
                name = "OD Nasal Color",
                hour = hours[index % hours.size]
            )
        }.toMutableList()
    }

    // DUMMY DATA - CHANGE LATER
    private fun generateDummyAnnotationItem() = mutableListOf(
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
        ),
        AnnotationItem(
            id = 3,
            label = "Annotation 3",
            date = "12/03/2025",
            dummyImageRes = R.drawable.asset_image_annotate_3
        ),
        AnnotationItem(
            id = 4,
            label = "Annotation 4",
            date = "13/03/2025",
            dummyImageRes = R.drawable.asset_image_annotate_4
        ),
        AnnotationItem(
            id = 5,
            label = "Annotation 5",
            date = "14/03/2025",
            dummyImageRes = R.drawable.asset_image_annotate_5
        ),
    )

    fun insertSlides(slides: List<SlidesItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            thunderscopeRepository.insertSlides(slides)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val caseRecordId: Int,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SlidesViewModel::class.java)) {
                return SlidesViewModel(caseRecordId, ThunderscopeRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}