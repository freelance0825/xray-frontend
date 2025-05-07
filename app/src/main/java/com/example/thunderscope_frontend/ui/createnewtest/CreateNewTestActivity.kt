package com.example.thunderscope_frontend.ui.createnewtest

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.databinding.CreateNewTestActivityBinding
import com.example.thunderscope_frontend.ui.createnewtest.screens.CreateNewTestActivityFragment
import com.example.thunderscope_frontend.ui.createnewtest.screens.CreatePatientInfoActivityFragment

class CreateNewTestActivity : AppCompatActivity() {

    private lateinit var binding: CreateNewTestActivityBinding

    private val doctorId: Long by lazy { intent.getLongExtra(EXTRA_DOCTOR_ID, 0) }

    val viewModel by viewModels<CreateNewTestViewModel> {
        CreateNewTestViewModel.Factory(this, doctorId.toInt())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateNewTestActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // POLICY WORKAROUND - Refactor Later with MVVM Architecture
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CreateNewTestActivityFragment())
            .addToBackStack(null)
            .commit()

        viewModel.caseRecordResponse.observe(this) {
            if (it != null) {
                val intent = Intent()
                setResult(RESULT_OK, intent)
            }
        }

        viewModel.isLoadingPreparingTest.observe(this) {
            binding.loadingContainer.visibility = if (it) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.isStateChanged.observe(this) {
            toggleIsStateChanged(it)

            if (it) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CreatePatientInfoActivityFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CreateNewTestActivityFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun toggleIsStateChanged(isStateChanged: Boolean) {
        binding.apply {
            if (isStateChanged) {
                cbSetup.isChecked = false
                cbInfo.isChecked = true
            }

            if (isStateChanged) {
                layoutSetupDevice.setBackgroundResource(0)
                layoutPatientInfo.setBackgroundResource(R.drawable.bg_soft_gray)

                tvPatientInfo.setTypeface(null, Typeface.BOLD)
                tvSetupDevice.setTypeface(null, Typeface.NORMAL)
            } else {
                layoutSetupDevice.setBackgroundResource(R.drawable.bg_soft_gray)
                layoutPatientInfo.setBackgroundResource(0)

                tvPatientInfo.setTypeface(null, Typeface.NORMAL)
                tvSetupDevice.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    companion object {
        const val EXTRA_DOCTOR_ID = "extra_doctor_id"
    }
}