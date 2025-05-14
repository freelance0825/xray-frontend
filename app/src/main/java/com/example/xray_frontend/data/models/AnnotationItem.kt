package com.example.xray_frontend.data.models

import com.google.gson.annotations.SerializedName


data class AnnotationItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("imageBase64")
	val imageBase64: String? = null,
	val dummyImageRes: Int = 0,

	@field:SerializedName("year")
	val year: String? = null,

	@field:SerializedName("coordinates")
	val coordinates: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("label")
	val label: String? = null,

	@field:SerializedName("time")
	val time: String? = null
)