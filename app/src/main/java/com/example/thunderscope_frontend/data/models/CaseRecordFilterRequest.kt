package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import android.transition.Slide
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaseRecordFilterRequest(

    @field:SerializedName("doctorId")
	val doctorId: Int? = null,

    @field:SerializedName("patientId")
	val patientId: Int? = null,
): Parcelable