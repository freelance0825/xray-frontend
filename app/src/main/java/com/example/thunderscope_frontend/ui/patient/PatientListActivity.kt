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

class PatientListActivity : AppCompatActivity() {

    // UI components
    private lateinit var allCasesCount: TextView
    private lateinit var menuAllCasesCount : TextView
    private lateinit var startNewTest: Button

    // Case Record ViewModel (properly initialized)
    private val caseRecordViewModel: CaseRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_list_activity)

        // Initialize UI components
        allCasesCount = findViewById(R.id.all_cases_number)
        startNewTest = findViewById(R.id.start_new_test_button)
        menuAllCasesCount = findViewById(R.id.menu_all_cases_count)


        // Observe case count LiveData and update UI when it changes
        caseRecordViewModel.caseCountLiveData.observe(this, Observer { count ->
            allCasesCount.text = count.toString()
            menuAllCasesCount.text =count.toString()
        })

        // Fetch case records (this will also update the count)
        caseRecordViewModel.fetchCaseRecords()


        // Set click listener for "Start New Test" button
        startNewTest.setOnClickListener {
            val intent = Intent(this, CreateNewTestActivity::class.java)
            startActivity(intent)
        }
    }
}
