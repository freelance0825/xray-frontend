package com.example.thunderscope_frontend.data.repo

import android.content.Context
import android.util.Log
import com.example.thunderscope_frontend.data.local.SlidesDatabase
import com.example.thunderscope_frontend.data.models.PostTestReviewPayload
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

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

    fun getSlideItem(slideId: Long) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getSlideItem(slideId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun updateSlide(id: Long, payload: PostTestReviewPayload): Flow<Result<SlidesItem>> = flow {
        emit(Result.Loading)

        try {
            val formData = mutableMapOf<String, RequestBody>()

            payload.caseRecordId?.let { formData["caseRecordId"] = it.toString().toRequestBody() }
            payload.microscopicDc?.let { formData["microscopicDc"] = it.toRequestBody() }
            payload.diagnosis?.let { formData["diagnosis"] = it.toRequestBody() }

            Log.e("FTEST", "updateSlide: ${payload.caseRecordId}", )

            val payloadSign = payload.doctorSignature

            val doctorSignaturePart =
                payloadSign?.asRequestBody("image/*".toMediaTypeOrNull())?.let {
                    MultipartBody.Part.createFormData(
                        "doctorSignature",
                        payloadSign.name,
                        it
                    )
                }

            val response = apiService.updateSlide(id, formData, doctorSignaturePart)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            emit(Result.Error("Server error: ${e.message}"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun String.toRequestBody() =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    private fun Any.toMultipart(name: String): MultipartBody.Part {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), this.toString())
        return MultipartBody.Part.createFormData(name, "${name}.jpg", requestFile)
    }

//    private fun String.formatToServerDate(): String {
//        return try {
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
//            val outputFormat = SimpleDateFormat("MM-dd-yyyy hh:mma", Locale.US)
//            val date = inputFormat.parse(this)
//            outputFormat.format(date ?: this)
//        } catch (e: Exception) {
//            this // Return the original if parsing fails
//        }
//    }

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