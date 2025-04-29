package com.example.thunderscope_frontend.ui.utils

enum class PatientStatus {
    NOT_STARTED,
    ON_PROGRESS,
    COMPLETED;

    fun getTranslatedStringValue(): String {
        return when (this) {
            NOT_STARTED -> "Not Started"
            ON_PROGRESS -> "On Progress"
            COMPLETED -> "Completed"
        }
    }
}
