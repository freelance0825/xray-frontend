package com.example.thunderscope_frontend.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class SlidesRecordViewModel(application: Application) : AndroidViewModel(application) {

    val slidesRecordsLiveData = MutableLiveData<MutableList<SlidesRecordUI>>()
    val errorLiveData = MutableLiveData<String>()

    private val client = OkHttpClient()

    fun fetchSlidesRecords(caseRecordId: Int) {
        Log.d("SlidesDebug", "Fetching slides for caseRecordId: $caseRecordId")

        val sharedPreferences = getApplication<Application>()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("token", null)

        if (authToken.isNullOrEmpty()) {
            postError("Authentication token is missing. Please log in again.")
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/slides/case/$caseRecordId")
            .addHeader("Authorization", "Bearer $authToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                postError("Network error: ${e.message}")
                Log.e("SlidesDebug", "Network request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        postError("Error: ${res.code} - ${res.message}")
                        return
                    }

                    val body = res.body?.string()
                    if (body.isNullOrEmpty()) {
                        postError("Received empty response from server.")
                        return
                    }

                    val newSlides = mapApiResponseToUI(body)

                    viewModelScope.launch(Dispatchers.Main) {
                        val currentList = slidesRecordsLiveData.value ?: mutableListOf()
                        currentList.addAll(newSlides)
                        slidesRecordsLiveData.value = currentList
                        Log.d("SlidesDebug", "LiveData updated successfully with ${newSlides.size} new records.")
                    }
                }
            }
        })
    }

    private fun mapApiResponseToUI(response: String): List<SlidesRecordUI> {
        val slidesRecordsList = mutableListOf<SlidesRecordUI>()
        try {
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val mainImage = jsonObject.optString("main_image", "N/A")
                val caseRecordId = jsonObject.optJSONObject("case_record")?.optInt("id", 0) ?: 0

                slidesRecordsList.add(SlidesRecordUI(caseRecordId = caseRecordId, mainImage = mainImage))
            }
        } catch (e: Exception) {
            postError("Error parsing API response: ${e.message}")
        }
        return slidesRecordsList
    }

    private fun postError(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            errorLiveData.value = message
        }
    }
}
