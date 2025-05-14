package com.example.xray_frontend.viewmodel

import java.io.Serializable

data class PatientRecordUI(

    val patientId: Int,

    val patientImage: String,

    val patientName: String,

    val patientBirthDate: String,

    val patientGender: String,

    val patientAge: String,

    val patientPhoneNumber: String,

    val patientEmail: String,

    val patientState: String,

    val patientStatus: String,

    val patientType: String,

    val patientAddress: String,

    val patientLastUpdate: String,

    ) : Serializable