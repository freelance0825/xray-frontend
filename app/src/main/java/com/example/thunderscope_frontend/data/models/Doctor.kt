package com.example.thunderscope_frontend.data.models

import com.google.gson.annotations.SerializedName

data class Doctor(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("signature")
	val signature: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("email")
	val email: String? = null
)