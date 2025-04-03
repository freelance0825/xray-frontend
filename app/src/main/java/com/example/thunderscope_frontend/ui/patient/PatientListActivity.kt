package com.example.thunderscope_frontend.ui.patient

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.databinding.PatientListActivityBinding
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.thunderscope_frontend.ui.todolist.TodoListActivity
import com.example.thunderscope_frontend.viewmodel.CaseRecordViewModel
import com.google.android.material.button.MaterialButton

class PatientListActivity : AppCompatActivity() {

    private lateinit var binding: PatientListActivityBinding

    // UI components
    private lateinit var allCasesCount: TextView
    private lateinit var menuAllCasesCount: TextView
    private lateinit var menuHighPriorityCount: TextView
    private lateinit var menuInPreparations: TextView
    private lateinit var menuForReview: TextView
    private lateinit var menuFinished: TextView
    private lateinit var startNewTest: MaterialButton
    private lateinit var patientModule: LinearLayout
    private lateinit var todoModule: LinearLayout

    // Case Record ViewModel (properly initialized)
    private val caseRecordViewModel: CaseRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PatientListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        allCasesCount = binding.allCasesNumber
        menuAllCasesCount = binding.menuAllCasesCount
        menuHighPriorityCount = binding.menuHighPriorityCount
        menuInPreparations = binding.menuInPreparationsCount
        menuForReview = binding.menuForReviewCount
        menuFinished = binding.menuFinishedCount
        startNewTest = binding.startNewTestButton
        patientModule = binding.menuPatientModule
        todoModule = binding.menuTodoList


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

        // Set click listener for Todo Module
        todoModule.setOnClickListener {
            val intent = Intent(this, TodoListActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for Patient Module
        patientModule.setOnClickListener {
            val intent = Intent(this, PatientActivity::class.java)
            startActivity(intent)
        }
    }
}