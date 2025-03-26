package com.example.thunderscope_frontend.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.createnewtest.LoadingPrepareTestActivity
import com.example.thunderscope_frontend.ui.patient.PatientActivity
import com.example.thunderscope_frontend.viewmodel.PatientRecordUI
import com.example.thunderscope_frontend.viewmodel.PatientRecordViewModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*

class EditPatientDialogFragment : DialogFragment() {

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
    private lateinit var closeButton: ImageView

    // Patient Record View Model
    private lateinit var patientRecordViewModel: PatientRecordViewModel

    private var selectedImageUri: Uri? = null
    private var patientId: String? = null
    private val client = OkHttpClient()

    companion object {
        private const val ARG_PATIENT_ID = "patientId"

        fun newInstance(patientId: String): EditPatientDialogFragment {
            val fragment = EditPatientDialogFragment()
            val args = Bundle()
            args.putString(ARG_PATIENT_ID, patientId)
            fragment.arguments = args
            return fragment
        }
    }

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
    ): View {
        return inflater.inflate(R.layout.edit_patient_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve patientId from arguments
        patientId = arguments?.getString(ARG_PATIENT_ID)

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
        closeButton = view.findViewById(R.id.btnClose)

        // Retrieve patientId from arguments
        patientId = arguments?.getString(ARG_PATIENT_ID)

        // Initialize ViewModel
        patientRecordViewModel =
            ViewModelProvider(requireActivity()).get(PatientRecordViewModel::class.java)

        // Fetch records to populate LiveData
        patientRecordViewModel.fetchCaseRecords()

        // Observe LiveData correctly
        patientRecordViewModel.patientRecordsLiveData.observe(viewLifecycleOwner) { records ->
            val patient = records.find { it.patientId == patientId?.toIntOrNull() }

            if (patient != null) {
                populateFields(patient)
            } else {
                Toast.makeText(requireContext(), "Patient not found", Toast.LENGTH_SHORT).show()
            }
        }

        // On click listener for close button
        closeButton.setOnClickListener {
            val intent = Intent(requireContext(), PatientActivity::class.java)
            startActivity(intent)
            dismiss() // Close the dialog
        }

        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val genderAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // State Spinner Setup
        val stateOptions = mutableListOf("Select State").apply {
            addAll(resources.getStringArray(R.array.state_options))
        }
        val stateAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, stateOptions)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = stateAdapter

        // Event Listeners
        editProfile.setOnClickListener { openImagePicker() }
        birthDate.setOnClickListener { showDatePicker() }
        btnSubmit.setOnClickListener { updatePatientData() }
    }


    // Populate UI Fields
    private fun populateFields(patient: PatientRecordUI) {
        editPatientName.setText(patient.patientName)
        email.setText(patient.patientEmail)
        phoneNumber.setText(patient.patientPhoneNumber)
        birthDate.setText(patient.patientBirthDate)
        age.setText(patient.patientAge.toString())
        address.setText(patient.patientAddress)

        // Set Gender Spinner
        val genderIndex =
            resources.getStringArray(R.array.gender_options).indexOf(patient.patientGender)
        if (genderIndex >= 0) spinnerGender.setSelection(genderIndex)

        // Set State Spinner
        val stateIndex =
            resources.getStringArray(R.array.state_options).indexOf(patient.patientState)
        if (stateIndex >= 0) spinnerState.setSelection(stateIndex + 1) // +1 to skip "Select State"

        // Load Image with Glide
        Glide.with(this)
            .load(patient.patientImage)
            .apply(RequestOptions.circleCropTransform())
            .into(imgProfile)
    }

    // Open Image Picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

    // Show Date Picker
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, year, month, day ->
                val formattedDate = String.format("%02d-%02d-%04d", month + 1, day, year)
                birthDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Get InputStream from selectedImageUri
    private fun getImageRequestBody(uri: Uri): RequestBody? {
        return requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
        }
    }

    // Update Patient Data
    private fun updatePatientData() {
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
            .url("http://10.0.2.2:8080/api/patients/$patientId")
            .put(requestBody)
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
                        Toast.makeText(requireContext(), "Patient updated successfully!", Toast.LENGTH_LONG).show()
                        val handler = android.os.Handler()
                        handler.postDelayed({
                        }, 30000)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(1800, 1300) // Force dialog to match XML size
    }


}
