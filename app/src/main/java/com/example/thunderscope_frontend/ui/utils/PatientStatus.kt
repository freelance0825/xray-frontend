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

    companion object {
        fun getTranslatedStringValue(value: String): String {
            return when (value) {
                NOT_STARTED.name -> "Not Started"
                ON_PROGRESS.name -> "On Progress"
                COMPLETED.name -> "Completed"
                else -> "Completed"
            }
        }
    }
}
