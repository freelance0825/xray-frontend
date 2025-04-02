package com.example.thunderscope_frontend.ui.patient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.ui.createnewtest.CreateNewTestActivity
import com.example.thunderscope_frontend.ui.createnewtest.CreatePatientInfoActivity
import com.example.thunderscope_frontend.viewmodel.PatientRecordViewModel
import com.google.android.material.button.MaterialButton

class PatientActivity : AppCompatActivity() {

    // UI components
    private lateinit var allPatientCount: TextView
    private lateinit var menuAllPatientCount: TextView
    private lateinit var menuNotStartedCount: TextView
    private lateinit var menuOnProgress: TextView
    private lateinit var menuFinished: TextView
    private lateinit var addNewPatientButton: MaterialButton
    private lateinit var backButton: ImageView

    // Patient Record ViewModel (properly initialized)
    private val patientRecordViewModel: PatientRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_activity)

        // Initialize UI components
        allPatientCount = findViewById(R.id.all_patient_number)
        menuAllPatientCount = findViewById(R.id.menu_all_patient_count)
        menuNotStartedCount = findViewById(R.id.menu_not_started_count)
        menuOnProgress = findViewById(R.id.menu_on_progress_count)
        menuFinished = findViewById(R.id.menu_finished_count)
        addNewPatientButton = findViewById(R.id.add_new_patient_button)
        backButton = findViewById(R.id.back_button)


        // Observe case records list and update UI
        patientRecordViewModel.patientRecordsLiveData.observe(this, Observer { caseList ->
            val totalCases = caseList.size

            val notStartedCount = caseList.count { it.patientStatus == "Not Started" }
            val onProgressCount = caseList.count { it.patientStatus == "On Progress" }
            val completedCount = caseList.count { it.patientStatus == "Completed" }

            // Update UI
            allPatientCount.text = StringBuilder("($totalCases)")
            menuAllPatientCount.text = totalCases.toString()
            menuNotStartedCount.text = notStartedCount.toString()
            menuOnProgress.text = onProgressCount.toString()
            menuFinished.text = completedCount.toString()
        })

        // Fetch patient records (this will also update the count)
        patientRecordViewModel.fetchCaseRecords()

        // Back button click listener
        backButton.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
