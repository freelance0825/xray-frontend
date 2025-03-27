package com.example.thunderscope_frontend.ui.slidesdetail.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    private val shapes = mutableListOf<Shape>()
    private var currentShape: Shape? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentShape = Shape(RectF(x, y, x, y), "Label ${shapes.size + 1}")
            }
            MotionEvent.ACTION_MOVE -> {
                currentShape?.rect?.right = x
                currentShape?.rect?.bottom = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentShape?.let { shapes.add(it) }
                currentShape = null
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        shapes.forEach { shape ->
            canvas.drawRect(shape.rect, paint)
            canvas.drawText(shape.label, shape.rect.left, shape.rect.top - 10, textPaint)
        }
        currentShape?.let { shape ->
            canvas.drawRect(shape.rect, paint)
            canvas.drawText(shape.label, shape.rect.left, shape.rect.top - 10, textPaint)
        }
    }

    fun clearCanvas() {
        shapes.clear()
        invalidate()
    }
}

data class Shape(val rect: RectF, val label: String)
