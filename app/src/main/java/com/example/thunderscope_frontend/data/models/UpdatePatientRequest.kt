package com.example.thunderscope_frontend.data.models

import com.google.gson.annotations.SerializedName
import java.io.File

data class UpdatePatientRequest(

	@field:SerializedName("image")
	val image: File? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("dob")
	val dob: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: String? = null,

	@field:SerializedName("state")
	val state: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("age")
	val age: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
