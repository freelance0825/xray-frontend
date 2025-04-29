package com.example.thunderscope_frontend.data.remote

import com.example.thunderscope_frontend.data.models.AuthDoctorRequest
import com.example.thunderscope_frontend.data.models.AuthDoctorResponse
import com.example.thunderscope_frontend.data.models.BatchAnnotationResponse
import com.example.thunderscope_frontend.data.models.CaseRecordFilterRequest
import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.models.PatientResponse
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.data.models.SlidesItemWithAnnotationResponse
import com.example.thunderscope_frontend.data.models.SlidesOnlyWithAnnotationResponse
import com.example.thunderscope_frontend.data.models.UpdatePatientRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {
    @POST("/api/doctors/add")
    suspend fun registerDoctor(@Body authDoctorRequest: AuthDoctorRequest): AuthDoctorResponse

    @POST("/api/doctors/login")
    suspend fun loginDoctor(@Body authDoctorRequest: AuthDoctorRequest): AuthDoctorResponse

    @GET("case/{id}")
    suspend fun getCaseById(@Path("id") caseId: Int): CaseRecordResponse

    @GET("slides/case/{id}")
    suspend fun getAllSlides(@Path("id") caseId: Int): List<SlidesItem>

    @GET("slides/{id}")
    suspend fun getSlideItem(@Path("id") slidesId: Long): SlidesItem

    @GET("slides/annotations/{id}")
    suspend fun getAnnotationsBySlidesId(@Path("id") slidesId: Long): SlidesOnlyWithAnnotationResponse

    @GET("slides/annotations/{id}")
    suspend fun getSlidesWithAnnotations(@Path("id") slidesId: Long): SlidesItemWithAnnotationResponse

    @Multipart
    @POST("/api/slides/annotations/batch")
    suspend fun uploadAnnotationsBatch(
        @Part("slideId") slideId: RequestBody,
        @Part annotatedImages: List<MultipartBody.Part>,
        @Part labels: List<MultipartBody.Part>
    ): List<BatchAnnotationResponse>

    @Multipart
    @PUT("slides/{id}")
    suspend fun updateSlide(
        @Path("id") id: Long,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part doctorSignature: MultipartBody.Part? = null
    ): SlidesItem

    @GET("case/list")
    suspend fun getCaseRecords(): List<CaseRecordResponse>

    @POST("case/filter")
    suspend fun getCaseRecordsFilterId(
        @Body caseRecordFilterRequest: CaseRecordFilterRequest
    ): List<CaseRecordResponse>

    @GET("patients")
    suspend fun getPatientRecords(): List<PatientResponse>

    @GET("doctors")
    suspend fun getDoctorRecords(): List<AuthDoctorResponse>

    @DELETE("patients/{id}")
    suspend fun deletePatient(
        @Path("id") patientId: Int
    ): ResponseBody

    @Multipart
    @PUT("patients/{id}")
    suspend fun updatePatient(
        @Path("id") patientId: Int,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part doctorSignature: MultipartBody.Part? = null
    ): PatientResponse

    @Multipart
    @POST("patients/add")
    suspend fun addPatient(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part doctorSignature: MultipartBody.Part? = null
    ): PatientResponse
}