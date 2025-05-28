package com.example.xray_frontend.ui.crmpatient.patientreportpdf

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.xray_frontend.R
import com.example.xray_frontend.databinding.ActivityPatientReportPdfBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.utils.helpers.Base64Helper

class PatientReportPdfActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientReportPdfBinding

    private val caseId by lazy {
        intent.getLongExtra(EXTRA_CASE_ID, 0)
    }

    private val patientReportPdfViewModel by viewModels<PatientReportPdfViewModel> {
        PatientReportPdfViewModel.Factory(this, caseId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientReportPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViews()
        setListeners()
    }

    private fun observeViews() {
        patientReportPdfViewModel.currentlySelectedCaseRecord.observe(this) { result ->
            result?.let { slide ->
                Log.d("PatientReport", "Observer triggered. New data: $result")
                binding.apply {
                    // Patient Info
                    val patient = slide.caseRecordResponse?.patient
                    patient?.let {
                        val prefix = if (it.gender?.lowercase() == "female") "Mrs." else "Mr."
                        tvPatientName.text = "$prefix ${it.name}"
                        tvPatientGenderAge.text = "${it.gender} • ${it.age} years old"
                        tvPatientMobileNumber.text =
                            getString(R.string.activity_patient_report_dashboard_pdf_mob_no_label, it.phoneNumber ?: "")
                        tvPatientId.text =
                            getString(R.string.activity_patient_report_dashboard_pdf_patient_id_label, it.id?.toString() ?: "")
                        tvPatientAddress.text = it.address ?: ""
                    }

                    // Doctor Info
                    slide.caseRecordResponse?.doctor?.let {
                        // Check if signature is not null or empty before converting to bitmap
                        it.signature?.takeIf { it.isNotEmpty() }?.let { signature ->
                            val signatureBitmap = Base64Helper.convertToBitmap(signature)
                            if (signatureBitmap != null) {
                                ivSignature.setImageBitmap(signatureBitmap)
                            } else {
                                ivSignature.setImageResource(R.drawable.placeholder_drsig)
                            }
                        } ?: run {
                            // Handle case when signature is null or empty
                            ivSignature.setImageResource(R.drawable.placeholder_drsig)
                        }

                        tvDoctorName.text = it.name
                        tvOccupation.text = it.specialist
                    }

                    // Slide Info
                    tvSampleCollectedAtAddress.text = slide.collectionSite ?: "-"
                    tvSubmissionConclusion.text = slide.microscopicDc ?: "-"
                    tvSubmissionDiagnosis.text = slide.diagnosis ?: "-"
                }
            }
        }
    }


    private fun setListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_CASE_ID = "extra_case_id"
    }
}


