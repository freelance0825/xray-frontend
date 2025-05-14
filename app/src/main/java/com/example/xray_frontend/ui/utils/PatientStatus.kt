package com.example.xray_frontend.ui.utils

enum class PatientStatus {
    NOT_STARTED,
    FOR_REVIEW,
    IN_PROGRESS,
    COMPLETED;

    fun getTranslatedStringValue(): String {
        return when (this) {
            NOT_STARTED -> "Not Started"
            FOR_REVIEW -> "For Review"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
        }
    }

    companion object {
        fun getTranslatedStringValue(value: String?): String? {
            return when (value?.uppercase()) {
                NOT_STARTED.name -> "Not Started"
                FOR_REVIEW.name -> "For Review"
                IN_PROGRESS.name -> "In Progress"
                COMPLETED.name -> "Completed"
                else -> null
            }
        }
    }
}
