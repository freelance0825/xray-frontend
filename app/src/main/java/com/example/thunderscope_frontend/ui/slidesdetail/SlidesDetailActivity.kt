package com.example.thunderscope_frontend.ui.slidesdetail

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.Patient
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.databinding.ActivitySlidesDetailBinding
import com.example.thunderscope_frontend.ui.report.ReportActivity
import com.example.thunderscope_frontend.ui.slidesdetail.adapters.SavedAnnotationAdapter
import com.example.thunderscope_frontend.ui.slidesdetail.customview.ShapeType
import com.example.thunderscope_frontend.ui.slidesdetail.customview.ZoomImageView
import com.google.android.material.button.MaterialButton
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SlidesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesDetailBinding

    private val slidesDetailViewModel by viewModels<SlidesDetailViewModel> {
        SlidesDetailViewModel.Factory(this)
    }

    private val patientData: Patient? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_PATIENT, Patient::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_PATIENT)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            (result.data?.data as Uri).let { uri ->
                slidesDetailViewModel.signatureImageFile.postValue(reduceFileSize(uri.toFile()))
            }
        }
    }

    private val savedAnnotationAdapter = SavedAnnotationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlidesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OpenCVLoader.initDebug()

        observeViewModel()

        setListeners()
    }

    private fun observeViewModel() {
        slidesDetailViewModel.apply {
            slideItems.observe(this@SlidesDetailActivity) { slideList ->
                currentlySelectedSlides.observe(this@SlidesDetailActivity) { selectedSlides ->
                    binding.tvCaseId.text = selectedSlides?.caseRecord?.id.toString()

                    binding.ivBaseImage.setImageBitmap(selectedSlides?.bitmapImage)

                    binding.tvActiveSlides.text = getString(
                        R.string.activity_slides_detail_slides_selected,
                        (slideList.indexOfFirst { it.id == selectedSlides?.id } + 1).toString(),
                        slideList.size.toString(),
                    )

                    handleSpinner(selectedSlides, slideList)
                }
            }

            selectedPaintColor.observe(this@SlidesDetailActivity) {
                if (it != 0) {
                    binding.ivBaseImage.setPaintColor(it)
                    binding.cvPaintColorFlag.setCardBackgroundColor(it)
                }
            }

            selectedAnnotationShape.observe(this@SlidesDetailActivity) { annotationShape ->
                annotationShape?.let {
                    when (annotationShape) {
                        ShapeType.RECTANGLE -> {
                            binding.ivFlagAnnotateShape.setImageResource(R.drawable.ic_rectangle)

                            binding.ivBaseImage.enableDrawing(true)
                            binding.ivBaseImage.setDrawMode(ZoomImageView.DrawMode.RECTANGLE)
                        }

                        ShapeType.CIRCLE -> {
                            binding.ivFlagAnnotateShape.setImageResource(R.drawable.ic_circle)

                            binding.ivBaseImage.enableDrawing(true)
                            binding.ivBaseImage.setDrawMode(ZoomImageView.DrawMode.CIRCLE)
                        }

                        ShapeType.FREE_DRAW -> {
                            binding.ivFlagAnnotateShape.setImageResource(R.drawable.ic_point)

                            binding.ivBaseImage.enableDrawing(true)
                            binding.ivBaseImage.setDrawMode(ZoomImageView.DrawMode.FREE_DRAW)
                        }
                    }
                }
            }

            selectedMenuOptions.observe(this@SlidesDetailActivity) { menu ->
                resetSelectedMenuActiveColorState()

                when (menu) {
                    SlidesDetailViewModel.SelectedMenu.SELECT -> {
                        binding.apply {
                            ivSelect.setImageTintList(ColorStateList.valueOf(getColor(R.color.active_menu_color)))
                            tvSelect.setTextColor(getColor(R.color.active_menu_color))
                        }

                        binding.ivBaseImage.enableDrawing(false)
                        openLeftMenuSettings(false, menu)
                    }

                    SlidesDetailViewModel.SelectedMenu.ANNOTATE -> {
                        binding.apply {
                            ivFlagAnnotateShape.setImageTintList(ColorStateList.valueOf(getColor(R.color.active_menu_color)))
                            ivFlagAnnotateDropdown.setImageTintList(
                                ColorStateList.valueOf(
                                    getColor(
                                        R.color.active_menu_color
                                    )
                                )
                            )
                            tvAnnotate.setTextColor(getColor(R.color.active_menu_color))
                        }

                        binding.tvMenuContentsTitle.text = StringBuilder("Annotation Shape")
                        binding.btnClose.setOnClickListener {
                            selectedMenuOptions.value = SlidesDetailViewModel.SelectedMenu.SELECT
                        }

                        openLeftMenuSettings(true, menu)
                    }

                    SlidesDetailViewModel.SelectedMenu.ANNOTATE_COLOR -> {
                        ColorPickerDialog.Builder(this@SlidesDetailActivity)
                            .setTitle("Pick Annotation Line Color")
                            .setPreferenceName("annotation_line_color")
                            .setPositiveButton(
                                "Confirm",
                                object : ColorEnvelopeListener {
                                    override fun onColorSelected(
                                        envelope: ColorEnvelope?,
                                        fromUser: Boolean
                                    ) {
                                        selectedPaintColor.value =
                                            envelope?.color ?: getColor(R.color.blue_lines)
                                    }
                                })
                            .setNegativeButton(
                                "Cancel"
                            ) { dialogInterface, _ -> dialogInterface.dismiss() }
                            .attachAlphaSlideBar(true)
                            .attachBrightnessSlideBar(true)
                            .setBottomSpace(12)
                            .show()
                    }

                    SlidesDetailViewModel.SelectedMenu.IMAGE_SETTINGS -> {
                        binding.apply {
                            ivImageSettings.setImageTintList(ColorStateList.valueOf(getColor(R.color.active_menu_color)))
                            tvImageSettings.setTextColor(getColor(R.color.active_menu_color))
                        }

                        binding.tvMenuContentsTitle.text = StringBuilder("Image Settings")
                        binding.btnClose.setOnClickListener {
                            openLeftMenuSettings(false, menu)
                        }

                        openLeftMenuSettings(true, menu)
                    }

                    else -> {}
                }
            }

            gamma.observe(this@SlidesDetailActivity) { value ->
                binding.tvGammaCount.text = String.format("%.1f", value)
            }

            brightness.observe(this@SlidesDetailActivity) { value ->
                binding.tvBrightnessCount.text = "${value.toInt()}%"
            }

            contrast.observe(this@SlidesDetailActivity) { value ->
                binding.tvContrastCount.text = "${(value * 100).toInt() - 100}%"
            }

            redAdjust.observe(this@SlidesDetailActivity) { value ->
                binding.tvRedCount.text = "${(value * 100).toInt() - 100}%"
            }

            greenAdjust.observe(this@SlidesDetailActivity) { value ->
                binding.tvGreenCount.text = "${(value * 100).toInt() - 100}%"
            }

            blueAdjust.observe(this@SlidesDetailActivity) { value ->
                binding.tvBlueCount.text = "${(value * 100).toInt() - 100}%"
            }

            // DUMMY SEGMENTATIONS!!!
            selectedViewSettings.observe(this@SlidesDetailActivity) { viewSettings ->
                when (viewSettings) {
                    SlidesDetailViewModel.SelectedViewSettings.ORIGINAL -> {
                        binding.ivDummySegmentation.visibility = View.GONE
                        binding.layoutSegmentationSettings.visibility = View.GONE
                    }

                    SlidesDetailViewModel.SelectedViewSettings.SEGMENTATION -> {
                        binding.ivDummySegmentation.visibility = View.VISIBLE
                        binding.layoutSegmentationSettings.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.ivDummySegmentation.visibility = View.GONE
                    }
                }
            }

            selectedSegmentationSettings.observe(this@SlidesDetailActivity, ::showSegmentationConfigurationLayout)

            patientData?.let { patient ->
                binding.tvPatientName.text = patient.name
                binding.tvPatientId.text = patient.id.toString()
                binding.tvPatientDob.text = StringBuilder("${patient.dateOfBirth} (${patient.age} yo)")
                binding.tvPatientGender.text = patient.gender
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            btnCompleteReview.setOnClickListener {
                openPostTestReviewDialog()
            }


            // Left Menu Configuration
            btnSelect.setOnClickListener {
                slidesDetailViewModel.selectedMenuOptions.value =
                    SlidesDetailViewModel.SelectedMenu.SELECT
            }

            btnAnnotate.setOnClickListener {
                slidesDetailViewModel.selectedMenuOptions.value =
                    SlidesDetailViewModel.SelectedMenu.ANNOTATE
            }

            btnAnnotateColor.setOnClickListener {
                slidesDetailViewModel.selectedMenuOptions.value =
                    SlidesDetailViewModel.SelectedMenu.ANNOTATE_COLOR
            }

            btnImageSettings.setOnClickListener {
                slidesDetailViewModel.selectedMenuOptions.value =
                    SlidesDetailViewModel.SelectedMenu.IMAGE_SETTINGS
            }

            // View Settings Configuration
            rgViewSettings.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_view_settings_original -> {
                        slidesDetailViewModel.selectedViewSettings.value =
                            SlidesDetailViewModel.SelectedViewSettings.ORIGINAL
                    }

                    R.id.rb_view_settings_segmentation -> {
                        slidesDetailViewModel.selectedViewSettings.value =
                            SlidesDetailViewModel.SelectedViewSettings.SEGMENTATION
                    }
                }
            }

            // Segmentation Settings Configuration
            rgSegmentationSettings.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_st_optic_disc_centroid -> {
                        slidesDetailViewModel.selectedSegmentationSettings.value =
                            SlidesDetailViewModel.SelectedSegmentationSettings.ODC
                    }

                    R.id.rb_st_optic_disc_mask -> {
                        slidesDetailViewModel.selectedSegmentationSettings.value =
                            SlidesDetailViewModel.SelectedSegmentationSettings.ODM
                    }

                    R.id.rb_st_optic_cup_mask -> {
                        slidesDetailViewModel.selectedSegmentationSettings.value =
                            SlidesDetailViewModel.SelectedSegmentationSettings.OCM
                    }

                    R.id.rb_st_fovea_centroid -> {
                        slidesDetailViewModel.selectedSegmentationSettings.value =
                            SlidesDetailViewModel.SelectedSegmentationSettings.FC
                    }

                    R.id.rb_st_morphological_measurement -> {
                        slidesDetailViewModel.selectedSegmentationSettings.value =
                            SlidesDetailViewModel.SelectedSegmentationSettings.MM
                    }
                }
            }

            // Annotate Shape Configuration
            btnAnnotateRectangle.setOnClickListener {
                slidesDetailViewModel.selectedAnnotationShape.value = ShapeType.RECTANGLE
                openLeftMenuSettings(false, SlidesDetailViewModel.SelectedMenu.ANNOTATE)
            }

            btnAnnotateCircle.setOnClickListener {
                slidesDetailViewModel.selectedAnnotationShape.value = ShapeType.CIRCLE
                openLeftMenuSettings(false, SlidesDetailViewModel.SelectedMenu.ANNOTATE)
            }

            btnAnnotateFreeDraw.setOnClickListener {
                slidesDetailViewModel.selectedAnnotationShape.value = ShapeType.FREE_DRAW
                openLeftMenuSettings(false, SlidesDetailViewModel.SelectedMenu.ANNOTATE)
            }


            // Image Settings Configurations
            seekGamma.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateGamma(value)
            })

            seekBrightness.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateBrightness(value)
            })

            seekContrast.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateContrast(value)
            })

            seekRed.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateRed(value)
            })

            seekGreen.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateGreen(value)
            })

            seekBlue.setOnSeekBarChangeListener(seekBarChangeListener { value ->
                slidesDetailViewModel.updateBlue(value)
            })

            btnApplyFilter.setOnClickListener {
                processFilter()
            }

            btnCancelFilter.setOnClickListener {
                resetFilters()
                processFilter()
            }

            btnBack.setOnClickListener {
                finish()
            }

            rvSavedAnnotation.apply {
                savedAnnotationAdapter.submitList(slidesDetailViewModel.generateDummyAnnotationItem())
                adapter = savedAnnotationAdapter
                layoutManager =
                    LinearLayoutManager(this@SlidesDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun processFilter() {
        val originalBitmap =
            slidesDetailViewModel.currentlySelectedSlides.value?.bitmapImage
        if (originalBitmap != null) {
            val filteredBitmap = applyFilter(originalBitmap)
            binding.ivBaseImage.setImageBitmap(filteredBitmap)
        }
    }

    private fun applyFilter(bitmap: Bitmap): Bitmap {
        val gamma = slidesDetailViewModel.gamma.value ?: 1.0
        val brightness = slidesDetailViewModel.brightness.value ?: 0.0
        val contrast = slidesDetailViewModel.contrast.value ?: 1.0
        val redAdjust = slidesDetailViewModel.redAdjust.value ?: 1.0
        val greenAdjust = slidesDetailViewModel.greenAdjust.value ?: 1.0
        val blueAdjust = slidesDetailViewModel.blueAdjust.value ?: 1.0

        val srcMat = Mat()
        val outputMat = Mat()

        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        org.opencv.android.Utils.bitmapToMat(bmp32, srcMat)

        // Adjust Brightness and Contrast
        srcMat.convertTo(outputMat, -1, contrast, brightness)

        // Adjust Gamma
        val gammaLUT = Mat(1, 256, CvType.CV_8U)
        val lutBuffer = ByteBuffer.allocate(256)
        for (i in 0 until 256) {
            lutBuffer.put(i, ((255.0 * Math.pow(i / 255.0, gamma)).toInt()).toByte())
        }
        gammaLUT.put(0, 0, lutBuffer.array())
        Core.LUT(outputMat, gammaLUT, outputMat)

        // Adjust RGB channels separately
        val channels = ArrayList<Mat>(3)
        Core.split(outputMat, channels)

        Core.multiply(channels[0], Scalar(redAdjust), channels[0])
        Core.multiply(channels[1], Scalar(greenAdjust), channels[1])
        Core.multiply(channels[2], Scalar(blueAdjust), channels[2])

        Core.merge(channels, outputMat)

        val resultBitmap = createBitmap(bitmap.width, bitmap.height)
        org.opencv.android.Utils.matToBitmap(outputMat, resultBitmap)

        srcMat.release()
        outputMat.release()
        gammaLUT.release()
        for (mat in channels) {
            mat.release()
        }

        return resultBitmap
    }

    private fun resetFilters() {
        binding.apply {
            seekGamma.progress = 10
            seekBrightness.progress = 100
            seekContrast.progress = 100
            seekRed.progress = 100
            seekGreen.progress = 100
            seekBlue.progress = 100
        }

        slidesDetailViewModel.resetFilters()
    }

    private fun seekBarChangeListener(action: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                action(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    private fun handleSpinner(selectedSlides: SlidesItem?, slideList: List<SlidesItem>) {
        val slideNames = slideList.map { it.id.toString() }

        val adapter = ArrayAdapter(
            this@SlidesDetailActivity,
            android.R.layout.simple_dropdown_item_1line, // Menggunakan dropdown style
            slideNames
        )
        binding.spinnerSlides.setAdapter(adapter)

        val selectedIndex = slideList.indexOfFirst { it.id == selectedSlides?.id }
        if (selectedIndex != -1) {
            binding.spinnerSlides.setText(slideNames[selectedIndex], false)
        } else if (slideList.isNotEmpty()) {
            binding.spinnerSlides.setText(slideNames[0], false)
            slidesDetailViewModel.updateSelectedSlide(slideList[0])
        }

        // RESET STATE WHEN CHANGE IMAGE
        binding.spinnerSlides.setOnItemClickListener { _, _, position, _ ->
            resetFilters()

            binding.rbViewSettingsOriginal.isChecked = true
            slidesDetailViewModel.selectedViewSettings.value = SlidesDetailViewModel.SelectedViewSettings.ORIGINAL
            binding.ivBaseImage.clearCanvas()
            binding.ivBaseImage.resetZoomManually()

            val selectedSlide = slideList[position]
            slidesDetailViewModel.updateSelectedSlide(selectedSlide)
        }

        binding.spinnerSlides.setOnClickListener {
            binding.spinnerSlides.showDropDown()
        }
    }

    private fun openLeftMenuSettings(
        isOpening: Boolean,
        menuType: SlidesDetailViewModel.SelectedMenu
    ) {
        binding.apply {
            if (isOpening) {
                if (menuType == SlidesDetailViewModel.SelectedMenu.ANNOTATE) {
                    layoutImageSettings.visibility = View.GONE
                    layoutAnnotateShapeMenu.visibility = View.VISIBLE
                } else if (menuType == SlidesDetailViewModel.SelectedMenu.IMAGE_SETTINGS) {
                    layoutAnnotateShapeMenu.visibility = View.GONE
                    layoutImageSettings.visibility = View.VISIBLE
                }
            } else {
                layoutAnnotateShapeMenu.visibility = View.GONE
                layoutImageSettings.visibility = View.GONE
            }

            layoutMenuContents.visibility = if (isOpening) View.VISIBLE else View.GONE
        }
    }

    private fun resetSelectedMenuActiveColorState() {
        binding.apply {
            ivSelect.setImageTintList(ColorStateList.valueOf(Color.WHITE))
            tvSelect.setTextColor(Color.WHITE)

            ivFlagAnnotateShape.setImageTintList(ColorStateList.valueOf(Color.WHITE))
            ivFlagAnnotateDropdown.setImageTintList(ColorStateList.valueOf(Color.WHITE))
            tvAnnotate.setTextColor(Color.WHITE)

            ivImageSettings.setImageTintList(ColorStateList.valueOf(Color.WHITE))
            tvImageSettings.setTextColor(Color.WHITE)
        }
    }

    private fun showSegmentationConfigurationLayout(segmentationType: SlidesDetailViewModel.SelectedSegmentationSettings) {
        binding.apply {
            layoutOdcConfig.visibility = View.GONE
            layoutOdmConfig.visibility = View.GONE
            layoutOcmConfig.visibility = View.GONE
            layoutFcConfig.visibility = View.GONE
            layoutMmConfig.visibility = View.GONE

            when (segmentationType) {
                SlidesDetailViewModel.SelectedSegmentationSettings.ODC -> {
                    layoutOdcConfig.visibility = View.VISIBLE
                }

                SlidesDetailViewModel.SelectedSegmentationSettings.ODM -> {
                    layoutOdmConfig.visibility = View.VISIBLE
                }

                SlidesDetailViewModel.SelectedSegmentationSettings.OCM -> {
                    layoutOcmConfig.visibility = View.VISIBLE
                }

                SlidesDetailViewModel.SelectedSegmentationSettings.FC -> {
                    layoutFcConfig.visibility = View.VISIBLE
                }

                SlidesDetailViewModel.SelectedSegmentationSettings.MM -> {
                    layoutMmConfig.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun Uri.toFile(): File {
        val tempFile = File.createTempFile(
            "TS_SIGNATURE_${System.currentTimeMillis()}_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun reduceFileSize(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun openPostTestReviewDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_post_test_review, null)

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind Views
        val btnClose = dialogView.findViewById<ImageView>(R.id.btn_close)
        val tvPatientName = dialogView.findViewById<TextView>(R.id.tv_patient_name)
        val tvPatientGenderAge = dialogView.findViewById<TextView>(R.id.tv_patient_gender_age)
        val tvDateNow = dialogView.findViewById<TextView>(R.id.tv_date_now)
        val tvTimeNow = dialogView.findViewById<TextView>(R.id.tv_time_now)
        val tvDateTimeCombined = dialogView.findViewById<TextView>(R.id.tv_date_time_combined_now)
        val btnGetSignature = dialogView.findViewById<LinearLayout>(R.id.btn_get_signature)
        val ivSignature = dialogView.findViewById<ImageView>(R.id.iv_signature)
        val ivGender = dialogView.findViewById<ImageView>(R.id.iv_gender)
        val layoutPlaceholderSignature = dialogView.findViewById<LinearLayout>(R.id.layout_placeholder_signature)
        val layoutSignatureView = dialogView.findViewById<LinearLayout>(R.id.layout_signature_view)
        val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btn_submit)

        // Set current date & time
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        val currentDate = dateFormat.format(now.time)
        val currentTime = timeFormat.format(now.time)

        tvPatientName.text = patientData?.name
        tvPatientGenderAge.text = StringBuilder("${patientData?.gender} • ${patientData?.age} years old")
        tvDateNow.text = currentDate
        tvTimeNow.text = currentTime
        tvDateTimeCombined.text = StringBuilder("$currentDate • $currentTime")

        if (patientData?.gender?.lowercase().equals("female")) {
            ivGender.setImageDrawable(ContextCompat.getDrawable(this@SlidesDetailActivity, R.drawable.ic_female))
        } else {
            ivGender.setImageDrawable(ContextCompat.getDrawable(this@SlidesDetailActivity, R.drawable.ic_male))
        }

        slidesDetailViewModel.signatureImageFile.observe(this@SlidesDetailActivity) {
            it?.let {
                ivSignature.setImageBitmap(BitmapFactory.decodeFile(it.path))
            }

            layoutPlaceholderSignature.visibility = if (it != null) View.GONE else View.VISIBLE
            layoutSignatureView.visibility = if (it == null) View.GONE else View.VISIBLE
        }

        // Close dialog
        btnClose.setOnClickListener {
            slidesDetailViewModel.signatureImageFile.value = null
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            slidesDetailViewModel.signatureImageFile.value = null
            dialog.dismiss()

//            startActivity(Intent(this@SlidesDetailActivity, ReportActivity::class.java))
        }

        // Open Gallery to Pick Signature
        btnGetSignature.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "")
            galleryLauncher.launch(chooser)
        }

        dialog.show()
        val window = dialog.window
        window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), LayoutParams.WRAP_CONTENT)
    }


    companion object {
        const val EXTRA_PATIENT = "extra_patient"
    }
}