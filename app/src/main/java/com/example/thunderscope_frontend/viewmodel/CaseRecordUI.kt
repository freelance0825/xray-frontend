package com.example.thunderscope_frontend.viewmodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaseRecordUI(


    val caseRecordId: Int,
    val physicianName: String,
    val patientName: String,
    val patientId: Int,
    val patientImage: String,
    val patientPhoneNumber: String,
    val patientBirthdate: String,
    val patientAge: String,
    val patientGender: String,
    val lastUpdateDate: String,
    val lastUpdateTime: String,
    val status: String,
    val type: String,
) : Parcelable
