package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(

	@field:SerializedName("imageBase64")
	var imageBase64: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("phoneNumber")
	val phoneNumber: String? = null,

	@field:SerializedName("gender")
	var gender: String? = null,

	@field:SerializedName("name")
	var name: String? = null,

	@field:SerializedName("dateOfBirth")
	var dateOfBirth: String? = null,

	@field:SerializedName("id")
	var id: Long? = null,

	@field:SerializedName("age")
	var age: String? = null,

	@field:SerializedName("email")
	val email: String? = null
) : Parcelable