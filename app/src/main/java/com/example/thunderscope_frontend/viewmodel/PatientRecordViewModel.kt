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

class PatientRecordViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to store case records fetched from the backend
    val patientRecordsLiveData = MutableLiveData<List<PatientRecordUI>>()

    // LiveData to store the count of case records
    val patientCountLiveData = MutableLiveData<Int>()  // Holds the total count of casedashboard

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
            .url("http://10.0.2.2:8080/api/patients")
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
                        patientRecordsLiveData.value = caseRecordsList  // Set case records

                        // Update the count of fetched case records
                        patientCountLiveData.value = caseRecordsList.size
                    }
                }
            }
        })
    }

    // Function to map JSON response to a list of CaseRecordUI objects
    private fun mapApiResponseToUI(response: String): List<PatientRecordUI> {
        val patientRecordsList = mutableListOf<PatientRecordUI>()
        val jsonArray = JSONArray(response)

        if (jsonArray.length() == 0) {
            // If there are no records, post an error and return an empty list
            postError("No case records found.")
            return emptyList()
        }

        // Loop through JSON array and extract relevant data
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            // Validation
            if (response.isNotBlank()) {

                val patientRecord = PatientRecordUI(
                    patientId = jsonObject.optInt("id"),
                    patientImage = jsonObject.optString("image_base64", "N/A"),
                    patientName = jsonObject.optString("name", "N/A"),
                    patientBirthDate = jsonObject.optString("date_of_birth", "N/A"),
                    patientGender = jsonObject.optString("gender", "N/A"),
                    patientAge = jsonObject.optString("age", "N/A"),
                    patientPhoneNumber = jsonObject.optString("phone_number", "N/A"),
                    patientEmail = jsonObject.optString("email", "N/A"),
                    patientState = jsonObject.optString("state", "N/A"),
                    patientStatus = jsonObject.optString("status", "N/A"),
                    patientType = jsonObject.optString("type", "N/A"),
                    patientAddress = jsonObject.optString("address", "N/A"),
                    patientLastUpdate = jsonObject.optString("updated_at", "N/A")
                )
                patientRecordsList.add(patientRecord)
            }
        }
        // Sort the list by patientId in descending order before returning
        return patientRecordsList.sortedByDescending { it.patientLastUpdate }
    }


    fun deletePatient(patientId: Int) {
        // Get authentication token from SharedPreferences
        val sharedPreferences = getApplication<Application>()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("token", null)

        if (authToken.isNullOrEmpty()) {
            postError("Authentication token is missing. Please log in again.")
            return
        }

        // Create the DELETE request
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/patients/$patientId") // Adjust the endpoint as needed
            .addHeader("Authorization", "Bearer $authToken")
            .delete()
            .build()

        // Execute the network request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                postError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        postError("Failed to delete casedashboard: ${res.code} - ${res.message}")
                        return
                    }

                    // Ensure patientRecordsLiveData.value is not null before filtering
                    viewModelScope.launch(Dispatchers.Main) {
                        val currentList = patientRecordsLiveData.value ?: emptyList() // Use an empty list if null
                        val updatedList = currentList.filter { it.patientId != patientId }
                        patientRecordsLiveData.value = updatedList
                        patientCountLiveData.value = updatedList.size
                    }
                }
            }
        })
    }


    // Function to post error messages to errorLiveData
    private fun postError(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            errorLiveData.value = message
        }
    }
}