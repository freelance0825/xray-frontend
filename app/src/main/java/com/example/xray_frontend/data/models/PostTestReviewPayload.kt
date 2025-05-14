package com.example.xray_frontend.data.models

import com.google.gson.annotations.SerializedName
import java.io.File

data class PostTestReviewPayload(

	@field:SerializedName("caseRecordId")
	val caseRecordId: Long? = null,

	@field:SerializedName("microscopicDc")
	val microscopicDc: String? = null,

	@field:SerializedName("dateAndTime")
	val dateAndTime: String? = null,

	@field:SerializedName("diagnosis")
	val diagnosis: String? = null,

	@field:SerializedName("doctorSignature")
	val doctorSignature: File? = null,
)
