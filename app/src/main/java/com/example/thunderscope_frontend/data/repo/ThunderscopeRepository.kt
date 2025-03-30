package com.example.thunderscope_frontend.data.repo

import android.content.Context
import com.example.thunderscope_frontend.data.local.SlidesDatabase
import com.example.thunderscope_frontend.data.models.SlidesEntity
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.remote.ApiConfig
import com.example.thunderscope_frontend.data.utils.SlidesMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.thunderscope_frontend.ui.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class ThunderscopeRepository(
    context: Context
) {
    private val apiService = ApiConfig.getApiService(context)
    private val slidesDao = SlidesDatabase.getDatabase(context).slidesDao()

    fun getAllSlides(caseId: Int) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getAllSlides(caseId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllSlidesFromDB(): Flow<List<SlidesItem>> =
        slidesDao.getAllSlides().map { it.map { slideList -> SlidesMapper.toDomain(slideList) } }
            .flowOn(Dispatchers.IO)

    suspend fun insertSlides(slides: List<SlidesItem>) {
        val slidesEntities = slides.map { SlidesMapper.toEntity(it) }
        runBlocking { slidesDao.clearAllSlides() }
        slidesDao.insertSlides(slidesEntities)
    }

    suspend fun clearSlides() {
        slidesDao.clearAllSlides()
    }

    // DUMMY SLIDES FOR CREATE NEW TEST PURPOSE!!!

    suspend fun generateDummySlidesToDatabaseForMVPPurpose() {
        insertSlides(slidesList)
    }
}