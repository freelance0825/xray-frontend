package com.example.thunderscope_frontend.ui.slidesdetail

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ArrayAdapter
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

class SlidesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesDetailBinding

    private val slidesDetailViewModel by viewModels<SlidesDetailViewModel> {
        SlidesDetailViewModel.Factory(this)
    }

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

                    binding.ivBaseImage.setImageBitmap(Base64Helper.convertToBitmap(selectedSlides?.mainImage))

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
            btnRectangle.setOnClickListener {
                ivBaseImage.enableDrawing(true)
                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.RECTANGLE)
            }
            btnCircle.setOnClickListener {
                ivBaseImage.enableDrawing(true)
                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.CIRCLE)
            }
            btnFreeDraw.setOnClickListener {
                ivBaseImage.enableDrawing(true)
                ivBaseImage.setDrawMode(ZoomImageView.DrawMode.FREE_DRAW)
            }

            btnClear.setOnClickListener {
                ivBaseImage.clearLastDrawing()
                ivBaseImage.enableDrawing(false)
            }

            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun applyFilter(bitmap: Bitmap): Bitmap {
        val srcMat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        val filteredMat = Mat()
        Imgproc.GaussianBlur(srcMat, filteredMat, Size(15.0, 15.0), 0.0)

        val filteredBitmap = createBitmap(bitmap.width, bitmap.height)
        org.opencv.android.Utils.matToBitmap(filteredMat, filteredBitmap)
        return filteredBitmap
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
            val selectedSlide = slideList[position]
            slidesDetailViewModel.updateSelectedSlide(selectedSlide)
        }

        binding.spinnerSlides.setOnClickListener {
            binding.spinnerSlides.showDropDown()
        }
    }
}