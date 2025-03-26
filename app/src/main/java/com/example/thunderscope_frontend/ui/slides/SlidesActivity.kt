package com.example.thunderscope_frontend.ui.slides

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thunderscope_frontend.R
import com.example.thunderscope_frontend.databinding.ActivitySlidesBinding
import com.example.thunderscope_frontend.ui.slides.adapters.AnnotationAdapter
import com.example.thunderscope_frontend.ui.slides.adapters.MenuSlidesAdapter
import com.example.thunderscope_frontend.ui.slides.adapters.PhotoAdapter
import com.example.thunderscope_frontend.ui.slides.adapters.SlidesAdapter
import com.example.thunderscope_frontend.ui.utils.Base64Helper
import com.example.thunderscope_frontend.viewmodel.CaseRecordUI

class SlidesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlidesBinding

    private val caseRecord: CaseRecordUI? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CASE_RECORD, CaseRecordUI::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_CASE_RECORD)
        }
    }

    private val slidesViewModel by viewModels<SlidesViewModel> {
        SlidesViewModel.Factory(caseRecord)
    }

    private val slidesAdapter = SlidesAdapter()
    private val photoAdapter = PhotoAdapter()
    private val menuSlidesAdapter = MenuSlidesAdapter()
    private val annotationAdapter = AnnotationAdapter()

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
            slidesItem.observe(this@SlidesActivity) {
                binding.tvAssesmentCount.text =
                    getString(R.string.activity_slides_assesment_count, it.size.toString())
                slidesAdapter.submitList(it)
            }

            activeSlidesItem.observe(this@SlidesActivity) {
                animateActiveSlidesCounter(it.isNotEmpty())

                if (it.isEmpty() && isOpeningRightMenu.value == true) {
                    isOpeningRightMenu.value = false
                }

                binding.tvActiveSlidesCount.text =
                    getString(R.string.activity_slides_active_count, it.size.toString())
                menuSlidesAdapter.submitList(it)
            }

            photoGalleryItems.observe(this@SlidesActivity) {
                binding.tvPhotoCount.text =
                    getString(R.string.activity_slides_photo_count, it.size.toString())
                photoAdapter.submitList(it)
            }

            annotationItems.observe(this@SlidesActivity) {
                binding.tvAnnotationCount.text =
                    getString(R.string.activity_slides_annotation_count, it.size.toString())
                annotationAdapter.submitList(it)
            }

            isOpeningRightMenu.observe(this@SlidesActivity, ::animateRightMenu)
        }
    }

    private fun setViews() {
        binding.apply {
            caseRecord?.let {
                ivPatient.setImageBitmap(Base64Helper.convertToBitmap(it.patientImage))

                tvCaseId.text = it.caseRecordId.toString()
                tvCaseStatus.text = it.status
                tvPatientId.text = it.patientId.toString()
                tvPatientName.text = it.patientName
                tvPatientNumber.text = it.patientPhoneNumber
                tvPatientBirthdate.text = it.patientBirthdate
                tvPatientGender.text = it.patientGender
                tvPatientAge.text = it.patientAge
                tvDoctorName.text = it.physicianName
                tvDoctorNameMenu.text = it.physicianName
                tvDoctorInitial.text = generateInitials(it.physicianName)
            }

            rvSlides.apply {
                adapter = slidesAdapter
                layoutManager =
                    LinearLayoutManager(this@SlidesActivity, LinearLayoutManager.HORIZONTAL, false)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val totalItemCount = layoutManager.itemCount

                        if (totalItemCount > 0) {
                            val scrollX = recyclerView.computeHorizontalScrollOffset().toFloat()
                            val maxScrollX = recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
                            val maxSliderX = (recyclerView.width - slidesSliderBar.width).toFloat()
                            val newX = (scrollX / maxScrollX) * maxSliderX
                            slidesSliderBar.translationX = newX
                        }
                    }
                })
            }

            rvPhoto.apply {
                adapter = photoAdapter
                layoutManager =
                    LinearLayoutManager(this@SlidesActivity, LinearLayoutManager.HORIZONTAL, false)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val totalItemCount = layoutManager.itemCount

                        if (totalItemCount > 0) {
                            val scrollX = recyclerView.computeHorizontalScrollOffset().toFloat()
                            val maxScrollX = recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
                            val maxSliderX = (recyclerView.width - photoSliderBar.width).toFloat()
                            val newX = (scrollX / maxScrollX) * maxSliderX
                            photoSliderBar.translationX = newX
                        }
                    }
                })
            }

            rvSlidesMenuIndicator.apply {
                adapter = menuSlidesAdapter
                layoutManager =
                    LinearLayoutManager(this@SlidesActivity, LinearLayoutManager.HORIZONTAL, false)
            }

            rvAnnotation.apply {
                adapter = annotationAdapter
                layoutManager = GridLayoutManager(this@SlidesActivity, 3)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.apply {
            btnOpenViewer.setOnClickListener {
                // NAVIGATE TO IMAGE PROCESSING HERE
            }

            slidesAdapter.onItemClick = { slideItem, position ->
                if (!slideItem.isActive && slidesViewModel.isOpeningRightMenu.value == false) {
                    slidesViewModel.isOpeningRightMenu.value = true
                }

                slidesViewModel.toggleSlidesItem(slideItem)
                slidesAdapter.notifyItemChanged(position)
            }

            menuSlidesAdapter.onItemClick = { slideItem, position ->
                if (!slideItem.isCurrentlySelected) {
                    slidesViewModel.toggleSlidesItem(slideItem, true)
                    slidesAdapter.notifyItemChanged(position)
                }
            }

            btnCloseMenu.setOnClickListener {
                slidesViewModel.isOpeningRightMenu.value = false
            }

            btnBack.setOnClickListener {
                finish()
            }

            // SlideBars
            slidesSliderBar.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        slidesViewModel.isDraggingSlides = true
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (slidesViewModel.isDraggingSlides) {
                            val parentWidth = (slidesSliderBar.parent as View).width
                            val maxSliderX = parentWidth - slidesSliderBar.width

                            val newX = (event.rawX - slidesSliderBar.width / 2).coerceIn(0f,
                                maxSliderX.toFloat()
                            )
                            slidesSliderBar.translationX = newX

                            val maxScrollX = rvSlides.computeHorizontalScrollRange() - rvSlides.computeHorizontalScrollExtent()
                            val scrollX = (newX / maxSliderX) * maxScrollX

                            rvSlides.scrollBy((scrollX - rvSlides.computeHorizontalScrollOffset()).toInt(), 0)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        slidesViewModel.isDraggingSlides = false
                        true
                    }

                    else -> false
                }
            }

            photoSliderBar.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        slidesViewModel.isDraggingPhotos = true
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (slidesViewModel.isDraggingPhotos) {
                            val parentWidth = (photoSliderBar.parent as View).width
                            val maxSliderX = parentWidth - photoSliderBar.width

                            val newX = (event.rawX - photoSliderBar.width / 2).coerceIn(0f,
                                maxSliderX.toFloat()
                            )
                            photoSliderBar.translationX = newX

                            val maxScrollX = rvPhoto.computeHorizontalScrollRange() - rvPhoto.computeHorizontalScrollExtent()
                            val scrollX = (newX / maxSliderX) * maxScrollX

                            rvPhoto.scrollBy((scrollX - rvPhoto.computeHorizontalScrollOffset()).toInt(), 0)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        slidesViewModel.isDraggingPhotos = false
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun generateInitials(name: String): String {
        val cleanName = name.replace(Regex("^dr\\.?", RegexOption.IGNORE_CASE), "").trim()
        val words = cleanName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.isEmpty() -> ""
            words.size == 1 -> words[0].firstOrNull()?.uppercase() ?: ""
            else -> "${words[0].first().uppercase()}${words[1].first().uppercase()}"
        }
    }

    private fun animateActiveSlidesCounter(isOpen: Boolean) {
        val tvActiveCount = binding.tvActiveSlidesCount
        val btnOpenViewer = binding.btnOpenViewer

        if (isOpen) {
            tvActiveCount.visibility = View.VISIBLE
            btnOpenViewer.visibility = View.VISIBLE

            ObjectAnimator.ofFloat(tvActiveCount, "translationX", 0f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(btnOpenViewer, "translationX", 0f).apply {
                duration = 300
                start()
            }
        } else {
            ObjectAnimator.ofFloat(tvActiveCount, "translationX", 10f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(btnOpenViewer, "translationX", 10f).apply {
                duration = 300
                start()
            }

            tvActiveCount.postDelayed({ tvActiveCount.visibility = View.INVISIBLE }, 300)
            btnOpenViewer.postDelayed({ btnOpenViewer.visibility = View.INVISIBLE }, 300)
        }
    }

    private fun animateRightMenu(isOpen: Boolean) {
        val menu = binding.layoutRightMenu
        val shadow = binding.layoutMenuShadow
        val diopters = binding.layoutDiopters
        val doctors = binding.layoutDoctor

        if (isOpen) {
            menu.visibility = View.VISIBLE
            shadow.visibility = View.VISIBLE
            diopters.visibility = View.GONE
            doctors.visibility = View.GONE

            ObjectAnimator.ofFloat(menu, "translationX", 0f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(shadow, "translationX", 0f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(diopters, "translationX", 0f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(doctors, "translationX", 0f).apply {
                duration = 300
                start()
            }
        } else {
            ObjectAnimator.ofFloat(menu, "translationX", 400f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(shadow, "translationX", 400f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(diopters, "translationX", 10f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(doctors, "translationX", 10f).apply {
                duration = 300
                start()
            }

            menu.postDelayed({ menu.visibility = View.GONE }, 300)
            shadow.postDelayed({ shadow.visibility = View.GONE }, 300)
            diopters.postDelayed({ diopters.visibility = View.VISIBLE }, 300)
            doctors.postDelayed({ doctors.visibility = View.VISIBLE }, 300)
        }
    }


    companion object {
        const val EXTRA_CASE_RECORD = "extra_case_record"
    }
}