package com.example.thunderscope_frontend.ui.slides

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.databinding.ActivitySlidesBinding
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI

class SlidesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesBinding

    private val caseRecord: CaseRecordUI? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            intent.getParcelableExtra(EXTRA_CASE_RECORD, CaseRecordUI::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_CASE_RECORD)
        }
    }

    private val slidesViewModel by viewModels<SlidesViewModel> {
        SlidesViewModel.Factory(caseRecord)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViews()

        setViews()
        setListeners()
    }

    private fun observeViews() {
        slidesViewModel.apply {

        }
    }

    private fun setViews() {
        binding.apply {

        }
    }

    private fun setListeners() {
        binding.apply {

        }
    }

    fun generateInitials(name: String): String {
        val cleanName = name.replace(Regex("^dr\\.?", RegexOption.IGNORE_CASE), "").trim()
        val words = cleanName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.isEmpty() -> ""
            words.size == 1 -> words[0].firstOrNull()?.uppercase() ?: ""
            else -> "${words[0].first().uppercase()}${words[1].first().uppercase()}"
        }
    }

    companion object {
        const val EXTRA_CASE_RECORD = "extra_case_record"
    }
}