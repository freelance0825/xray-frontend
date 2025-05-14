package com.example.xray_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SlidesOnlyWithAnnotationResponse(

    @field:SerializedName("slide_annotations")
    var slidesAnnotationList: MutableList<AnnotationResponse> = mutableListOf(),
) : Parcelable
