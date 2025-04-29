package com.example.thunderscope_frontend.ui.utils

import com.example.thunderscope_frontend.ui.utils.PatientStatus.COMPLETED
import com.example.thunderscope_frontend.ui.utils.PatientStatus.NOT_STARTED
import com.example.thunderscope_frontend.ui.utils.PatientStatus.ON_PROGRESS

enum class CaseRecordStatus {
    HIGH_PRIORITY,
    IN_PREPARATIONS,
    FOR_REVIEW,
    COMPLETED;

    fun getTranslatedStringValue(): String {
        return when (this) {
            HIGH_PRIORITY -> "High Priority"
            IN_PREPARATIONS -> "In Preparations"
            FOR_REVIEW -> "For Review"
            COMPLETED -> "Completed"
        }
    }
}