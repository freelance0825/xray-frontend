package com.example.thunderscope_frontend.viewmodel

data class Doctor(
    val id: Int,
    val email: String,
    val name: String,
    val phone_number: String,
    val specialist: String,
    val birth_date: String,
    val signature: String? // Nullable
)
