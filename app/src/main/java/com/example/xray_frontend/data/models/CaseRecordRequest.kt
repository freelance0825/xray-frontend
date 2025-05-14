package com.example.xray_frontend.data.models

import com.google.gson.annotations.SerializedName

data class CaseRecordRequest(

	@field:SerializedName("date")
	var date: String? = null,

	@field:SerializedName("doctorId")
	var doctorId: Int? = null,

	@field:SerializedName("patientId")
	var patientId: Int? = null,

	@field:SerializedName("year")
	var year: String? = null,

	@field:SerializedName("time")
	var time: String? = null,

	@field:SerializedName("type")
	var type: String? = null,
)
