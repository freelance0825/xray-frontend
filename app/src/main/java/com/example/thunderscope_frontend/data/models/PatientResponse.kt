package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PatientResponse(

	@field:SerializedName("image_base64")
	var imageBase64: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: String? = null,

	@field:SerializedName("gender")
	var gender: String? = null,

	@field:SerializedName("name")
	var name: String? = null,

	@field:SerializedName("date_of_birth")
	var dateOfBirth: String? = null,

	@field:SerializedName("id")
	var id: Long? = null,

	@field:SerializedName("age")
	var age: String? = null,

	@field:SerializedName("updated_at")
	var updatedAt: String? = null,

	@field:SerializedName("status")
	var status: String? = null,

	@field:SerializedName("state")
	var state: String? = null,

	@field:SerializedName("type")
	var type: String? = null,

	@field:SerializedName("email")
	val email: String? = null
) : Parcelable