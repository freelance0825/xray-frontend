package com.example.xray_frontend.ui.todolistdashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xray_frontend.R
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivityTodoListDashboardBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.login.LoginActivity
import com.example.xray_frontend.ui.slides.SlidesActivity
import com.example.xray_frontend.ui.todolistdashboard.adapters.TodoAdapter
import com.example.xray_frontend.ui.utils.enums.CaseRecordStatus

class TodoListDashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityTodoListDashboardBinding

    private val todoAdapter = TodoAdapter()

    val todoListDashboardViewModel: TodoListDashboardViewModel by viewModels {
        TodoListDashboardViewModel.Factory(ThunderscopeRepository(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodoListDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        setViews()
        setListeners()
    }

    private fun observeViewModel() {
        todoListDashboardViewModel.apply {
            selectedPatientId.observe(this@TodoListDashboardActivity) { patientId ->
                fetchCaseRecordsFilterId(patientId, selectedDoctorId.value)
            }

            selectedDoctorId.observe(this@TodoListDashboardActivity) { doctorId ->
                fetchCaseRecordsFilterId(selectedPatientId.value, doctorId)
            }

            caseRecordsLiveData.observe(this@TodoListDashboardActivity) { caseList ->
                val totalCases = caseList.size
                val forReviewCount = caseList.count { it.status == CaseRecordStatus.FOR_REVIEW.name }
                val inProgressCount = caseList.count { it.status == CaseRecordStatus.IN_PROGRESS.name }
                val completedCount = caseList.count { it.status == CaseRecordStatus.COMPLETED.name }

                binding.apply {
                    todoListCount.text = StringBuilder("($totalCases)")
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

            filteredRecordsLiveData.observe(this@TodoListDashboardActivity) { filteredRecords ->
                todoAdapter.submitList(filteredRecords)
                binding.textPagination.text =
                    StringBuilder("Showing ${startIndex + 1} - ${endIndex} of ${totalRecords}")
            }

            errorLiveData.observe(this@TodoListDashboardActivity) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(this@TodoListDashboardActivity, errorMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun setViews() {
        binding.apply {
            rvTodo.apply {
                adapter = todoAdapter
                layoutManager = LinearLayoutManager(this@TodoListDashboardActivity)
            }

            setupFilterSpinners()
            setupFilterListeners()
        }
    }

    private fun setListeners() {
        binding.apply {
            backButton.setOnClickListener { finish() }

            setupPaginationButtons()

            todoAdapter.onItemClick = { caseRecord ->
                val recordToBeSent = caseRecord.copy()
                recordToBeSent.slides = mutableListOf()

                val intent = Intent(this@TodoListDashboardActivity, SlidesActivity::class.java)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD_ID, caseRecord.id)
                intent.putExtra(SlidesActivity.EXTRA_CASE_RECORD, recordToBeSent)
                startActivity(intent)
            }

            settingsIcon.setOnClickListener {
           /*     val popupMenu = PopupMenu(this@TodoListDashboardActivity, settingsIcon) // or getContext() if inside Fragment
                popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    val id = item.itemId
                    if (id == R.id.menu_logout) {
                        // Handle Logout
                        todoListDashboardViewModel.logout()
                        finishAffinity()
                        startActivity(Intent(this@TodoListDashboardActivity, LoginActivity::class.java))

                        return@setOnMenuItemClickListener true
                    }
                    false
                }
                popupMenu.show()*/
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
                todoListDashboardViewModel.applyFilters(
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
                if (todoListDashboardViewModel.searchPatient(keyword.trim().toInt())) {
                    todoListDashboardViewModel.selectedPatientId.value = keyword.trim().toInt()
                } else {
                    Toast.makeText(this@TodoListDashboardActivity, "Patient with ID:$keyword not found!", Toast.LENGTH_LONG).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    todoListDashboardViewModel.selectedPatientId.value = null
                }
                return false
            }
        })
    }

    private fun setupPaginationButtons() {
        binding.btnNextPage.setOnClickListener {
            todoListDashboardViewModel.nextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            todoListDashboardViewModel.previousPage()
        }
    }
}
