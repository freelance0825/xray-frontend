package com.example.xray_frontend.ui.slidesdetail.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.example.xray_frontend.R
import androidx.core.content.withStyledAttributes

class AccordionView(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    // variables
    var isOpen = false
    private var isClicked = false

    // UI elements
    private val ivIcon: ImageView
    private val icon: ImageView
    private val titleTv: TextView
    private val titleHolder: LinearLayout
    private val textHolder: FrameLayout

    // accordion properties
    private var _titleIcon: Drawable? = null
    private var _title = ""
    private var _text = ""
    private var _textSize = 18.toFloat()
    private var _titleSize = 22.toFloat()
    private var _textColor = 0XFFaaaaaa.toInt()
    private var _titleColor = 0XFF000000.toInt()

    var titleIcon: Drawable?
        get() = _titleIcon
        set(value) {
            _titleIcon = value
            ivIcon.setImageDrawable(value)
        }

    var title = _title
        get() {
            return _title
        }
        set(value) {
            field = value
            _title = value
            titleTv.text = value
        }

    // colors for title and text
    var titleColor = _titleColor
        get() {
            return _titleColor
        }
        set(@ColorInt colorInt) {
            field = colorInt
            _titleColor = colorInt
            titleTv.setTextColor(colorInt)
        }

    var textColor = _textColor
        get() {
            return _textColor
        }
        set(@ColorInt colorInt) {
            field = colorInt
            _textColor = colorInt
        }

    // size of title and text
    var titleSize = _titleSize
        get() {
            return _titleSize
        }
        set(value) {
            field = value
            _titleSize = value
            titleTv.textSize = value
        }

    var textSize = _textSize
        get() {
            return _textSize
        }
        set(value) {
            field = value
            _textSize = value
        }

    init {
        context.withStyledAttributes(attributeSet, R.styleable.Accordion, 0, 0) {

            _titleIcon = getDrawable(R.styleable.Accordion_titleIcon)

            _title = getString(R.styleable.Accordion_title) ?: _title
            _text = getString(R.styleable.Accordion_text) ?: _text

            _titleSize = getDimension(R.styleable.Accordion_titleSize, _titleSize)
            _textSize = getDimension(R.styleable.Accordion_textSize, _textSize)

            _titleColor = getColor(R.styleable.Accordion_titleColor, _titleColor)
            _textColor = getColor(R.styleable.Accordion_textColor, _textColor)
        }

        // inflate the default accordion layout
        inflate(context, R.layout.base_accordion, this)

        ivIcon = findViewById(R.id.iv_icon)
        titleTv = findViewById(R.id.title)
        icon = findViewById(R.id.arrow)

        icon.layoutParams.height = titleSize.toInt()
        icon.layoutParams.width = titleSize.toInt()
        icon.requestLayout()

        // set the styles of the values as passed
        titleTv.textSize = _titleSize

        titleTv.setTextColor(_titleColor)

        ivIcon.setImageDrawable(_titleIcon)

        titleTv.text = _title

        titleHolder = findViewById(R.id.titleHolder)
        textHolder = findViewById(R.id.textHolder)

        textHolder.visibility = View.GONE

        // animate the text when title is clicked
        titleHolder.setOnClickListener {
            handleAccordion()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // Pindahkan hanya jika ada child selain yang sudah diinflate dari XML
        if (childCount > 1) {
            val childViews = mutableListOf<View>()

            // Simpan child yang bukan bagian dari layout XML
            for (i in 1 until childCount) { // Mulai dari 1 agar tidak mengambil textHolder
                childViews.add(getChildAt(i))
            }

            // Hapus hanya child yang dipindahkan
            childViews.forEach { removeView(it) }

            // Masukkan child ke dalam textHolder
            childViews.forEach { textHolder.addView(it) }
        }
    }

    // open/close the accordion on title press
    fun handleAccordion() {
        if (!isClicked) {
            isClicked = true

            textHolder.visibility = if (isOpen) View.GONE else View.VISIBLE
            turn(icon)

            isOpen = !isOpen
            isClicked = false
        }
    }

    // flips any view upside down/downside up
    private fun turn(view: View) {
        if (!isOpen) view.animate().rotation(180f).start()
        else view.animate().rotation(0f).start()
    }
}