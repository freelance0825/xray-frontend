package com.example.xray_frontend.ui.utils.helpers

sealed class Result<out R> private constructor() {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}