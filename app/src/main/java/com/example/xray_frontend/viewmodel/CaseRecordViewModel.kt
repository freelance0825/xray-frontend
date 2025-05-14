package com.example.xray_frontend.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class CaseRecordViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to store case records fetched from the backend
    val caseRecordsLiveData = MutableLiveData<List<CaseRecordUI>>()

    // LiveData to store the count of case records
    val caseCountLiveData = MutableLiveData<Int>()  // Holds the total count of casedashboard

    // LiveData to store any error messages
    val errorLiveData = MutableLiveData<String>()

    // OkHttpClient for making network requests
    private val client = OkHttpClient()

    // Function to fetch case records from the backend
    fun fetchCaseRecords() {
        // Get the authentication token from SharedPreferences
        val sharedPreferences = getApplication<Application>()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("token", null)

        // If token is missing, post an error message
        if (authToken.isNullOrEmpty()) {
            postError("Authentication token is missing. Please log in again.")
            return
        }

        // Create an HTTP request to fetch case records
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/case/list")
            .addHeader("Authorization", "Bearer $authToken")
            .get()
            .build()

        // Execute the network request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure
                postError("Network error: ${e.message}")
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

                    // Convert API response to a list of case records
                    val caseRecordsList = mapApiResponseToUI(body)

                    // Update LiveData on the main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        caseRecordsLiveData.value = caseRecordsList  // Set case records

                        // Update the count of fetched case records
                        caseCountLiveData.value = caseRecordsList.size
                    }
                }
            }
        })
    }

    // Function to map JSON response to a list of CaseRecordUI objects
    private fun mapApiResponseToUI(response: String): List<CaseRecordUI> {
        val caseRecordsList = mutableListOf<CaseRecordUI>()
        val jsonArray = JSONArray(response)

        if (jsonArray.length() == 0) {
            // If there are no records, post an error and return an empty list
            postError("No case records found.")
            return emptyList()
        }

        // Loop through JSON array and extract relevant data
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val doctor = jsonObject.optJSONObject("doctor")
            val patient = jsonObject.optJSONObject("casedashboard")

            // Ensure both doctor and casedashboard objects exist before proceeding
            if (doctor != null && patient != null) {
                val caseRecord = CaseRecordUI(
                    caseRecordId = jsonObject.optInt("id"),
                    physicianName = doctor.optString("name", "N/A"),
                    patientName = patient.optString("name", "N/A"),
                    patientId = patient.optInt("id", -1),  // Ensure ID is not null (-1 as fallback)
                    patientImage = patient.optString("image_base64", "N/A"),
                    patientPhoneNumber = patient.optString("phone_number", "N/A"),
                    patientBirthdate = patient.optString("date_of_birth", "N/A"),
                    patientAge = patient.optString("age", "N/A"),
                    patientGender = patient.optString("gender", "N/A"),
                    lastUpdateDate = jsonObject.optString("date", "N/A"),
                    lastUpdateTime = jsonObject.optString("time", "N/A"),
                    status = jsonObject.optString("status", "N/A"),
                    type = jsonObject.optString("type", "N/A"),
                    todo = jsonObject.optString("todo", "N/A"),
                )
                caseRecordsList.add(caseRecord)
            }
        }
        // Sort the list by time in descending order before returning
        return caseRecordsList.sortedByDescending { it.lastUpdateTime }
    }

    // Function to post error messages to errorLiveData
    private fun postError(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            errorLiveData.value = message
        }
    }
}
