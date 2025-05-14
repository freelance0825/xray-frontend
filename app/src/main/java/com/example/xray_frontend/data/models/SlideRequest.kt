package com.example.xray_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class SlideRequest(

	@field:SerializedName("caseRecordId")
	var caseRecordId: Int? = null,

	@field:SerializedName("specimen_type")
	var specimenType: String? = null,

	@field:SerializedName("date_and_time")
	var dateAndTime: String? = null,

	@field:SerializedName("main_image")
	var mainImage: File? = null,

	@field:SerializedName("microscopic_dc")
	var microscopicDc: String? = null,

	@field:SerializedName("report_id")
	var reportId: String? = null,

	@field:SerializedName("clinical_data")
	var clinicalData: String? = null,

	@field:SerializedName("doctor_signature")
	var doctorSignature: String? = null,

	@field:SerializedName("qr_code")
	var qrCode: String? = null,

	@field:SerializedName("collection_site")
	var collectionSite: String? = null
) : Parcelable
