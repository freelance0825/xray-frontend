package com.example.xray_frontend.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.xray_frontend.R
import com.example.xray_frontend.data.local.database.SlidesDatabase
import com.example.xray_frontend.data.local.datastore.AuthDataStore
import com.example.xray_frontend.data.models.AuthDoctorRequest
import com.example.xray_frontend.data.models.BatchAnnotationResponse
import com.example.xray_frontend.data.models.CaseRecordFilterRequest
import com.example.xray_frontend.data.models.CaseRecordRequest
import com.example.xray_frontend.data.models.PostTestReviewPayload
import com.example.xray_frontend.data.models.SlideRequest
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.data.models.UpdatePatientRequest
import com.example.xray_frontend.data.remote.ApiConfig
import com.example.xray_frontend.data.utils.SlidesMapper
import com.example.xray_frontend.ui.utils.Base64Helper
import com.example.xray_frontend.ui.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThunderscopeRepository(private val context: Context) {
    private val authDataStore = AuthDataStore.getInstance(context)
    private val apiService = ApiConfig.getApiService(authDataStore)
    private val slidesDao = SlidesDatabase.getDatabase(context).slidesDao()

    fun loginDoctor(email: String, password: String) = flow {
        emit(Result.Loading)
        try {
            val loginRequest = AuthDoctorRequest(email = email, password = password)
            val loginResponse = apiService.loginDoctor(loginRequest)

            // SAVE TOKEN TO DATASTORE
            runBlocking {
                authDataStore.saveToken(loginResponse.token.toString())
                authDataStore.saveDoctorId(loginResponse.id?.toInt() ?: 0)
            }

            emit(Result.Success(loginResponse))
        } catch (e: Exception) {
            if (e.message?.contains("404") == true) {
                emit(Result.Error("User not found"))
            } else {
                emit(Result.Error("Login Failed, Please try again!"))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun registerDoctor(authDoctorRequest: AuthDoctorRequest) = flow {
        emit(Result.Loading)
        try {
            val registerResponse = apiService.registerDoctor(authDoctorRequest)

            // SAVE TOKEN TO DATASTORE
            runBlocking {
                authDataStore.saveToken(registerResponse.token.toString())
                authDataStore.saveDoctorId(registerResponse.id?.toInt() ?: 0)
            }

            emit(Result.Success(registerResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getToken() = authDataStore.getToken()

    suspend fun logout() {
        authDataStore.clearPreferences()
    }

    fun getAllCases() = flow {
        emit(Result.Loading)
        try {
            val caseRecordResponse = apiService.getCaseRecords()
            emit(Result.Success(caseRecordResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllCasesFilterId(patientId: Int? = null, doctorId: Int? = null) = flow {
        emit(Result.Loading)
        try {
            val caseRecordFilterRequest =
                CaseRecordFilterRequest(patientId = patientId, doctorId = doctorId)
            val caseRecordResponse = apiService.getCaseRecordsFilterId(caseRecordFilterRequest)
            emit(Result.Success(caseRecordResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllPatients() = flow {
        emit(Result.Loading)
        try {
            val patientResponse = apiService.getPatientRecords()
            emit(Result.Success(patientResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun deletePatient(patientId: Int) = flow {
        emit(Result.Loading)
        try {
            val patientResponse = apiService.deletePatient(patientId)
            emit(Result.Success(patientResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun updatePatient(patientId: Int, patientRequest: UpdatePatientRequest) = flow {
        emit(Result.Loading)
        try {
            val formData = mutableMapOf<String, RequestBody>()
            patientRequest.name?.let { formData["name"] = it.toRequestBody() }
            patientRequest.email?.let { formData["email"] = it.toRequestBody() }
            patientRequest.phoneNumber?.let { formData["phoneNumber"] = it.toRequestBody() }
            patientRequest.dob?.let { formData["dob"] = it.toRequestBody() }
            patientRequest.age?.let { formData["age"] = it.toRequestBody() }
            patientRequest.address?.let { formData["address"] = it.toRequestBody() }
            patientRequest.gender?.let { formData["gender"] = it.toRequestBody() }
            patientRequest.state?.let { formData["state"] = it.toRequestBody() }
            patientRequest.type?.let { formData["type"] = it.toRequestBody() }
            patientRequest.status?.let { formData["status"] = it.toRequestBody() }

            val payloadImage = patientRequest.image

            val patientImagePart =
                payloadImage?.asRequestBody("image/*".toMediaTypeOrNull())?.let {
                    MultipartBody.Part.createFormData(
                        "image",
                        payloadImage.name,
                        it
                    )
                }

            val response = apiService.updatePatient(patientId, formData, patientImagePart)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun addPatient(patientRequest: UpdatePatientRequest) = flow {
        emit(Result.Loading)
        try {
            val formData = mutableMapOf<String, RequestBody>()
            patientRequest.name?.let { formData["name"] = it.toRequestBody() }
            patientRequest.email?.let { formData["email"] = it.toRequestBody() }
            patientRequest.phoneNumber?.let { formData["phoneNumber"] = it.toRequestBody() }
            patientRequest.dob?.let { formData["dob"] = it.toRequestBody() }
            patientRequest.age?.let { formData["age"] = it.toRequestBody() }
            patientRequest.address?.let { formData["address"] = it.toRequestBody() }
            patientRequest.gender?.let { formData["gender"] = it.toRequestBody() }
            patientRequest.state?.let { formData["state"] = it.toRequestBody() }
            patientRequest.type?.let { formData["type"] = it.toRequestBody() }
            patientRequest.status?.let { formData["status"] = it.toRequestBody() }

            val payloadImage = patientRequest.image

            val patientImagePart =
                payloadImage?.asRequestBody("image/*".toMediaTypeOrNull())?.let {
                    MultipartBody.Part.createFormData(
                        "image",
                        payloadImage.name,
                        it
                    )
                }

            val response = apiService.addPatient(formData, patientImagePart)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllDoctors() = flow {
        emit(Result.Loading)
        try {
            val doctorResponse = apiService.getDoctorRecords()
            emit(Result.Success(doctorResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getCaseById(caseId: Int) = flow {
        emit(Result.Loading)
        try {
            val caseRecordResponse = apiService.getCaseById(caseId)
            emit(Result.Success(caseRecordResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun addCaseRecord(caseRecordRequest: CaseRecordRequest) = flow {
        emit(Result.Loading)
        try {
            val registerResponse = apiService.addCaseRecord(caseRecordRequest)


            emit(Result.Success(registerResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    // Later we'll fetch the slides from the device
    fun addCaseRecordWithDummySlides(caseRecordRequest: CaseRecordRequest) = flow {
        emit(Result.Loading)
        try {
            val caseRecordResponse = apiService.addCaseRecord(caseRecordRequest)

            caseRecordResponse.id?.let { caseId ->
                val dateFormat = SimpleDateFormat("MM-dd-yyyy hh:mma", Locale.getDefault())

                val slideRequest = SlideRequest().apply {
                    caseRecordId = caseId
                    mainImage = getFileFromDrawable(context, R.drawable.asset_image_annotate_4)
                    qrCode = "QR1121231"
                    microscopicDc = "Test microscopic diagnosis and test"
                    specimenType = "Test Speciment"
                    collectionSite = "Siloam Hospital"
                    reportId = "REP-1000"
                    clinicalData = "Test Clinical Data"
                    dateAndTime = dateFormat.format(Date())
                }

                Log.e("FTEST", "addCaseRecordWithDummySlides: ${slideRequest.toString()}")

                // Collect slide upload result and ensure it's successful
                addSlideItem(slideRequest).collect { result ->
                    if (result is Result.Success) {
                        caseRecordResponse.slides.add(result.data)
                        emit(Result.Success(caseRecordResponse)) // only emit success if slide added
                    } else if (result is Result.Error) {
                        Log.e("FTEST", "Slide upload failed: ${result.error}" )
                        emit(Result.Error("Slide upload failed: ${result.error}"))
                    }
                }
            } ?: run {
                emit(Result.Error("Case record ID is null"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)


    fun addSlideItem(slideRequest: SlideRequest) = flow {
        emit(Result.Loading)
        try {
            val formData = mutableMapOf<String, RequestBody>()
            slideRequest.caseRecordId?.let { formData["caseRecordId"] = it.toString().toRequestBody() }
            slideRequest.dateAndTime?.let { formData["dateAndTime"] = it.toRequestBody() }
            slideRequest.microscopicDc?.let { formData["microscopicDc"] = it.toRequestBody() }
            slideRequest.specimenType?.let { formData["specimenType"] = it.toRequestBody() }
            slideRequest.clinicalData?.let { formData["clinicalData"] = it.toRequestBody() }
            slideRequest.collectionSite?.let { formData["collectionSite"] = it.toRequestBody() }
            slideRequest.reportId?.let { formData["reportId"] = it.toRequestBody() }

            val payloadImage = slideRequest.mainImage

            val mainImagePart =
                payloadImage?.asRequestBody("image/*".toMediaTypeOrNull())?.let {
                    MultipartBody.Part.createFormData(
                        "mainImage",
                        payloadImage.name,
                        it
                    )
                }

            val slideResponse = apiService.addSlideItem(formData, mainImagePart)
            emit(Result.Success(slideResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

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

    fun getAnnotationsBySlidesId(slideId: Long) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getAnnotationsBySlidesId(slideId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getSlidesWithAnnotations(slideId: Long) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getSlidesWithAnnotations(slideId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadAnnotationBatch(
        slideId: Long,
        imagesBase64List: List<String>,
        labels: List<String>
    ): Flow<Result<List<BatchAnnotationResponse>>> = flow {
        emit(Result.Loading)
        try {
            val slideIdPart = slideId.toString().toRequestBody()

            val imageParts = imagesBase64List.mapIndexed { index, base64 ->
                val bitmap = Base64Helper.convertToBitmap(base64)
                val file = bitmapToFile(bitmap, "image_$index.jpg")
                MultipartBody.Part.createFormData(
                    "annotationData[0].annotatedImage[]", file.name, file
                        .asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            val labelParts = labels.map { label ->
                MultipartBody.Part.createFormData("annotationData[0].label[]", label)
            }

            val response = apiService.uploadAnnotationsBatch(slideIdPart, imageParts, labelParts)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            emit(Result.Error("Server error: ${e.message}"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateSlide(id: Long, payload: PostTestReviewPayload): Flow<Result<SlidesItem>> = flow {
        emit(Result.Loading)

        try {
            val formData = mutableMapOf<String, RequestBody>()

            payload.caseRecordId?.let { formData["caseRecordId"] = it.toString().toRequestBody() }
            payload.microscopicDc?.let { formData["microscopicDc"] = it.toRequestBody() }
            payload.diagnosis?.let { formData["diagnosis"] = it.toRequestBody() }

            Log.e("FTEST", "updateSlide: ${payload.caseRecordId}")

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


    fun getAllCaseRecordByCompletedStatus() = flow {
        emit(Result.Loading)
        try {
            val caseRecordResponse = apiService.getAllCaseRecordByCompletedStatus()
            emit(Result.Success(caseRecordResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun archiveOrUnarchiveCaseRecord(caseRecordId: Long, isArchive: Boolean) = flow {
        emit(Result.Loading)
        try {
            val response = apiService.archiveOrUnarchivedCaseRecord(caseRecordId, isArchive)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getArchivedCaseRecords() = flow {
        emit(Result.Loading)
        try {
            val caseRecordResponse = apiService.getArchivedCaseRecords()
            emit(Result.Success(caseRecordResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)


    fun getSlideByCaseId(caseId: Long) = flow {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getSlideByCaseId(caseId)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
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

    suspend fun getDoctorId() = authDataStore.getDoctorId()

    // DUMMY SLIDES FOR CREATE NEW TEST PURPOSE!!!
    suspend fun generateDummySlidesToDatabaseForMVPPurpose() {
        insertSlides(slidesList)
    }

    private fun bitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File.createTempFile(filename, null)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    private fun getFileFromDrawable(context: Context, drawableId: Int): File {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val file = File(context.cacheDir, "temp_image.jpg")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }
        return file
    }
}