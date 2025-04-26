package com.example.thunderscope_frontend.data.models

import com.google.gson.annotations.SerializedName

data class DoctorRequest(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("specialist")
	val specialist: String? = null,

	@field:SerializedName("birth_date")
	val birthDate: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
