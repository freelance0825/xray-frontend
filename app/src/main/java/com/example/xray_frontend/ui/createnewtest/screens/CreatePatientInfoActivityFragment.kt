package com.example.xray_frontend.ui.createnewtest.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.PatientResponse
import com.example.xray_frontend.data.models.SlidesItem
import com.example.xray_frontend.data.models.UpdatePatientRequest
import com.example.xray_frontend.databinding.CreatePatientInfoActivityFragmentBinding
import com.example.xray_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.xray_frontend.ui.createnewtest.CreateNewTestViewModel
import com.example.xray_frontend.ui.slidesdetail.SlidesDetailActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar


class CreatePatientInfoActivityFragment : Fragment() {

    // Declare the ViewBinding object
    private var _binding: CreatePatientInfoActivityFragmentBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var viewModel: CreateNewTestViewModel? = null

    // Image Picker Launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    val file = uriToFile(it, requireContext())
                    viewModel?.selectedPatientImage?.value = file

                    Glide.with(this)
                        .load(it)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.imgProfile)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreatePatientInfoActivityFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // POLICY WORKAROUND - Refactor Later with MVVM Architecture
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        viewModel = (requireActivity() as CreateNewTestActivity).viewModel

        viewModel?.apply {
            isCreatingNewPatient.observe(viewLifecycleOwner) {
                if (it != null) {
                    enableFields(it)
                }
            }

            caseRecordResponse.observe(viewLifecycleOwner) {
                it?.let { caseRecord ->
                    Toast.makeText(
                        requireContext(),
                        "Patient record is successfully added",
                        Toast.LENGTH_SHORT
                    ).show()

                    val activeSlidesList = arrayListOf<SlidesItem>()
                    activeSlidesList.addAll(caseRecord.slides.map { slideItem -> SlidesItem(id = slideItem.id) })

                    Handler(Looper.getMainLooper()).postDelayed({
                        requireActivity().finish()
                        val iDetail =
                            Intent(requireActivity(), SlidesDetailActivity::class.java)
                        iDetail.putExtra(SlidesDetailActivity.EXTRA_PATIENT, viewModel?.selectedPatient?.value)
                        iDetail.putExtra(SlidesDetailActivity.EXTRA_CASE_ID, caseRecord.id?.toLong())
                        iDetail.putParcelableArrayListExtra(SlidesDetailActivity.EXTRA_SLIDE_ID_LIST, activeSlidesList)
                        startActivity(iDetail)
                    }, 100L)
                }
            }

            successfullySubmittedPatient.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel?.addCaseRecord()
                }
            }

            selectedPatient.observe(viewLifecycleOwner) { patientResponse ->
                if (patientResponse != null) {
                    populateFields(patientResponse)
                }
            }
        }

        // Handle RadioGroup Option Selection
        binding.rgPatientOption.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_create_patient -> {
                    viewModel?.selectPatient(null)
                    clearFields()
                    viewModel?.isCreatingNewPatient?.value = true
                    binding.layoutPatientSpinner.visibility = View.GONE
                }

                R.id.rb_select_existing_patient -> {
                    clearFields()
                    viewModel?.isCreatingNewPatient?.value = false
                    binding.layoutPatientSpinner.visibility = View.VISIBLE
                }

                else -> {
                    viewModel?.isCreatingNewPatient?.value = false
                }
            }
        }

        binding.svPatient.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(keyword: String): Boolean {
                if (viewModel?.searchPatient(keyword.trim().toInt()) == true) {
                    viewModel?.selectPatient(keyword.trim().toInt())
                } else {
                    Toast.makeText(requireContext(), "Patient with ID:$keyword not found!", Toast.LENGTH_LONG).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel?.selectPatient(null)
                }
                return false
            }
        })

        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val genderAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter
        binding.spinnerGender.setSelection(0)

        // State Spinner Setup
        val stateOptions = mutableListOf("Select State").apply {
            addAll(resources.getStringArray(R.array.state_options))
        }
        val stateAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, stateOptions)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerState.adapter = stateAdapter
        binding.spinnerState.setSelection(0, false)

        // Event Listeners
        binding.btnEditProfile.setOnClickListener { openImagePicker() }
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                if (viewModel?.isCreatingNewPatient?.value == true) {
                    addPatientData()
                } else {
                    viewModel?.successfullySubmittedPatient?.value = true
                }
            }
        }
    }

    private fun addPatientData() {
        val patientRequest = UpdatePatientRequest(
            name = binding.etPatientName.text.toString(),
            email = binding.etEmail.text.toString(),
            phoneNumber = binding.etPhoneNumber.text.toString(),
            dob = binding.etBirthDate.text.toString(),
            age = binding.etAge.text.toString(),
            address = binding.etAddress.text.toString(),
            gender = binding.spinnerGender.selectedItem.toString(),
            state = binding.spinnerState.selectedItem.toString(),
            image = viewModel?.selectedPatientImage?.value
        )

        viewModel?.addPatient(patientRequest)
    }

    // Open Image Picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

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
                binding.etBirthDate.setText(selectedDate)
                binding.etAge.setText((year - selectedYear).toString())
            }, year, month, day
        )

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    // Validate Input Fields
    private fun validateInputs(): Boolean {
        if (binding.etPatientName.text.isBlank()) {
            binding.etPatientName.error = "Name is required"
            return false
        }
        if (binding.etEmail.text.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString())
                .matches()
        ) {
            binding.etEmail.error = "Enter a valid email"
            return false
        }
        if (binding.etPhoneNumber.text.isBlank() || !binding.etPhoneNumber.text.toString()
                .matches(Regex("^[0-9]{10,15}$"))
        ) {
            binding.etPhoneNumber.error = "Phone Number is required"
            return false
        }
        if (binding.etBirthDate.text.isBlank() || !binding.etBirthDate.text.toString()
                .matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))
        ) {
            binding.etBirthDate.error = "Enter birthdate in MM-DD-YYYY format"
            return false
        }
        if (binding.etAge.text.isBlank()) {
            binding.etAge.error = "Age is required"
            return false
        }
        if (binding.spinnerState.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Please select a state", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etAddress.text.isBlank()) {
            binding.etAddress.error = "Address is required"
            return false
        }
        if (viewModel?.selectedPatientImage?.value == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Function to convert URI to File
    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val fileName = getFileName(uri, context)
        val file = File(context.cacheDir, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }

        return file
    }

    private fun getFileName(uri: Uri, context: Context): String {
        var fileName = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex >= 0) {
                    fileName = it.getString(columnIndex)
                }
            }
        }
        if (fileName.isEmpty()) {
            fileName = "image_${System.currentTimeMillis()}.jpg"
        }
        return fileName
    }

    fun bitmapToFile(bitmap: Bitmap, context: Context): File? {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${requireContext().applicationInfo.name}_temp.png"
        )

        try {
            // Create an output stream to write the bitmap data to the file
            val outputStream = FileOutputStream(file)

            // Compress the Bitmap to the output stream (e.g., as PNG or JPEG)
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                outputStream
            ) // You can change the format to JPEG if needed

            // Flush and close the stream
            outputStream.flush()
            outputStream.close()

            return file // Return the File object
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun populateFields(patient: PatientResponse?) {
        binding.apply {
            patient?.let {
                etPatientName.setText(patient.name)
                etEmail.setText(patient.email)
                etPhoneNumber.setText(patient.phoneNumber)
                etBirthDate.setText(patient.dateOfBirth)
                etAge.setText(patient.age.toString())
                etAddress.setText(patient.address)

                // Set Gender Spinner
                val genderIndex =
                    resources.getStringArray(R.array.gender_options).indexOf(patient.gender)
                if (genderIndex >= 0) spinnerGender.setSelection(genderIndex)

                // Set State Spinner
                val stateIndex =
                    resources.getStringArray(R.array.state_options).indexOf(patient.state)
                if (stateIndex >= 0) spinnerState.setSelection(stateIndex + 1) // +1 to skip "Select State"

                // Decode Base64 image string and load into Glide
                if (!patient.imageBase64.isNullOrEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(patient.imageBase64, Base64.DEFAULT)
                        val decodedBitmap =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        val imageFile = bitmapToFile(decodedBitmap, requireContext())
                        viewModel?.selectedPatientImage?.value = imageFile

                        // Load decoded Bitmap into Glide
                        Glide.with(requireActivity())
                            .load(decodedBitmap)
                            .apply(RequestOptions.circleCropTransform()) // Apply circular transformation
                            .into(imgProfile)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        // Set a default image in case of decoding error
                        imgProfile.setImageResource(R.drawable.circle_background)
                    }
                } else {
                    // Set a default profile picture if image data is empty
                    imgProfile.setImageResource(R.drawable.circle_background)
                }
            }
        }
    }

    private fun clearFields() {
        binding.apply {
            // Clear EditText fields
            etPatientName.setText("")
            etEmail.setText("")
            etPhoneNumber.setText("")
            etBirthDate.setText("")
            etAge.setText("")
            etAddress.setText("")

            // Reset Gender Spinner to default (first item)
            spinnerGender.setSelection(0)

            // Reset State Spinner to default (first item)
            spinnerState.setSelection(0) // Assuming the first item is "Select State"

            // Clear the profile image
            imgProfile.setImageResource(R.drawable.ic_default_profile) // Reset to default image
        }
    }

    private fun enableFields(isCreatingNewPatients: Boolean) {
        binding.apply {
            etPatientName.isEnabled = isCreatingNewPatients
            etEmail.isEnabled = isCreatingNewPatients
            etPhoneNumber.isEnabled = isCreatingNewPatients
            etBirthDate.isEnabled = isCreatingNewPatients
            etAge.isEnabled = isCreatingNewPatients
            spinnerGender.isEnabled = isCreatingNewPatients
            spinnerState.isEnabled = isCreatingNewPatients
            etAddress.isEnabled = isCreatingNewPatients
            btnEditProfile.isEnabled = isCreatingNewPatients
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks
        _binding = null
    }
}