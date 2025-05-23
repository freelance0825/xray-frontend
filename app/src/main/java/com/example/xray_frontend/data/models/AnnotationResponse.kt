package com.example.xray_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnnotationResponse(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("slideId")
	val slideId: Int? = null,

	@field:SerializedName("annotatedImage")
	var annotatedImage: String? = null,

	@field:SerializedName("caseRecordStatus")
	val caseRecordStatus: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("label")
	var label: String? = null
) : Parcelable
