package com.example.thunderscope_frontend.data.models

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
	val annotatedImage: String? = null,

	@field:SerializedName("caseRecordStatus")
	val caseRecordStatus: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("label")
	val label: String? = null
) : Parcelable
