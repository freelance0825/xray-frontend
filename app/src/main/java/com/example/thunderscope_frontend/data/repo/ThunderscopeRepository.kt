package com.example.thunderscope_frontend.data.repo

import android.content.Context
import com.example.thunderscope_frontend.data.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.thunderscope_frontend.ui.utils.Result

class ThunderscopeRepository {
    private val apiService = ApiConfig.getApiService()

    fun getAllSlides(caseId: Int) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getAllSlides(caseId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)
}