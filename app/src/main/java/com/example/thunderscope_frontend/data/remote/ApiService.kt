package com.example.thunderscope_frontend.data.remote

import com.example.thunderscope_frontend.data.models.DoctorRequest
import com.example.thunderscope_frontend.data.models.DoctorResponse
import com.example.thunderscope_frontend.data.models.SlidesItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @GET("slides/case/{id}")
    suspend fun getAllSlides(@Path("id") caseId: Int): List<SlidesItem>

    @GET("slides/{id}")
    suspend fun getSlideItem(@Path("id") slidesId: Long): SlidesItem

    @Multipart
    @PUT("slides/{id}")
    suspend fun updateSlide(
        @Path("id") id: Long,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part doctorSignature: MultipartBody.Part? = null
    ): SlidesItem


    @POST("/api/doctors/add")
    @Headers("Authorization: ")
    suspend fun registerDoctor(@Body doctorRequest: DoctorRequest): DoctorResponse

}