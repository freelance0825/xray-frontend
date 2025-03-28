package com.example.thunderscope_frontend.data.models

import android.graphics.Bitmap
import android.os.Parcelable
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SlidesItem(

	@field:SerializedName("mainImage")
	val mainImage: String? = null,

	@field:SerializedName("qrCode")
	val qrCode: String? = null,

	@field:SerializedName("reportId")
	val reportId: String? = null,

	@field:SerializedName("diagnosis")
	val diagnosis: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("specimenType")
	val specimenType: String? = null,

	@field:SerializedName("caseRecord")
	val caseRecord: CaseRecord? = null,

	@field:SerializedName("aiInsights")
	val aiInsights: String? = null,

	@field:SerializedName("collectionSite")
	val collectionSite: String? = null,

	@field:SerializedName("clinicalData")
	val clinicalData: String? = null,

	var isActive: Boolean = false,
	var isCurrentlySelected: Boolean = false,

	@Transient // Hindari serialisasi dalam Parcelable
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
