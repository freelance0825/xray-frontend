package com.example.thunderscope_frontend.ui.casedashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.databinding.ActivityCaseDashboardBinding
import com.example.thunderscope_frontend.ui.casedashboard.adapters.CaseAdapter
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.thunderscope_frontend.ui.login.LoginActivity
import com.example.thunderscope_frontend.ui.patientdashboard.PatientDashboardActivity
import com.example.thunderscope_frontend.ui.slides.SlidesActivity
import com.example.thunderscope_frontend.ui.todolistdashboard.TodoListDashboardActivity

class CaseDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaseDashboardBinding

    private val caseAdapter: CaseAdapter = CaseAdapter()

    val caseDashboardViewModel: CaseDashboardViewModel by viewModels {
        CaseDashboardViewModel.Factory(ThunderscopeRepository(this))
    }


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

            patientRecordsLiveData.observe(this@CaseDashboardActivity) { patientList ->
                val patientStringArray = mutableListOf("All Patients")
                patientStringArray.addAll(patientList.map { "${it.id} - ${it.name}" })

                val adapter = ArrayAdapter(
                    this@CaseDashboardActivity,
                    R.layout.custom_spinner_dropdown,
                    patientStringArray
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerPatientID.adapter = adapter
            }

            doctorRecordsLiveData.observe(this@CaseDashboardActivity) { doctorList ->
                val doctorStringArray = mutableListOf("All Doctors")
                doctorStringArray.addAll(doctorList.map { "${it.id} - ${it.name}" })

                val adapter = ArrayAdapter(
                    this@CaseDashboardActivity,
                    R.layout.custom_spinner_dropdown,
                    doctorStringArray
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDoctorID.adapter = adapter
            }

            caseRecordsLiveData.observe(this@CaseDashboardActivity) { caseList ->
                val totalCases = caseList.size
                val highPriorityCount = caseList.count { it.status == "High Priority" }
                val inPreparationsCount = caseList.count { it.status == "In Preparations" }
                val forReviewCount = caseList.count { it.status == "For Review" }
                val completedCount = caseList.count { it.status == "Completed" }

                binding.apply {
                    allCasesNumber.text = StringBuilder("($totalCases)")
                    menuAllCasesCount.text = totalCases.toString()
                    menuHighPriorityCount.text = highPriorityCount.toString()
                    menuInPreparationsCount.text = inPreparationsCount.toString()
                    menuForReviewCount.text = forReviewCount.toString()
                    menuFinishedCount.text = completedCount.toString()
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
                binding.textPagination.text =
                    StringBuilder("Showing ${startIndex + 1} - ${endIndex} of ${totalRecords}")
            }

            errorLiveData.observe(this@CaseDashboardActivity) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(this@CaseDashboardActivity, errorMessage, Toast.LENGTH_LONG)
                        .show()
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
                val recordToBeSent = caseRecord.copy()
                recordToBeSent.slides = mutableListOf()

                val intent = Intent(this@CaseDashboardActivity, SlidesActivity::class.java)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD, recordToBeSent)
                startActivity(intent)
            }

            startNewTestButton.setOnClickListener {
                startActivity(Intent(this@CaseDashboardActivity, CreateNewTestActivity::class.java))
            }

            menuTodoList.setOnClickListener {
                startActivity(Intent(this@CaseDashboardActivity, TodoListDashboardActivity::class.java))
            }

            menuPatientModule.setOnClickListener {
                startActivity(
                    Intent(
                        this@CaseDashboardActivity,
                        PatientDashboardActivity::class.java
                    )
                )
            }

            settingsIcon.setOnClickListener {
                val popupMenu =
                    PopupMenu(
                        this@CaseDashboardActivity,
                        settingsIcon
                    ) // or getContext() if inside Fragment
                popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    val id = item.itemId
                    if (id == R.id.menu_logout) {
                        // Handle Logout
                        caseDashboardViewModel.logout()
                        finishAffinity()
                        startActivity(Intent(this@CaseDashboardActivity, LoginActivity::class.java))

                        return@setOnMenuItemClickListener true
                    }
                    false
                }
                popupMenu.show()
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

        binding.spinnerPatientID.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        caseDashboardViewModel.selectedPatientId.value = null
                    } else {
                        caseDashboardViewModel.selectedPatientId.value = caseDashboardViewModel.patientRecordsLiveData.value?.get(position - 1)?.id?.toInt()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.spinnerDoctorID.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        caseDashboardViewModel.selectedDoctorId.value = null
                    } else {
                        caseDashboardViewModel.selectedDoctorId.value = caseDashboardViewModel.doctorRecordsLiveData.value?.get(position - 1)?.id?.toInt()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupPaginationButtons() {
        binding.btnNextPage.setOnClickListener {
            caseDashboardViewModel.nextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            caseDashboardViewModel.previousPage()
        }
    }
}
