package com.example.xray_frontend.ui.utils.enums

enum class CaseRecordStatus() {
    FOR_REVIEW,
    IN_PROGRESS,
    COMPLETED;

    fun getTranslatedStringValue(): String {
        return when (this) {
            FOR_REVIEW -> "For Review"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
        }
    }

    companion object {
        fun getTranslatedStringValue(value: String): String? {
            return when (value) {
                FOR_REVIEW.name -> "For Review"
                IN_PROGRESS.name -> "In Progress"
                COMPLETED.name -> "Completed"
                else -> null
            }
        }
    }
}