package com.example.thunderscope_frontend.ui.patientreport

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.repo.ThunderscopeRepository
import com.example.thunderscope_frontend.databinding.ActivityPatientReportBinding
import com.example.thunderscope_frontend.ui.login.LoginActivity
import com.example.thunderscope_frontend.ui.patientreport.adapters.PatientReportAdapter

class PatientReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientReportBinding
    private val patientReportAdapter = PatientReportAdapter { record, archive ->
        // Handle archiving or unarchiving the case record
        val recordId = record.id?.toLong() ?: return@PatientReportAdapter  // Convert Int? to Long, or exit if null
        if (archive) {
            // Archive the item
            patientReportViewModel.archiveOrUnarchiveCaseRecord(recordId, true)
        } else {
            // Unarchive the item
            patientReportViewModel.archiveOrUnarchiveCaseRecord(recordId, false)
        }
    }


    private val patientReportViewModel: PatientReportViewModel by viewModels {
        PatientReportViewModel.Factory(ThunderscopeRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViews()
        setListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        patientReportViewModel.caseRecordsLiveData.observe(this@PatientReportActivity) { records ->
            patientReportAdapter.submitList(records.toList())
        }

        patientReportViewModel.errorLiveData.observe(this@PatientReportActivity) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        if (patientReportViewModel.caseRecordsLiveData.value.isNullOrEmpty()) {
            patientReportViewModel.fetchCaseRecordReports()
        }

        // Observe updated range info
        patientReportViewModel.pageInfoLiveData.observe(this@PatientReportActivity) { (start, end) ->
            val total = patientReportViewModel.totalRecords
            binding.textPagination.text = "Showing $start–$end of $total"
        }

        // Bind count after records are fetched
        patientReportViewModel.caseRecordsLiveData.observe(this) {
            val totalCount = patientReportViewModel.totalRecords
            binding.allReportsCount.text = "($totalCount)"
        }

    }

    private fun setViews() {
        binding.rvCaseRecordsReport.apply {
            adapter = patientReportAdapter
            layoutManager = LinearLayoutManager(this@PatientReportActivity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition + 3 >= totalItemCount) {
                        patientReportViewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.settingsIcon.setOnClickListener {
            val popupMenu = PopupMenu(this@PatientReportActivity, binding.settingsIcon)
            popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.menu_logout) {
                    patientReportViewModel.logout()
                    finishAffinity()
                    startActivity(Intent(this@PatientReportActivity, LoginActivity::class.java))
                    true
                } else false
            }
            popupMenu.show()
        }
    }

}
