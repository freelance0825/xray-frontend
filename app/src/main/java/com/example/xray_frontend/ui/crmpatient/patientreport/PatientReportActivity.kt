package com.example.xray_frontend.ui.crmpatient.patientreport

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xray_frontend.R
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivityPatientReportBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.crmpatient.patientreport.adapters.PatientReportAdapter
import com.example.xray_frontend.ui.settings.SettingsActivity

class PatientReportActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientReportBinding

    private val patientReportAdapter = PatientReportAdapter { record, archive ->
        val recordId = record.id?.toLong() ?: return@PatientReportAdapter
        patientReportViewModel.archiveOrUnarchiveCaseRecord(recordId, archive)
    }

    private val patientReportViewModel: PatientReportViewModel by viewModels {
        PatientReportViewModel.Factory(ThunderscopeRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()

        setViews()
        setListeners()
    }

    private fun observeViewModel() {
        patientReportViewModel.apply {
            selectedPatientId.observe(this@PatientReportActivity) { patientId ->
                fetchCaseRecordsFilterId(patientId, selectedDoctorId.value)
            }

            selectedDoctorId.observe(this@PatientReportActivity) { doctorId ->
                fetchCaseRecordsFilterId(selectedPatientId.value, doctorId)
            }

            caseRecordsLiveData.observe(this@PatientReportActivity) { caseList ->
                val totalCases = caseList.size

                binding.apply {
                    allReportsCount.text = StringBuilder("($totalCases)")
                }

                applyFilters(
                    binding.spinnerStatus.selectedItem.toString(),
                    binding.spinnerTimePeriod.selectedItem.toString(),
                    binding.spinnerType.selectedItem.toString(),
                    binding.spinnerGender.selectedItem.toString(),
                    binding.spinnerAge.selectedItem.toString()
                )
            }

            filteredRecordsLiveData.observe(this@PatientReportActivity) { filteredRecords ->
                patientReportAdapter.submitList(filteredRecords)
                binding.textPagination.text =
                    StringBuilder("Showing ${startIndex + 1} - ${endIndex} of ${totalRecords}")
            }

            errorLiveData.observe(this@PatientReportActivity) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(this@PatientReportActivity, errorMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun setViews() {
        binding.apply {
            rvCaseRecordsReport.apply {
                adapter = patientReportAdapter
                layoutManager = LinearLayoutManager(this@PatientReportActivity)
            }

            setupFilterSpinners()
            setupFilterListeners()
        }
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.settingsIcon.setOnClickListener {
            startActivity(Intent(this@PatientReportActivity, SettingsActivity::class.java))

//            val popupMenu = PopupMenu(this@PatientReportActivity, binding.settingsIcon)
//            popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)
//            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
//                if (item.itemId == R.id.menu_logout) {
//                    patientReportViewModel.logout()
//                    finishAffinity()
//                    startActivity(Intent(this@PatientReportActivity, LoginActivity::class.java))
//                    true
//                } else false
//            }
//            popupMenu.show()
        }
    }

    private fun setupFilterSpinners() {
        setupSpinner(binding.spinnerTimePeriod, R.array.time_period_options)
        setupSpinner(binding.spinnerStatus, R.array.case_record_status_options)
        setupSpinner(binding.spinnerType, R.array.type_options)
        setupSpinner(binding.spinnerGender, R.array.gender_filter_options)
        setupSpinner(binding.spinnerAge, R.array.age_options)
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_dropdown,
            resources.getStringArray(arrayResId)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupFilterListeners() {
        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                patientReportViewModel.applyFilters(
                    binding.spinnerStatus.selectedItem.toString(),
                    binding.spinnerTimePeriod.selectedItem.toString(),
                    binding.spinnerType.selectedItem.toString(),
                    binding.spinnerGender.selectedItem.toString(),
                    binding.spinnerAge.selectedItem.toString()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerTimePeriod.onItemSelectedListener = filterListener
        binding.spinnerStatus.onItemSelectedListener = filterListener
        binding.spinnerType.onItemSelectedListener = filterListener
        binding.spinnerGender.onItemSelectedListener = filterListener
        binding.spinnerAge.onItemSelectedListener = filterListener

        binding.svPatient.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(keyword: String): Boolean {
                if (patientReportViewModel.searchPatient(keyword.trim().toInt())) {
                    patientReportViewModel.selectedPatientId.value = keyword.trim().toInt()
                } else {
                    Toast.makeText(
                        this@PatientReportActivity,
                        "Patient with ID:$keyword not found!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    patientReportViewModel.selectedPatientId.value = null
                }
                return false
            }
        })

        binding.svDoctor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(keyword: String): Boolean {
                if (patientReportViewModel.searchDoctor(keyword.trim().toInt())) {
                    patientReportViewModel.selectedDoctorId.value = keyword.trim().toInt()
                } else {
                    Toast.makeText(
                        this@PatientReportActivity,
                        "Doctor with ID:$keyword not found!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    patientReportViewModel.selectedDoctorId.value = null
                }
                return false
            }
        })
    }

    private fun setupPaginationButtons() {
        binding.btnNextPage.setOnClickListener {
            patientReportViewModel.nextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            patientReportViewModel.previousPage()
        }
    }

    override fun onDestroy() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        super.onDestroy()
    }
}
