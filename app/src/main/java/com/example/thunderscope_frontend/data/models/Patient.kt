package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(

	@field:SerializedName("imageBase64")
	val imageBase64: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("phoneNumber")
	val phoneNumber: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("dateOfBirth")
	val dateOfBirth: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("age")
	val age: String? = null,

	@field:SerializedName("email")
	val email: String? = null
) : Parcelable