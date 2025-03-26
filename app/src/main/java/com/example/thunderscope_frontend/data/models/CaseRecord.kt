package com.example.thunderscope_frontend.data.models

import com.google.gson.annotations.SerializedName

data class CaseRecord(

	@field:SerializedName("doctor")
	val doctor: Doctor? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("year")
	val year: String? = null,

	@field:SerializedName("patient")
	val patient: Patient? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("time")
	val time: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)