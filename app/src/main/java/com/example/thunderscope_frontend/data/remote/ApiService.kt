package com.example.thunderscope_frontend.data.remote

import com.example.thunderscope_frontend.data.models.SlidesItem
import retrofit2.http.*

interface ApiService {
    @GET("slides/case/{id}")
    suspend fun getAllSlides(@Path("id") caseId: Int): List<SlidesItem>
}