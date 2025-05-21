package com.example.xray_frontend.ui.casedashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xray_frontend.R
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivityCaseDashboardBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.casedashboard.adapters.CaseAdapter
import com.example.xray_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.xray_frontend.ui.crmpatient.patientarchive.PatientArchiveActivity
import com.example.xray_frontend.ui.crmpatient.patientdashboard.PatientDashboardActivity
import com.example.xray_frontend.ui.crmpatient.patientreport.PatientReportActivity
import com.example.xray_frontend.ui.settings.SettingsActivity
import com.example.xray_frontend.ui.slides.SlidesActivity
import com.example.xray_frontend.ui.todolistdashboard.TodoListDashboardActivity
import com.example.xray_frontend.ui.utils.enums.CaseRecordStatus

class CaseDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityCaseDashboardBinding

    private val caseAdapter: CaseAdapter = CaseAdapter()

    val caseDashboardViewModel: CaseDashboardViewModel by viewModels {
        CaseDashboardViewModel.Factory(ThunderscopeRepository(this))
    }

    private val activityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            if (activityResult.resultCode == RESULT_OK) {
//                refreshCases()
            }
        }

    private var isCrmExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaseDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        setViews()
        setListeners()
    }

    private fun observeViewModel() {
        caseDashboardViewModel.apply {
            selectedPatientId.observe(this@CaseDashboardActivity) { patientId ->
                fetchCaseRecordsFilterId(patientId, selectedDoctorId.value)
            }

            selectedDoctorId.observe(this@CaseDashboardActivity) { doctorId ->
                fetchCaseRecordsFilterId(selectedPatientId.value, doctorId)
            }

            caseRecordsLiveData.observe(this@CaseDashboardActivity) { caseList ->
                val totalCases = caseList.size
                val forReviewCount =
                    caseList.count { it.status == CaseRecordStatus.FOR_REVIEW.name }
                val inProgressCount =
                    caseList.count { it.status == CaseRecordStatus.IN_PROGRESS.name }
                val completedCount = caseList.count { it.status == CaseRecordStatus.COMPLETED.name }

                binding.apply {
                    allCasesCount.text = StringBuilder("($totalCases)")
                    menuAllCasesCount.text = totalCases.toString()
                    menuForReviewCount.text = forReviewCount.toString()
                    menuInProgressCount.text = inProgressCount.toString()
                    menuCompletedCount.text = completedCount.toString()
                }

                applyFilters(
                    binding.spinnerStatus.selectedItem.toString(),
                    binding.spinnerTimePeriod.selectedItem.toString(),
                    binding.spinnerType.selectedItem.toString(),
                    binding.spinnerGender.selectedItem.toString(),
                    binding.spinnerAge.selectedItem.toString()
                )
            }

            filteredRecordsLiveData.observe(this@CaseDashboardActivity) { filteredRecords ->
                caseAdapter.submitList(filteredRecords)
                binding.textPagination.text = "Showing ${startIndex + 1} - ${endIndex} of ${totalRecords}"
            }

            errorLiveData.observe(this@CaseDashboardActivity) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(this@CaseDashboardActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setViews() {
        binding.apply {
            rvCase.apply {
                adapter = caseAdapter
                layoutManager = LinearLayoutManager(this@CaseDashboardActivity)
            }

            setupFilterSpinners()
            setupFilterListeners()
        }
    }

    private fun setListeners() {
        binding.apply {
            setupPaginationButtons()

            caseAdapter.onItemClick = { caseRecord ->
                val intent = Intent(this@CaseDashboardActivity, SlidesActivity::class.java)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD_ID, caseRecord.id)
                activityLauncher.launch(intent)
            }

            startNewTestButton.setOnClickListener {
                val iNewTest = Intent(this@CaseDashboardActivity, CreateNewTestActivity::class.java)
                iNewTest.putExtra(
                    CreateNewTestActivity.EXTRA_DOCTOR_ID,
                    caseDashboardViewModel.doctorId.toLong()
                )
                activityLauncher.launch(iNewTest)
            }

            menuTodoList.setOnClickListener {
                activityLauncher.launch(
                    Intent(
                        this@CaseDashboardActivity,
                        TodoListDashboardActivity::class.java
                    )
                )
            }

            menuCrmModule.setOnClickListener {
                // Toggle visibility of submenu
                isCrmExpanded = !isCrmExpanded
                crmSubmenu.visibility = if (isCrmExpanded) View.VISIBLE else View.GONE

                // Rotate the arrow based on expansion state
                val rotationAngle = if (isCrmExpanded) 180f else 0f
                crmDropdownArrow.animate().rotation(rotationAngle).setDuration(200).start()
            }

            // Sub-features
            menuPatient.setOnClickListener {
                activityLauncher.launch(
                    Intent(
                        this@CaseDashboardActivity,
                        PatientDashboardActivity::class.java
                    )
                )
            }

            menuReport.setOnClickListener {
                activityLauncher.launch(Intent(this@CaseDashboardActivity, PatientReportActivity::class.java))
            }

            menuArchive.setOnClickListener {
                activityLauncher.launch(
                    Intent(
                        this@CaseDashboardActivity,
                        PatientArchiveActivity::class.java
                    )
                )
            }

            // Settings menu
            settingsIcon.setOnClickListener {
                activityLauncher.launch(Intent(this@CaseDashboardActivity, SettingsActivity::class.java))
            }
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
                caseDashboardViewModel.applyFilters(
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
                if (caseDashboardViewModel.searchPatient(keyword.trim().toInt())) {
                    caseDashboardViewModel.selectedPatientId.value = keyword.trim().toInt()
                } else {
                    Toast.makeText(
                        this@CaseDashboardActivity,
                        "Patient with ID:$keyword not found!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    caseDashboardViewModel.selectedPatientId.value = null
                }
                return false
            }
        })

        binding.svDoctor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(keyword: String): Boolean {
                if (caseDashboardViewModel.searchDoctor(keyword.trim().toInt())) {
                    caseDashboardViewModel.selectedDoctorId.value = keyword.trim().toInt()
                } else {
                    Toast.makeText(
                        this@CaseDashboardActivity,
                        "Doctor with ID:$keyword not found!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    caseDashboardViewModel.selectedDoctorId.value = null
                }
                return false
            }
        })
    }

    private fun setupPaginationButtons() {
        binding.btnNextPage.setOnClickListener {
            caseDashboardViewModel.nextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            caseDashboardViewModel.previousPage()
        }
    }

    private fun refreshCases() {
        binding.spinnerGender.setSelection(0)
        binding.spinnerAge.setSelection(0)
        binding.spinnerStatus.setSelection(0)
        binding.spinnerType.setSelection(0)
        binding.spinnerTimePeriod.setSelection(0)
        binding.svPatient.setQuery("", false)
        binding.svDoctor.setQuery("", false)
        binding.svPatient.clearFocus()
        binding.svDoctor.clearFocus()

        caseDashboardViewModel.fetchCaseRecords()
    }

    override fun onResume() {
        super.onResume()
        refreshCases()
    }
}
