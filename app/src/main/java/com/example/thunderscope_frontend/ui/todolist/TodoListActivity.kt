package com.example.thunderscope_frontend.ui.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.patient.PatientListActivity
import com.example.thunderscope_frontend.viewmodel.CaseRecordViewModel
import kotlin.getValue

class TodoListActivity : AppCompatActivity() {

    // UI components
    private lateinit var todoCount: TextView
    private lateinit var menuAllCasesCount: TextView
    private lateinit var menuHighPriorityCount: TextView
    private lateinit var menuInPreparations: TextView
    private lateinit var menuForReview: TextView
    private lateinit var menuFinished: TextView
    private lateinit var backButton: ImageView

    // Case Record ViewModel (properly initialized)
    private val caseRecordViewModel: CaseRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_list)

        // Initialize UI components
        todoCount = findViewById(R.id.todo_list_count)
        menuAllCasesCount = findViewById(R.id.menu_all_cases_count)
        menuHighPriorityCount = findViewById(R.id.menu_high_priority_count)
        menuInPreparations = findViewById(R.id.menu_in_preparations_count)
        menuForReview = findViewById(R.id.menu_for_review_count)
        menuFinished = findViewById(R.id.menu_finished_count)
        backButton = findViewById(R.id.back_button)

        // Observe case records list and update UI
        caseRecordViewModel.caseRecordsLiveData.observe(this, Observer { caseList ->
            val totalTodo = caseList.size
            val highPriorityCount = caseList.count { it.status == "High Priority" }
            val inPreparationsCount = caseList.count { it.status == "In Preparations" }
            val forReviewCount = caseList.count { it.status == "For Review" }
            val completedCount = caseList.count { it.status == "Completed" }

            // Update UI
            todoCount.text = totalTodo.toString()
            menuAllCasesCount.text = totalTodo.toString()
            menuHighPriorityCount.text = highPriorityCount.toString()
            menuInPreparations.text = inPreparationsCount.toString()
            menuForReview.text = forReviewCount.toString()
            menuFinished.text = completedCount.toString()
        })

        // Fetch case records (this will also update the count)
        caseRecordViewModel.fetchCaseRecords()

        // Back button click listener
        backButton.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}