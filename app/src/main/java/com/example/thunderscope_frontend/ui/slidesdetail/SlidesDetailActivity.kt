package com.example.thunderscope_frontend.ui.slidesdetail

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.data.models.SlidesItem
import com.example.thunderscope_frontend.databinding.ActivitySlidesDetailBinding

class SlidesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesDetailBinding

    private val slidesDetailViewModel by viewModels<SlidesDetailViewModel> {
        SlidesDetailViewModel.Factory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlidesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OpenCV.initDebug()

        observeViewModel()

        setListeners()
    }

    private fun observeViewModel() {
        slidesDetailViewModel.apply {
            slideItems.observe(this@SlidesDetailActivity) { slideList ->
                currentlySelectedSlides.observe(this@SlidesDetailActivity) { selectedSlides ->
                    binding.tvCaseId.text = selectedSlides?.caseRecord?.id.toString()

//                    binding.ivDummy.setImageBitmap(Base64Helper.convertToBitmap(selectedSlides?.mainImage))

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


            btnBack.setOnClickListener {
                finish()
            }
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
            val selectedSlide = slideList[position]
            slidesDetailViewModel.updateSelectedSlide(selectedSlide)
        }

        binding.spinnerSlides.setOnClickListener {
            binding.spinnerSlides.showDropDown()
        }
    }
}