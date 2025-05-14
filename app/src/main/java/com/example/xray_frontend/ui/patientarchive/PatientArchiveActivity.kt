package com.example.xray_frontend.ui.patientarchive

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xray_frontend.R
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivityPatientArchiveBinding
import com.example.xray_frontend.ui.login.LoginActivity
import com.example.xray_frontend.ui.patientarchive.adapters.PatientArchiveAdapter

class PatientArchiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientArchiveBinding

    private val patientArchiveViewModel: PatientArchiveViewModel by viewModels {
        PatientArchiveViewModel.Factory(ThunderscopeRepository(this))
    }

    private val patientArchiveAdapter = PatientArchiveAdapter(
        onArchive = { record, _ ->
            val recordId = record.id?.toLong() ?: return@PatientArchiveAdapter
            patientArchiveViewModel.unarchiveCaseRecord(recordId)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setViews()
        setListeners()
    }

    private fun observeViewModel() {
        patientArchiveViewModel.caseRecordsLiveData.observe(this) { records ->
            patientArchiveAdapter.submitList(records)
        }

        patientArchiveViewModel.errorLiveData.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        if (patientArchiveViewModel.caseRecordsLiveData.value.isNullOrEmpty()) {
            patientArchiveViewModel.fetchCaseRecordArchivedReports()
        }

        patientArchiveViewModel.pageInfoLiveData.observe(this) { (start, end) ->
            val total = patientArchiveViewModel.totalRecords
            binding.textPagination.text = "Showing $start–$end of $total"
        }

        patientArchiveViewModel.caseRecordsLiveData.observe(this) {
            val totalCount = patientArchiveViewModel.totalRecords
            binding.allArchiveCount.text = "($totalCount)"
        }
    }

    private fun setViews() {
        binding.rvCaseRecordsReport.apply {
            adapter = patientArchiveAdapter
            layoutManager = LinearLayoutManager(this@PatientArchiveActivity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition + 3 >= totalItemCount) {
                        patientArchiveViewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.settingsIcon.setOnClickListener {
            val popupMenu = PopupMenu(this@PatientArchiveActivity, binding.settingsIcon)
            popupMenu.menuInflater.inflate(R.menu.settings_dropdown_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.menu_logout) {
                    patientArchiveViewModel.logout()
                    finishAffinity()
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                } else {
                    false
                }
            }
            popupMenu.show()
        }
    }
}
