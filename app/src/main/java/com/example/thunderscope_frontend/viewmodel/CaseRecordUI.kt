package com.example.thunderscope_frontend.viewmodel

data class CaseRecordUI(


    val caseRecordId: Int,
    val physicianName: String,
    val patientName: String,
    val patientId: Int,
    val patientBirthdate: String,
    val patientAge: String,
    val patientGender: String,
    val lastUpdateDate: String,
    val lastUpdateTime: String,
    val status: String,
    val type: String,
)
