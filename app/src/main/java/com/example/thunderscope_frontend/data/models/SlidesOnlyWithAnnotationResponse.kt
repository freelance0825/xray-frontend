package com.example.thunderscope_frontend.data.models

import android.graphics.Bitmap
import android.os.Parcelable
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SlidesOnlyWithAnnotationResponse(

    @field:SerializedName("slide_annotations")
    var slidesAnnotationList: MutableList<AnnotationResponse> = mutableListOf(),
) : Parcelable
