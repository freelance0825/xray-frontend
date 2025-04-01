package com.example.thunderscope_frontend.ui.patient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.thunderscope_frontend.viewmodel.CaseRecordViewModel
import com.google.android.material.button.MaterialButton

class PatientListActivity : AppCompatActivity() {

    // UI components
    private lateinit var allCasesCount: TextView
    private lateinit var menuAllCasesCount: TextView
    private lateinit var menuHighPriorityCount: TextView
    private lateinit var menuInPreparations: TextView
    private lateinit var menuForReview: TextView
    private lateinit var menuFinished: TextView
    private lateinit var startNewTest: MaterialButton
    private lateinit var patientModule: TextView

    // Case Record ViewModel (properly initialized)
    private val caseRecordViewModel: CaseRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_list_activity)

        // Initialize UI components
        allCasesCount = findViewById(R.id.all_cases_number)
        menuAllCasesCount = findViewById(R.id.menu_all_cases_count)
        menuHighPriorityCount = findViewById(R.id.menu_high_priority_count)
        menuInPreparations = findViewById(R.id.menu_in_preparations_count)
        menuForReview = findViewById(R.id.menu_for_review_count)
        menuFinished = findViewById(R.id.menu_finished_count)
        startNewTest = findViewById(R.id.start_new_test_button)
        patientModule = findViewById(R.id.etPatient)

        // Observe case records list and update UI
        caseRecordViewModel.caseRecordsLiveData.observe(this, Observer { caseList ->
            val totalCases = caseList.size
            val highPriorityCount = caseList.count { it.status == "High Priority" }
            val inPreparationsCount = caseList.count { it.status == "In Preparations" }
            val forReviewCount = caseList.count { it.status == "For Review" }
            val completedCount = caseList.count { it.status == "Completed" }

            // Update UI
            allCasesCount.text = StringBuilder("($totalCases)")
            menuAllCasesCount.text = totalCases.toString()
            menuHighPriorityCount.text = highPriorityCount.toString()
            menuInPreparations.text = inPreparationsCount.toString()
            menuForReview.text = forReviewCount.toString()
            menuFinished.text = completedCount.toString()
        })

        // Fetch case records (this will also update the count)
        caseRecordViewModel.fetchCaseRecords()

        // Set click listener for "Start New Test" button
        startNewTest.setOnClickListener {
            val intent = Intent(this, CreateNewTestActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for Patient Module
        patientModule.setOnClickListener {
            val intent = Intent(this, PatientActivity::class.java)
            startActivity(intent)
        }
    }
}