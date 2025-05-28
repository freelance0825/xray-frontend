package com.example.xray_frontend.ui.report

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.xray_frontend.R
import com.example.xray_frontend.data.models.PatientResponse
import com.example.xray_frontend.databinding.ActivityReportBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.utils.helpers.Base64Helper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ReportActivity : BaseActivity() {
    private lateinit var binding: ActivityReportBinding

    private val slideId by lazy {
        intent.getLongExtra(EXTRA_SLIDE_ID, 0)
    }

    private val patientResponseData: PatientResponse? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_PATIENT, PatientResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_PATIENT)
        }
    }

    private val reportViewModel by viewModels<ReportViewModel> {
        ReportViewModel.Factory(this, slideId)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViews()

        setViews()
        setListeners()
    }

    private fun observeViews() {
        reportViewModel.currentlySelectedSlide.observe(this) { slide ->
            slide?.let {
                binding.apply {
                    slide.caseRecordResponse?.patient?.let {
                        val patientPrefix = if (it.gender?.lowercase().equals("female")) "Mrs." else "Mr."
                        tvPatientName.text = StringBuilder("$patientPrefix ${it.name}")
                        tvPatientGenderAge.text = StringBuilder("${patientResponseData?.gender} • ${patientResponseData?.age} years old")
                        tvPatientMobileNumber.text = getString(R.string.activity_patient_report_dashboard_pdf_mob_no_label, it.phoneNumber.toString())
                        tvPatientId.text = getString(R.string.activity_patient_report_dashboard_pdf_patient_id_label, it.id.toString())
                        tvPatientAddress.text = it.address
                    }

                    tvSampleCollectedAtAddress.text = slide.collectionSite
//                    tvSubmissionConclusion.text = slide.microscopicDc
//                    tvSubmissionDiagnosis.text = slide.diagnosis

                    slide.caseRecordResponse?.doctor.let {
                        ivSignature.setImageBitmap(Base64Helper.convertToBitmap(it?.signature))
                        tvDoctorName.text = it?.name
                        tvOccupation.text = it?.specialist
                    }
                }
            }
        }
    }

    private fun setViews() {
        binding.apply {
            patientResponseData?.let {
                val patientPrefix = if (it.gender?.lowercase().equals("female")) "Mrs." else "Mr."
                tvPatientName.text = StringBuilder("$patientPrefix ${it.name}")
                tvPatientGenderAge.text = StringBuilder("${patientResponseData?.gender} • ${patientResponseData?.age} years old")
                tvPatientMobileNumber.text = getString(R.string.activity_patient_report_dashboard_pdf_mob_no_label, it.phoneNumber.toString())
                tvPatientId.text = getString(R.string.activity_patient_report_dashboard_pdf_patient_id_label, it.id.toString())
                tvPatientAddress.text = it.address
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            fabShare.setOnClickListener {
//                sharePdfLayoutAsPdf()
            }
        }
    }

    private fun sharePdfLayoutAsPdf() {
        val pdfFile = generatePdfFromLayout(binding.tvPatientGenderAge)

        pdfFile?.let {
//            val uri = FileProvider.getUriForFile(
//                this,
//                "${applicationContext.packageName}.provider",
//                it
//            )
//
//            val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                type = "application/pdf"
//                putExtra(Intent.EXTRA_STREAM, uri)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//
//            startActivity(Intent.createChooser(shareIntent, "Share PDF via"))

            openPdf(it)
        }
    }

    private fun openPdf(pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            pdfFile
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(openIntent) // Open PDF with installed viewer (Adobe, Drive, etc.)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generatePdfFromLayout(view: View): File? {
        val document = PdfDocument()

        // Measure the view
        val width = view.width.takeIf { it > 0 } ?: 1080
        val height = view.height.takeIf { it > 0 } ?: 1500

        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(view.measuredWidth, view.measuredHeight, 1).create()
        val page = document.startPage(pageInfo)
        view.draw(page.canvas)
        document.finishPage(page)

        // Save PDF file
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "fundoscope_report.pdf")
        return try {
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    companion object {
        const val EXTRA_SLIDE_ID = "extra_slide_id"
        const val EXTRA_PATIENT = "extra_patient"
        const val EXTRA_DOCTOR = "extra_doctor"
    }
}