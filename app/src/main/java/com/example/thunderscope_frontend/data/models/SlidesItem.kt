package com.example.thunderscope_frontend.data.models

import android.graphics.Bitmap
import android.os.Parcelable
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SlidesItem(

	@field:SerializedName("main_image")
	var mainImage: String? = null,

	@field:SerializedName("qr_code")
	val qrCode: String? = null,

	@field:SerializedName("report_id")
	val reportId: String? = null,

	@field:SerializedName("diagnosis")
	val diagnosis: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("specimen_type")
	val specimenType: String? = null,

	@field:SerializedName("case_record")
	val caseRecord: CaseRecord? = null,

	@field:SerializedName("ai_insights")
	val aiInsights: String? = null,

	@field:SerializedName("collection_site")
	val collectionSite: String? = null,

	@field:SerializedName("clinical_data")
	val clinicalData: String? = null,

	@field:SerializedName("microscopic_dc")
	val microscopicDc: String? = null,

	var isActive: Boolean = false,
	var isCurrentlySelected: Boolean = false,

	@Transient
	private var _bitmapImage: Bitmap? = null
) : Parcelable {

	val bitmapImage: Bitmap?
		get() {
			if (_bitmapImage == null && !mainImage.isNullOrEmpty()) {
				_bitmapImage = Base64Helper.convertToBitmap(mainImage)
			}
			return _bitmapImage
		}
}
