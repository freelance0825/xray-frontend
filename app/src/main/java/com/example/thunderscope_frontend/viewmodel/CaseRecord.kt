package com.example.thunderscope_frontend.viewmodel

data class CaseRecord(
    val id: Int,
    val doctor: Doctor,
    val patient: Patient,
    val date: String,
    val time: String,
    val year: String,
    val status: String?,
    val type: String
)