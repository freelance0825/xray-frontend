package com.example.thunderscope_frontend.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.Patient
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestViewModel
import com.example.thunderscope_frontend.ui.createnewtest.LoadingPrepareTestActivity
import com.example.thunderscope_frontend.ui.slidesdetail.SlidesDetailActivity
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Calendar
import java.util.Locale


class CreatePatientInfoActivityFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var editProfile: ImageView
    private lateinit var editPatientName: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var email: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var birthDate: EditText
    private lateinit var age: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var address: EditText
    private lateinit var btnSubmit: Button
    private var selectedImageUri: Uri? = null

    private val client = OkHttpClient()

    private var viewModel: CreateNewTestViewModel? = null

    // Image Picker Launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data // Update class-level variable

                if (selectedImageUri != null) {
                    Glide.with(this)
                        .load(selectedImageUri)
                        .apply(RequestOptions.circleCropTransform()) // Circular image
                        .into(imgProfile)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_patient_info_activity_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (requireActivity() as CreateNewTestActivity).viewModel

        // Initialize UI components
        imgProfile = view.findViewById(R.id.imgProfile)
        editProfile = view.findViewById(R.id.btnEditProfile)
        editPatientName = view.findViewById(R.id.etPatientName)
        spinnerGender = view.findViewById(R.id.spinnerGender)
        email = view.findViewById(R.id.etEmail)
        phoneNumber = view.findViewById(R.id.etPhoneNumber)
        birthDate = view.findViewById(R.id.etBirthDate)
        age = view.findViewById(R.id.etAge)
        spinnerState = view.findViewById(R.id.spinnerState)
        address = view.findViewById(R.id.etAddress)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val genderAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter
        spinnerGender.setSelection(0)

        // State Spinner Setup
        val stateOptions = mutableListOf("Select State").apply {
            addAll(resources.getStringArray(R.array.state_options))
        }
        val stateAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, stateOptions)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = stateAdapter
        spinnerState.setSelection(0, false)

        // Event Listeners
        editProfile.setOnClickListener { openImagePicker() }
        birthDate.setOnClickListener { showDatePicker() }
        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                sendPatientData()
            }
        }
    }

    // Open Image Picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

    // Date Picker
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    String.format("%02d-%02d-%04d", selectedMonth + 1, selectedDay, selectedYear)
                birthDate.setText(selectedDate)
            }, year, month, day
        )

        datePickerDialog.show()
    }

    // Validate Input Fields
    private fun validateInputs(): Boolean {
        if (editPatientName.text.isBlank()) {
            editPatientName.error = "Name is required"
            return false
        }
        if (email.text.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.text.toString())
                .matches()
        ) {
            email.error = "Enter a valid email"
            return false
        }
        if (phoneNumber.text.isBlank() || !phoneNumber.text.toString()
                .matches(Regex("^[0-9]{10,15}$"))
        ) {
            phoneNumber.error = "Phone Number is required"
            return false
        }
        if (birthDate.text.isBlank() || !birthDate.text.toString()
                .matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))
        ) {
            birthDate.error = "Enter birthdate in MM-DD-YYYY format"
            return false
        }
        if (age.text.isBlank()) {
            age.error = "Age is required"
            return false
        }
        if (spinnerState.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Please select a state", Toast.LENGTH_SHORT).show()
            return false
        }
        if (address.text.isBlank()) {
            address.error = "Address is required"
            return false
        }
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Get InputStream from selectedImageUri
    private fun getImageRequestBody(uri: Uri): RequestBody? {
        return requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
        }
    }


    private fun sendPatientData() {
        val sharedPreferences =
            requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "") ?: ""

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRequestBody = getImageRequestBody(selectedImageUri!!)
        if (imageRequestBody == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val patient = Patient()

        patient.id = 1990
        patient.age = age.text.toString()
        patient.name = editPatientName.text.toString()
        patient.gender = spinnerGender.selectedItem.toString()
        patient.dateOfBirth = birthDate.text.toString()

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("name", editPatientName.text.toString())
            .addFormDataPart("address", address.text.toString())
            .addFormDataPart("gender", spinnerGender.selectedItem.toString())
            .addFormDataPart("email", email.text.toString())
            .addFormDataPart("state", spinnerState.selectedItem.toString())
            .addFormDataPart("age", age.text.toString())
            .addFormDataPart("dob", birthDate.text.toString())
            .addFormDataPart("phoneNumber", phoneNumber.text.toString())
            .addFormDataPart(
                "image",
                "profile.jpg",
                imageRequestBody
            ) // Send image as "profile.jpg"
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/patients/add")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Failed to send data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(), "Patient record is added successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel?.generateDummySlidesToDatabaseForMVPPurpose()

                        // Delay the navigation slightly for better UX
                        Handler(Looper.getMainLooper()).postDelayed({
                            requireActivity().finish()
                            val iDetail =
                                Intent(requireActivity(), SlidesDetailActivity::class.java)
                            iDetail.putExtra(SlidesDetailActivity.EXTRA_PATIENT, patient)
                            startActivity(iDetail)
                        }, 100L) // 1 second delay

                    } else {
                        Log.e("FTEST", "onResponse: ${response.message}", )
                        Log.e("FTEST", "onResponse: ${response}", )
                        Toast.makeText(
                            requireContext(),
                            String.format(Locale.getDefault(), "Error: %s", response.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}