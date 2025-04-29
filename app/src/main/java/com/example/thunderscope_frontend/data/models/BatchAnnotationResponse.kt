package com.example.thunderscope_frontend.data.models

import com.google.gson.annotations.SerializedName

data class BatchAnnotationResponse(

	@field:SerializedName("annotationData")
	val annotationData: List<AnnotationDataItem?>? = null,

	@field:SerializedName("slideId")
	val slideId: Int? = null
)

data class AnnotationDataItem(

	@field:SerializedName("annotatedImage")
	val annotatedImage: List<String?>? = null,

	@field:SerializedName("label")
	val label: List<String?>? = null
)
