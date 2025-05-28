package com.example.xray_frontend.ui.crmpatient.patientdashboard.screens

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
import android.provider.OpenableColumns
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.PatientResponse
import com.example.xray_frontend.data.models.UpdatePatientRequest
import com.example.xray_frontend.databinding.EditPatientDialogFragmentBinding
import com.example.xray_frontend.ui.crmpatient.patientdashboard.PatientDashboardActivity
import com.example.xray_frontend.ui.crmpatient.patientdashboard.PatientDashboardViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class EditPatientFragment : DialogFragment() {

    private lateinit var binding: EditPatientDialogFragmentBinding

    private val patientId: Long? by lazy {
        arguments?.getLong(ARG_PATIENT_ID, 0L)
    }

    private lateinit var patientViewModel: PatientDashboardViewModel

    private var selectedImageUri: Uri? = null
    private var currentPatient: PatientResponse? = null

    // Image Picker Launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    val file = uriToFile(it, requireContext())
                    patientViewModel.selectedPatientImage.value = file

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
        binding = EditPatientDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        patientViewModel = (requireActivity() as PatientDashboardActivity).patientDashboardViewModel

        setViews()
        observeViewModel()
        setListeners()
    }

    private fun setViews() {
        // Gender Spinner Setup
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val genderAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        // State Spinner Setup
        val stateOptions = mutableListOf("Select State").apply {
            addAll(resources.getStringArray(R.array.state_options))
        }
        val stateAdapter =
            ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown, stateOptions)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerState.adapter = stateAdapter
    }

    private fun observeViewModel() {
        patientViewModel.apply {
            isUpdatePatientSuccessful.observe(viewLifecycleOwner) {
                if (it) {
                    Toast.makeText(requireContext(), "Patient updated successfully!", Toast.LENGTH_LONG).show()
                    this@EditPatientFragment.dismiss()
                }
            }

            patientRecordsLiveData.observe(viewLifecycleOwner) { patient ->
                val selectedPatient = patient.find { it.id == patientId }
                currentPatient = selectedPatient
                populateFields(selectedPatient)
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                // Show or hide loading indicator (e.g., binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE)
            }

            errorMessage.observe(viewLifecycleOwner) { error ->
                if (!error.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnEditProfile.setOnClickListener { openImagePicker() }
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.btnSubmit.setOnClickListener { updatePatientData() }
        binding.btnClose.setOnClickListener { dismiss() }
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
                        val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        val imageFile = bitmapToFile(decodedBitmap, requireContext())
                        patientViewModel.selectedPatientImage.value = imageFile

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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
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

    private fun updatePatientData() {
        val patientRequest = UpdatePatientRequest(
            name = binding.etPatientName.text.toString(),
            email = binding.etEmail.text.toString(),
            phoneNumber = binding.etPhoneNumber.text.toString(),
            dateOfBirth = binding.etBirthDate.text.toString(),
            age = binding.etAge.text.toString(),
            address = binding.etAddress.text.toString(),
            gender = binding.spinnerGender.selectedItem.toString(),
            state = binding.spinnerState.selectedItem.toString(),
            image = patientViewModel.selectedPatientImage.value
        )

        patientId?.let {
            patientViewModel.updatePatient(it.toInt(), patientRequest)
        }
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
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${requireContext().applicationInfo.name}_temp.png")

        try {
            // Create an output stream to write the bitmap data to the file
            val outputStream = FileOutputStream(file)

            // Compress the Bitmap to the output stream (e.g., as PNG or JPEG)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // You can change the format to JPEG if needed

            // Flush and close the stream
            outputStream.flush()
            outputStream.close()

            return file // Return the File object
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            val height = ViewGroup.LayoutParams.WRAP_CONTENT

            window.setLayout(width, height)
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        private const val ARG_PATIENT_ID = "patientId"

        fun newInstance(patientId: Long): EditPatientFragment {
            val fragment = EditPatientFragment()
            val args = Bundle()
            args.putLong(ARG_PATIENT_ID, patientId)
            fragment.arguments = args
            return fragment
        }
    }
}