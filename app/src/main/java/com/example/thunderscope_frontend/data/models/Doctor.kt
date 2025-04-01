package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("signature")
	var signature: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("specialist")
	val specialist: String? = null,

	@field:SerializedName("id")
	val id: Long? = null,

	@field:SerializedName("email")
	val email: String? = null
) : Parcelable