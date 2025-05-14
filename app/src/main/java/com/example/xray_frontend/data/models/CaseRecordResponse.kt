package com.example.xray_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaseRecordResponse(

    @field:SerializedName("doctor")
	val doctor: AuthDoctorResponse? = null,

    @field:SerializedName("date")
	val date: String? = null,

    @field:SerializedName("year")
	val year: String? = null,

    @field:SerializedName("patient")
	val patient: PatientResponse? = null,

    @field:SerializedName("id")
	val id: Int? = null,

    @field:SerializedName("time")
	val time: String? = null,

    @field:SerializedName("type")
	val type: String? = null,

    @field:SerializedName("todo")
    val todo: String? = null,

    @field:SerializedName("status")
	val status: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    var slides: MutableList<SlidesItem> = mutableListOf(),
): Parcelable