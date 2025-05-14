package com.example.xray_frontend.viewmodel

data class Patient(
    val id: Int,
    val name: String,
    val age: String,
    val gender: String,
    val address: String,
    val email: String?,
    val date_of_birth: String,
    val phone_number: String,
    val image_base64: String? // Nullable
)