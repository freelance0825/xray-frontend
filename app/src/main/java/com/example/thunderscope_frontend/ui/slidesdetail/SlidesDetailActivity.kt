package com.example.thunderscope_frontend.ui.slidesdetail

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.databinding.ActivitySlidesDetailBinding
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap
import com.example.thunderscope_frontend.ui.slidesdetail.customview.ZoomImageView
import org.opencv.core.Core
import org.opencv.core.Scalar
import java.nio.ByteBuffer

class SlidesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesDetailBinding

    private val slidesDetailViewModel by viewModels<SlidesDetailViewModel> {
        SlidesDetailViewModel.Factory(this)
    }

    private var gamma = 1.0
    private var brightness = 0.0
    private var contrast = 1.0
    private var redAdjust = 1.0
    private var greenAdjust = 1.0
    private var blueAdjust = 1.0

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
        }
    }

    private fun setListeners() {
        binding.apply {
//            btnRectangle.setOnClickListener {
//                ivBaseImage.enableDrawing(true)
//                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.RECTANGLE)
//            }
//            btnCircle.setOnClickListener {
//                ivBaseImage.enableDrawing(true)
//                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.CIRCLE)
//            }
//            btnFreeDraw.setOnClickListener {
//                ivBaseImage.enableDrawing(true)
//                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.FREE_DRAW)
//            }
//
//            btnClear.setOnClickListener {
//                ivBaseImage.clearLastDrawing()
//                ivBaseImage.enableDrawing(false)
//            }

//            binding.seekGamma.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                gamma = 0.1 + (value / 10.0)
//            })
//
//            binding.seekBrightness.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                brightness = value - 100.0
//            })
//
//            binding.seekContrast.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                contrast = 0.5 + (value / 100.0)
//            })
//
//            binding.seekRed.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                redAdjust = value / 100.0
//            })
//
//            binding.seekGreen.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                greenAdjust = value / 100.0
//            })
//
//            binding.seekBlue.setOnSeekBarChangeListener(seekBarChangeListener { value ->
//                blueAdjust = value / 100.0
//            })
//
//            binding.btnApplyFilter.setOnClickListener {
//                val originalBitmap = slidesDetailViewModel.currentlySelectedSlides.value?.bitmapImage
//                if (originalBitmap != null) {
//                    val filteredBitmap = applyFilter(originalBitmap)
//                    binding.ivBaseImage.setImageBitmap(filteredBitmap)
//                }
//            }

            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun applyFilter(bitmap: Bitmap): Bitmap {
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

        Core.multiply(channels[0], Scalar(blueAdjust), channels[0])
        Core.multiply(channels[1], Scalar(greenAdjust), channels[1])
        Core.multiply(channels[2], Scalar(redAdjust), channels[2])

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

        binding.spinnerSlides.setOnItemClickListener { _, _, position, _ ->
//            binding.ivBaseImage.resetZoomManually()
            val selectedSlide = slideList[position]
            slidesDetailViewModel.updateSelectedSlide(selectedSlide)
        }

        binding.spinnerSlides.setOnClickListener {
            binding.spinnerSlides.showDropDown()
        }
    }
}