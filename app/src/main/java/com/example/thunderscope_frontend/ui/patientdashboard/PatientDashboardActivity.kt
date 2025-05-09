package com.example.thunderscope_frontend.ui.patientdashboard

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
import com.example.thunderscope_frontend.databinding.ActivityPatientDashboardBinding
import com.example.thunderscope_frontend.ui.fragments.EditPatientDialogFragment
import com.example.thunderscope_frontend.ui.login.LoginActivity
import com.example.thunderscope_frontend.ui.patientdashboard.adapters.PatientAdapter
import com.example.thunderscope_frontend.ui.patientdashboard.screens.EditPatientFragment
import com.example.thunderscope_frontend.ui.utils.PatientStatus

class PatientDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientDashboardBinding

    private val patientAdapter = PatientAdapter()

    val patientDashboardViewModel: PatientDashboardViewModel by viewModels {
        PatientDashboardViewModel.Factory(ThunderscopeRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        setViews()
        setListeners()
    }

    private fun observeViewModel() {
        patientDashboardViewModel.apply {
            patientRecordsLiveData.observe(this@PatientDashboardActivity) { patientList ->
                val totalPatients = patientList.size

                val notStartedCount = patientList.count { it.status == PatientStatus.NOT_STARTED.name }
                val forReviewCount = patientList.count { it.status == PatientStatus.FOR_REVIEW.name }
                val inProgressCount = patientList.count { it.status == PatientStatus.IN_PROGRESS.name }
                val completedCount = patientList.count { it.status == PatientStatus.COMPLETED.name }

                // Update UI
                binding.allPatientNumber.text = StringBuilder("($totalPatients)")
                binding.menuAllPatientCount.text = totalPatients.toString()
                binding.menuNotStartedCount.text = notStartedCount.toString()
                binding.menuForReviewCount.text = forReviewCount.toString()
                binding.menuInProgressCount.text = inProgressCount.toString()
                binding.menuCompletedCount.text = completedCount.toString()

                applyFilters(
                    binding.spinnerStatus.selectedItem.toString(),
                    binding.spinnerTimePeriod.selectedItem.toString(),
                    binding.spinnerType.selectedItem.toString(),
                    binding.spinnerGender.selectedItem.toString(),
                    binding.spinnerAge.selectedItem.toString()
                )
            }

            filteredRecordsLiveData.observe(this@PatientDashboardActivity) { filteredRecords ->
                patientAdapter.submitList(filteredRecords)
                binding.textPagination.text =
                    StringBuilder("Showing ${startIndex + 1} - ${endIndex} of ${totalRecords}")
            }

            errorMessage.observe(this@PatientDashboardActivity) { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(this@PatientDashboardActivity, errorMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun setViews() {
        binding.apply {
            rvPatient.apply {
                adapter = patientAdapter
                layoutManager = LinearLayoutManager(this@PatientDashboardActivity)
            }

            setupFilterSpinners()
            setupFilterListeners()
        }
    }

    private fun setListeners() {
        binding.apply {
            backButton.setOnClickListener { finish() }

            patientAdapter.onEditClick = { patientRecord ->
                val dialog = EditPatientFragment.newInstance(patientRecord.id ?: -1L)
                dialog.show(supportFragmentManager, "EditPatientDialog")
            }

            patientAdapter.onDeleteClick = { patientRecord ->
                val builder = android.app.AlertDialog.Builder(this@PatientDashboardActivity)
                builder.setTitle("Delete Patient")
                builder.setMessage("Are you sure you want to delete this Patient Record? This action cannot be undone.")

                builder.setPositiveButton("Delete") { _, _ ->
                    patientDashboardViewModel.deletePatient(patientRecord.id?.toInt() ?: -1)
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.create().show()
            }

            setupPaginationButtons()

            settingsIcon.setOnClickListener {
                val popupMenu =
                    PopupMenu(
                        this@PatientDashboardActivity,
                        settingsIcon
                    ) // or getContext() if inside Fragment
                popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    val id = item.itemId
                    if (id == R.id.menu_logout) {
                        // Handle Logout
                        patientDashboardViewModel.logout()
                        finishAffinity()
                        startActivity(
                            Intent(
                                this@PatientDashboardActivity,
                                LoginActivity::class.java
                            )
                        )

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
        setupSpinner(binding.spinnerStatus, R.array.patient_status_options)
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
                patientDashboardViewModel.applyFilters(
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
    }

    private fun setupPaginationButtons() {
        binding.btnNextPage.setOnClickListener {
            patientDashboardViewModel.nextPage()
        }

        binding.btnPrevPage.setOnClickListener {
            patientDashboardViewModel.previousPage()
        }
    }
}
