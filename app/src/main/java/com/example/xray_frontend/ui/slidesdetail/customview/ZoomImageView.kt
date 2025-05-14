package com.example.xray_frontend.ui.slidesdetail.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.OverScroller
import android.widget.Toast
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.xray_frontend.R
import com.google.android.material.button.MaterialButton
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

open class ZoomImageView : androidx.appcompat.widget.AppCompatImageView {

    private val savedAnnotations = mutableListOf<AnnotationData>()

    private var selectedAnnotationLabels: List<String> = emptyList()

    private var originalBitmap: Bitmap? = null

    var onAnnotationImageSaved: ((Bitmap, String) -> Unit)? = null

    /**
     * Get the currently displayed image as Bitmap.
     */
    fun getImageBitmap(): Bitmap? {
        return originalBitmap
    }

    private var annotationCreateView: View? = null

    private val mappedFixedAnnotationLabel = mutableMapOf<Shape, String>()

    var isDrawingActive = false

    @SuppressLint("ClickableViewAccessibility")
    private fun showAnnotationOverlay() {
        val shape = selectedShape ?: return removeAnnotationOverlay()
        val parentView = parent as? ViewGroup ?: return

        if (!mappedFixedAnnotationLabel.containsKey(shape)) {
            if (annotationCreateView == null) {
                annotationCreateView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_create_annotation, parentView, false)
                parentView.addView(annotationCreateView)
            }
            annotationCreateView?.apply {
                visibility = View.VISIBLE
                isDrawingActive = true
                findViewById<MaterialButton>(R.id.btn_save).setOnClickListener { saveAnnotation() }
                findViewById<MaterialButton>(R.id.btn_cancel).setOnClickListener { removeAnnotationOverlay() }
                findViewById<ImageView>(R.id.btn_close).setOnClickListener { removeAnnotationOverlay() }
            }
            positionAnnotationOverlay(annotationCreateView)
        }
    }

    private fun saveAnnotation() {
        val shape = selectedShape ?: return
        val edAnnotationName =
            annotationCreateView?.findViewById<EditText>(R.id.ed_annotation_name) ?: return
        val labelName = edAnnotationName.text.toString()

        if (labelName.isNotEmpty()) {
            mappedFixedAnnotationLabel[shape] = labelName
            annotationCreateView?.visibility = View.GONE
            edAnnotationName.text.clear()
            isDrawingActive = false

            updateOriginalBitmapWithAnnotations()

            invalidate()
        } else {
            Toast.makeText(context, "Label cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun positionAnnotationOverlay(view: View?) {
        val boundingBox = selectedShape?.let { getBoundingBox(it) } ?: return
        val overlayX = boundingBox.right + 25f
        val overlayY = boundingBox.top

        view?.apply {
            x = overlayX
            y = overlayY
        }
    }

    fun removeAnnotationOverlay() {
        isDrawingActive = false
        annotationCreateView?.visibility = View.GONE
        clearLastDrawing()
    }

    private fun updateOriginalBitmapWithAnnotations() {
        originalBitmap?.let { baseBitmap ->
            val tempBitmap = baseBitmap

            val parentHeight = (parent as? View)?.height ?: return
            val parentWidth = (parent as? View)?.width ?: return

            val aspectRatio = baseBitmap.width.toFloat() / baseBitmap.height
            val targetWidth = (aspectRatio * parentHeight).toInt()

            val newBitmapForParent =
                Bitmap.createBitmap(parentWidth, parentHeight, Bitmap.Config.ARGB_8888)
            val canvasParent = Canvas(newBitmapForParent)

            val newBitmap = Bitmap.createBitmap(targetWidth, parentHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)

            val scale = parentHeight.toFloat() / baseBitmap.height
            val scaleMatrix = Matrix().apply {
                setScale(scale, scale)
            }

            canvas.drawBitmap(baseBitmap, scaleMatrix, null)
            canvasParent.drawBitmap(newBitmap, (parentWidth - targetWidth) / 2f, 0f, null)

            var firstLabel = ""

            // --- Prepare inverse matrix to map zoomed coordinates back to image coordinates ---
            val inverseMatrix = Matrix()
            zoomMatrix.invert(inverseMatrix)

            val newAnnotation = AnnotationData()

            // --- Draw all shapes using inverse mapped coordinates ---
            shapes.forEach { shape ->
                val mappedShape = mapShapeToOriginal(shape, inverseMatrix)
                newAnnotation.shape = mappedShape

                drawShape(canvasParent, mappedShape)
            }

            // --- Draw all fixed labels using mapped coordinates ---
            mappedFixedAnnotationLabel.forEach { (shape, label) ->
                val mappedShape = mapShapeToOriginal(shape, inverseMatrix)
                val boundingBox = getBoundingBox(mappedShape)

                val textX = boundingBox.right + 25f
                val textY = boundingBox.centerY()
                firstLabel = label

                newAnnotation.label = label
                newAnnotation.labelX = textX
                newAnnotation.labelY = textY

                drawAnnotationLabel(canvasParent, label, textX, textY)
            }

            savedAnnotations.add(newAnnotation)

            val cropWidth = targetWidth
            val cropHeight = parentHeight
            val cropStartX = (parentWidth / 2) - (targetWidth / 2)

            val croppedBitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
            val croppedCanvas = Canvas(croppedBitmap)

            val cropRect = Rect(
                cropStartX.toInt(), 0,
                (cropStartX + cropWidth).toInt(), cropHeight
            )
            val destinationRectF = RectF(0f, 0f, cropWidth.toFloat(), cropHeight.toFloat())

            croppedCanvas.drawBitmap(newBitmapForParent, cropRect, destinationRectF, null)

            originalBitmap = croppedBitmap

            onAnnotationImageSaved?.invoke(croppedBitmap, firstLabel)

            clearLastDrawing(tempBitmap)
        }
    }

    fun redrawAnnotationsByLabels(labelNames: List<String>) {
        selectedAnnotationLabels = labelNames
        invalidate()
    }

    private fun mapShapeToOriginal(shape: Shape, inverseMatrix: Matrix): Shape {
        // Transform rect
        val rectPoints = floatArrayOf(
            shape.rect.left, shape.rect.top,
            shape.rect.right, shape.rect.bottom
        )
        inverseMatrix.mapPoints(rectPoints)

        val mappedRect = RectF(
            rectPoints[0], rectPoints[1],
            rectPoints[2], rectPoints[3]
        )

        // Transform path if exists
        val mappedPath = shape.path?.map { point ->
            val pts = floatArrayOf(point.x, point.y)
            inverseMatrix.mapPoints(pts)
            PointF(pts[0], pts[1])
        }?.toMutableList()

        return shape.copy(rect = mappedRect, path = mappedPath)
    }

    private val textPaint = Paint()
    private val zoomMatrix = Matrix()
    private val baseMatrix = Matrix()
    private val preEventImgRect = RectF()
    private val matrixValues = FloatArray(9)
    private val zoomInterpolator = AccelerateDecelerateInterpolator()
    private var logText = ""
    private var handlingDismiss = false
    private var touchSlop: Float = 0F
    private var oldScale = MIN_SCALE
    private var panAnimator: ValueAnimator? = null
    private var zoomAnimator: ValueAnimator? = null
    private var onClickListener: OnClickListener? = null
    private var onLongClickListener: OnLongClickListener? = null
    private var viewWidth = right - left - paddingLeft - paddingRight
    private var viewHeight = bottom - top - paddingTop - paddingBottom
    private lateinit var scroller: OverScroller
    private lateinit var tapDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector
    var debugInfoVisible = false
    var swipeToDismissEnabled = false
    var disallowPagingWhenZoomed = false
    var onDismiss: () -> Unit = {}
    var onDrawableLoaded: () -> Unit = {}
    var dismissProgressListener: (progress: Float) -> Unit = {}

    private var lastScale = 1f
    private var lastX = 0f
    private var lastY = 0f

    private val boundingBoxPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 5f)
    }

    private val path = Path()


    private var selectedColor: Int = ContextCompat.getColor(context, R.color.blue_lines)

    private val paint = Paint().apply {
        color = selectedColor
        style = Paint.Style.STROKE
        strokeWidth = 15f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    fun setPaintColor(color: Int) {
        selectedColor = color
        paint.color = selectedColor
        invalidate()
    }

    private var resetRequested = false
    private var firstInitialized = true

    private var drawingEnabled = false
    private val drawPaths =
        mutableListOf<Pair<Path, Paint>>()
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val inverseMatrix = Matrix()

    private fun getBitmapDisplayRect(): RectF? {
        originalBitmap?.let {
            val src = RectF(0f, 0f, it.width.toFloat(), it.height.toFloat())
            val dst = RectF()
            zoomMatrix.mapRect(dst, src)
            return dst
        }
        return null
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        initTextPaint()
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, scaleListener)
        scroller = OverScroller(context, DecelerateInterpolator())
        tapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                oldScale = currentScale
                val scaleFactor = if (currentScale != MIN_SCALE) MIN_SCALE else MID_SCALE
                setScaleAbsolute(scaleFactor, e.x, e.y)
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onClickListener?.onClick(this@ZoomImageView)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                onLongClickListener?.onLongClick(this@ZoomImageView)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (scaleDetector.isInProgress) return false
                val xAbs = distanceX.absoluteValue
                val yAbs = distanceY.absoluteValue
                if (currentScale <= MIN_SCALE) {
                    if (swipeToDismissEnabled && yAbs > xAbs) {
                        handlingDismiss = true
                        panImage(0F, distanceY)
                        dismissProgressListener.invoke(dismissProgress)
                    }
                } else {
                    panImage(distanceX, distanceY)
                }
                var disallowParentIntercept = true
                if (!disallowPagingWhenZoomed) {
                    if (handlingDismiss) {
                        disallowParentIntercept = true
                    } else if (xAbs > yAbs) {
                        if (distanceX > 0F && preEventImgRect.right == viewWidth.toFloat())
                            disallowParentIntercept = false
                        else if (distanceX < 0F && preEventImgRect.left == 0F)
                            disallowParentIntercept = false
                    } else {
                        if (distanceY > 0F && preEventImgRect.bottom == viewHeight.toFloat())
                            disallowParentIntercept = false
                        else if (distanceY < 0F && preEventImgRect.top == 0F)
                            disallowParentIntercept = false
                    }
                }
                parent?.requestDisallowInterceptTouchEvent(disallowParentIntercept)
                return (xAbs > touchSlop || yAbs > touchSlop)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (currentZoom <= MIN_SCALE) return false
                val maxX = (preEventImgRect.width() - viewWidth).toInt()
                val maxY = (preEventImgRect.height() - viewHeight).toInt()
                flingRunnable.lastX = -preEventImgRect.left
                flingRunnable.lastY = -preEventImgRect.top
                scroller.fling(
                    flingRunnable.lastX.toInt(), flingRunnable.lastY.toInt(), -velocityX.toInt(),
                    -velocityY.toInt(), 0, maxX, 0, maxY
                )
                ViewCompat.postOnAnimation(this@ZoomImageView, flingRunnable)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                removeCallbacks(flingRunnable)
                scroller.forceFinished(true)
                displayRect?.let {
                    preEventImgRect.set(it)
                }
                panAnimator?.removeAllUpdateListeners()
                panAnimator?.cancel()
                return true
            }
        })
    }

    private val flingRunnable = object : Runnable {
        var lastX = 0F
        var lastY = 0F
        override fun run() {
            if (!scroller.isFinished && scroller.computeScrollOffset()) {
                val curX = scroller.currX.toFloat()
                val curY = scroller.currY.toFloat()
                panImage((curX - lastX), (curY - lastY))
                lastX = curX
                lastY = curY
                ViewCompat.postOnAnimation(this@ZoomImageView, this)
            }
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        originalBitmap = bm

        // Reset zooming and translation when setting a new bitmap
        resetZoomAndPan()
    }


    private fun resetZoomAndPan() {
        setBounds()
        updateMatrix(drawMatrix)  // Apply the reset matrix
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (drawingEnabled) {
            return handleDrawing(event)
        }

        val disallowIntercept =
            currentScale > MIN_SCALE || scaleDetector.isInProgress || handlingDismiss
        if (event?.action == MotionEvent.ACTION_UP) {
            if (handlingDismiss) {
                if (currentTransY.absoluteValue > dismissThreshold) {
                    onDismiss.invoke()
                } else {
                    animatePan(0F, currentTransY, 0F, 0F, dismissProgress)
                }
            }
        }
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
        return tapDetector.onTouchEvent(event!!) || return scaleDetector.onTouchEvent(event) || return true
    }

    private fun mapTouchToImage(x: Float, y: Float): Pair<Float, Float> {
        val touchPoint = floatArrayOf(x, y)
        zoomMatrix.invert(inverseMatrix)
        inverseMatrix.mapPoints(touchPoint)
        return touchPoint[0] to touchPoint[1]
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleDrawing(event: MotionEvent?): Boolean {
        event ?: return false
        val (x, y) = mapTouchToImage(event.x, event.y)

        if (annotationCreateView?.visibility == View.VISIBLE) {
            removeAnnotationOverlay()
            return true // Prevent new shape creation
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedShape = findSelectedShape(x, y)

                selectedShape?.let {
                    resizeCornerIndex = getTouchedCornerIndex(it, x, y)
                    isResizing = resizeCornerIndex != -1
                    isMoving = !isResizing
                    lastTouchX = x
                    lastTouchY = y
                } ?: run {
                    when (currentMode) {
                        DrawMode.RECTANGLE -> {
                            currentShape = Shape(RectF(x, y, x, y), ShapeType.RECTANGLE)
                        }

                        DrawMode.CIRCLE -> {
                            currentShape = Shape(RectF(x, y, x, y), ShapeType.CIRCLE)
                        }

                        DrawMode.FREE_DRAW -> {
                            path.moveTo(x, y)
                            currentShape = Shape(
                                RectF(x, y, x, y),
                                ShapeType.FREE_DRAW,
                                mutableListOf(PointF(x, y))
                            )
                        }
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isResizing && selectedShape != null) {
                    resizeShape(selectedShape!!, x, y)
                } else if (isMoving && selectedShape != null) {
                    moveShape(selectedShape!!, x - lastTouchX, y - lastTouchY)
                    lastTouchX = x
                    lastTouchY = y
                } else {
                    currentShape?.let {
                        if (it.type == ShapeType.FREE_DRAW) {
                            it.path?.add(PointF(x, y))
                            updateFreeDrawBoundingBox(it)
                            path.quadTo(
                                lastTouchX,
                                lastTouchY,
                                (x + lastTouchX) / 2,
                                (y + lastTouchY) / 2
                            )
                        } else {
                            it.rect.right = x
                            it.rect.bottom = y
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selectedShape == null) {
                    currentShape?.let {
                        shapes.add(it)
                        if (it.type == ShapeType.FREE_DRAW) {
                            drawPaths.add(Pair(Path(path), Paint(paint)))
                            path.reset()
                        }
                    }
                    selectedShape = currentShape
                    currentShape = null
                }

                isResizing = false
                isMoving = false
                resizeCornerIndex = -1
            }
        }
        invalidate()
        return true
    }


    private fun setZoom(scale: Float, x: Float, y: Float) {
        zoomMatrix.postScale(scale, scale, x, y)

        lastScale = scale
        lastX = x
        lastY = y

        setBounds()
        updateMatrix(drawMatrix)
    }

    fun setZoom(scale: Float) {
        val drawable = drawable ?: return
        val intrinsicHeight = drawable.intrinsicHeight.toFloat()
        val viewHeight = height.toFloat()

        // Pastikan kita zoom ke bagian atas gambar
        val x = width / 2f  // Zoom tetap di tengah horizontal
        val y = (viewHeight / intrinsicHeight) * 0f  // Atas gambar

        zoomMatrix.postScale(scale, scale, x, y)

        lastScale = scale
        lastX = x
        lastY = y

        setBounds()
        updateMatrix(drawMatrix)
    }


    private fun updateMatrix(drawMatrix: Matrix) {
        logText = "tX: $currentTransX tY: $currentTransY"
        logText += " Scale: $currentScale"
        imageMatrix = drawMatrix
    }

    private fun setScale(scale: Float, x: Float, y: Float) {
        setZoom(scale, x, y)
    }

    private fun setScaleAbsolute(scale: Float, x: Float, y: Float) {
        val zoom = when {
            scale > MAX_SCALE -> MAX_SCALE
            scale < MIN_SCALE -> MIN_SCALE
            else -> scale
        }
        cancelAnimation()
        animateZoom(oldScale, zoom, x, y)
    }

    private inline val drawableWidth: Int
        get() = drawable?.intrinsicWidth ?: 0

    private inline val drawableHeight: Int
        get() = drawable?.intrinsicHeight ?: 0

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        drawable?.let {
            onDrawableLoaded.invoke()

            if (resetRequested || firstInitialized) {
                resetZoom()
                zoomMatrix.set(imageMatrix)
                firstInitialized = false
            } else {
                setZoom(lastScale, lastX, lastY)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewWidth = right - left - paddingLeft - paddingRight
        viewHeight = bottom - top - paddingTop - paddingBottom
        if (changed && resetRequested) resetZoom()
    }

    fun resetZoom() {
        val tempSrc = RectF(0F, 0F, drawableWidth.toFloat(), drawableHeight.toFloat())
        val tempDst = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        baseMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
        setScaleAbsolute(MIN_SCALE, viewWidth / 2F, viewHeight / 2F)
        imageMatrix = baseMatrix
        resetRequested = false
    }

    fun resetZoomManually() {
        resetRequested = true
        firstInitialized = true
        resetZoom()
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.scaleFactor.isNaN() || detector.scaleFactor.isInfinite())
                return false
            if (currentScale > MAX_SCALE && detector.scaleFactor > 1F)
                return false
            oldScale = currentScale
            setScale(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            oldScale = currentScale
            var needsReset = false
            var newScale = MIN_SCALE
            if (currentScale < MIN_SCALE) {
                newScale = MIN_SCALE
                needsReset = true
            }
            if (needsReset) setScaleAbsolute(newScale, detector.focusX, detector.focusY)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()

        if (selectedAnnotationLabels.isNotEmpty()) {
            Log.e("FTEST", "onDraw: cek ini ${selectedAnnotationLabels.size}", )

            selectedAnnotationLabels.forEach { a ->
                Log.e("FTEST", "-- selected:  ini ${a}", )
            }

            savedAnnotations.forEach { a ->
                Log.e("FTEST", "-- selectedSaved:  ini ${a.label}", )
            }
            val annotationsToDraw = savedAnnotations.filter { it.label in selectedAnnotationLabels }
            Log.e("FTEST", "onDraw: cek iniZZ ${annotationsToDraw.size}", )
            annotationsToDraw.forEach { annotation ->
                annotation.shape?.let {
                    Log.e("FTEST", "onDraw: cek iniZZ shape true ${it.type}", )

                    drawShape(canvas, it)
                    drawAnnotationLabel(
                        canvas,
                        annotation.label ?: "",
                        annotation.labelX ?: 0F,
                        annotation.labelY ?: 0F
                    )
                } ?: kotlin.run {
                    Log.e("FTEST", "onDraw: cek iniZZ shape false null", )
                }
            }
        } else {
            Log.e("FTEST", "onDraw: empty", )

            // Draw shapes
            shapes.forEach { shape ->
                drawShape(canvas, shape)
            }

            // Bounding box / selection overlay
            selectedShape?.let {
                showAnnotationOverlay()
//        drawBoundingBox(canvas, it)
            }

            // Draw annotation labels
            mappedFixedAnnotationLabel.forEach { (shape, label) ->
                val boundingBox = getBoundingBox(shape)
                val textX = boundingBox.right + 25f
                val textY = boundingBox.centerY()

                drawAnnotationLabel(canvas, label, textX, textY)
            }

            // Draw current shape in progress
            currentShape?.let { drawShape(canvas, it) }
        }

        canvas.restore()

        // Draw debug info (outside zoomMatrix transform)
        if (debugInfoVisible) {
            canvas.drawText(logText, 10F, height - 10F, textPaint)
            val drawableBound = displayRect?.let {
                "Drawable: $it"
            } ?: ""
            canvas.drawText(drawableBound, 10F, 40F, textPaint)
        }
    }


    private fun drawAnnotationLabel(
        canvas: Canvas,
        label: String,
        overlayX: Float,
        overlayY: Float
    ) {
        val cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        )
        val padding = 16f
        val textMargin = 12f

        val textPaintLabel = Paint().apply {
            color = ContextCompat.getColor(context, R.color.gray_opacity_80)
            textSize = 24f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        val textPaintAnnotation = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        // Measure text sizes
        val labelWidth = max(
            textPaintLabel.measureText("Label"),
            textPaintAnnotation.measureText(label)
        )
        val labelHeight = textPaintAnnotation.descent() - textPaintAnnotation.ascent()

        val boxWidth = labelWidth + (2 * padding)
        val boxHeight = (labelHeight * 2) + textMargin + (2 * padding)

        val boxLeft = overlayX
        val boxTop = overlayY - (boxHeight / 2)
        val boxRight = boxLeft + boxWidth + 50
        val boxBottom = boxTop + boxHeight

        // Background Paint (matches @drawable/bg_rounded_dialog)
        val rectPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.base_dialog) // Use your color
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw rounded rectangle background
        canvas.drawRoundRect(
            boxLeft, boxTop, boxRight, boxBottom,
            cornerRadius, cornerRadius, rectPaint
        )

        // Calculate text positions (gravity: start|center)
        val textStartX = boxLeft + padding
        val textCenterY = boxTop + (boxHeight / 2) - ((labelHeight + textMargin) / 2) + 12

        // Draw "Label" title
        canvas.drawText("Label", textStartX, textCenterY, textPaintLabel)

        // Draw annotation text (below "Label")
        canvas.drawText(
            label,
            textStartX,
            textCenterY + labelHeight + textMargin,
            textPaintAnnotation
        )
    }

    fun enableDrawing(enable: Boolean) {
        drawingEnabled = enable
    }

    private fun initTextPaint() {
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 40F
    }

    private fun animateZoom(startZoom: Float, endZoom: Float, x: Float, y: Float) {
        zoomAnimator = ValueAnimator.ofFloat(startZoom, endZoom).apply {
            duration = VALUE_ANIMATOR_DURATION
            addUpdateListener {
                val scale = (it.animatedValue as Float) / currentScale
                setZoom(scale, x, y)
            }
            interpolator = zoomInterpolator
            start()
        }
    }

    private fun animatePan(
        startX: Float, startY: Float, endX: Float, endY: Float, dismissProgress: Float? = null
    ) {
        panAnimator = ValueAnimator.ofFloat(startX, startY, endX, endY).apply {
            duration = VALUE_ANIMATOR_DURATION
            addUpdateListener {
                val newX = (startX - endX) * it.animatedFraction
                val newY = (startY - endY) * it.animatedFraction
                panImage(startX - newX, startY - newY, setAbsolute = true)
                dismissProgress?.let { progress ->
                    if (1.0F - it.animatedFraction < progress) {
                        dismissProgressListener.invoke(1.0F - it.animatedFraction)
                    }
                }
            }
            interpolator = zoomInterpolator
            start()
            doOnCancel {
                panImage(0F, 0F, setAbsolute = true)
                handlingDismiss = false
            }
            doOnEnd {
                handlingDismiss = false
            }
        }
    }

    private fun cancelAnimation() {
        zoomAnimator?.removeAllUpdateListeners()
        zoomAnimator?.cancel()
    }

    private fun panImage(x: Float, y: Float, setAbsolute: Boolean = false) {
        if (setAbsolute)
            zoomMatrix.setTranslate(x, y)
        else
            zoomMatrix.postTranslate(-x, -y)
        setBounds()
        updateMatrix(drawMatrix)
    }

    private fun setBounds() {
        val rect = displayRect ?: return
        val height = rect.height()
        val width = rect.width()
        val viewHeight: Int = this.viewHeight
        var deltaX = 0f
        var deltaY = 0f
        when {
            height <= viewHeight -> {
                if (!handlingDismiss)
                    deltaY = (viewHeight - height) / 2 - rect.top
            }

            rect.top > 0 -> {
                deltaY = -rect.top
            }

            rect.bottom < viewHeight -> {
                deltaY = viewHeight - rect.bottom
            }
        }
        val viewWidth: Int = this.viewWidth
        when {
            width <= viewWidth -> {
                deltaX = (viewWidth - width) / 2 - rect.left
            }

            rect.left > 0 -> {
                deltaX = -rect.left
            }

            rect.right < viewWidth -> {
                deltaX = viewWidth - rect.right
            }
        }
        zoomMatrix.postTranslate(deltaX, deltaY)
    }

    private inline val dismissThreshold: Float
        get() = viewHeight / 3F

    private inline val currentScale: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }

    private inline val currentTransX: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MTRANS_X]
        }

    private inline val currentTransY: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MTRANS_Y]
        }

    private inline val dismissProgress: Float
        get() = currentTransY.absoluteValue / dismissThreshold

    private val displayRect: RectF? = RectF()
        get() {
            drawable?.let { d ->
                field?.set(
                    0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat()
                )
                drawMatrix.mapRect(field)
                return field
            }
            return null
        }

    private val drawMatrix: Matrix = Matrix()
        get() {
            field.set(baseMatrix)
            field.postConcat(zoomMatrix)
            return field
        }

    var currentZoom: Float
        get() = currentScale
        set(value) {
            oldScale = currentScale
            setScaleAbsolute(value, viewWidth / 2F, viewHeight / 2F)
        }

    companion object {
        const val MAX_SCALE = 3F
        const val MIN_SCALE = 1F
        const val MID_SCALE = 1.75F
        private const val VALUE_ANIMATOR_DURATION = 300L
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.onClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        this.onLongClickListener = l
    }

    private val shapes = mutableListOf<Shape>()
    private var currentShape: Shape? = null
    private var selectedShape: Shape? = null
    private var currentMode: DrawMode = DrawMode.RECTANGLE

    private var isResizing = false
    private var isMoving = false
    private var resizeCornerIndex = -1
    private val handleSize = 60f

    private fun drawShape(canvas: Canvas, shape: Shape) {
        when (shape.type) {
            ShapeType.RECTANGLE -> canvas.drawRect(shape.rect, paint)
            ShapeType.CIRCLE -> {
                val radius = getRadius(shape.rect)
                canvas.drawCircle(shape.rect.centerX(), shape.rect.centerY(), radius, paint)
            }

            ShapeType.FREE_DRAW -> {
                val path = Path().apply {
                    shape.path?.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y)
                        else lineTo(point.x, point.y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawBoundingBox(canvas: Canvas, shape: Shape) {
        val boundingBox = getBoundingBox(shape)
        canvas.drawRect(boundingBox, boundingBoxPaint)

        val corners = arrayOf(
            PointF(boundingBox.left, boundingBox.top),
            PointF(boundingBox.right, boundingBox.top),
            PointF(boundingBox.left, boundingBox.bottom),
            PointF(boundingBox.right, boundingBox.bottom)
        )

        corners.forEach { point ->
            canvas.drawCircle(point.x, point.y, handleSize / 2, boundingBoxPaint)
        }
    }

    private fun getBoundingBox(shape: Shape): RectF {
        return when (shape.type) {
            ShapeType.CIRCLE -> {
                val radius = getRadius(shape.rect)
                RectF(
                    shape.rect.centerX() - radius,
                    shape.rect.centerY() - radius,
                    shape.rect.centerX() + radius,
                    shape.rect.centerY() + radius
                )
            }

            ShapeType.FREE_DRAW -> {
                updateFreeDrawBoundingBox(shape)
                shape.rect
            }

            else -> shape.rect
        }
    }

    private fun updateFreeDrawBoundingBox(shape: Shape) {
        shape.path?.let { path ->
            if (path.isNotEmpty()) {
                val minX = path.minOf { it.x }
                val minY = path.minOf { it.y }
                val maxX = path.maxOf { it.x }
                val maxY = path.maxOf { it.y }
                shape.rect.set(minX, minY, maxX, maxY)
            }
        }
    }

    private fun moveShape(shape: Shape, dx: Float, dy: Float) {
        shape.rect.offset(dx, dy)
        shape.path?.forEach { point ->
            point.x += dx
            point.y += dy
        }
    }

    private fun resizeShape(shape: Shape, x: Float, y: Float) {
        val oldBounds = RectF(shape.rect)
        when (resizeCornerIndex) {
            0 -> {
                shape.rect.left = x; shape.rect.top = y
            }

            1 -> {
                shape.rect.right = x; shape.rect.top = y
            }

            2 -> {
                shape.rect.left = x; shape.rect.bottom = y
            }

            3 -> {
                shape.rect.right = x; shape.rect.bottom = y
            }
        }

        if (shape.type == ShapeType.FREE_DRAW) {
            val scaleX = shape.rect.width() / oldBounds.width()
            val scaleY = shape.rect.height() / oldBounds.height()

            shape.path?.forEach { point ->
                point.x = oldBounds.left + (point.x - oldBounds.left) * scaleX
                point.y = oldBounds.top + (point.y - oldBounds.top) * scaleY
            }
        }
    }

    fun setDrawMode(mode: DrawMode) {
        currentMode = mode
    }

    fun clearLastDrawing(tempBitmapImage: Bitmap? = null) {
        tempBitmapImage?.let {
            originalBitmap = it
        }

        mappedFixedAnnotationLabel.remove(shapes.lastOrNull())
        shapes.removeLastOrNull()
        selectedShape = null
        invalidate()
    }

    fun clearCanvas(tempBitmapImage: Bitmap? = null) {
        tempBitmapImage?.let {
            originalBitmap = it
        }

        mappedFixedAnnotationLabel.clear()
        shapes.clear()
        selectedShape = null
        invalidate()
    }

    private fun findSelectedShape(x: Float, y: Float): Shape? {
        return shapes.find { getBoundingBox(it).contains(x, y) }
    }

    private fun getRadius(rect: RectF): Float {
        return sqrt((rect.right - rect.left).pow(2) + (rect.bottom - rect.top).pow(2)) / 2
    }

    private fun getTouchedCornerIndex(shape: Shape, x: Float, y: Float): Int {
        val boundingBox = getBoundingBox(shape)
        val corners = arrayOf(
            PointF(boundingBox.left, boundingBox.top),
            PointF(boundingBox.right, boundingBox.top),
            PointF(boundingBox.left, boundingBox.bottom),
            PointF(boundingBox.right, boundingBox.bottom)
        )
        return corners.indexOfFirst { point ->
            (x in point.x - handleSize..point.x + handleSize) &&
                    (y in point.y - handleSize..point.y + handleSize)
        }
    }

    enum class DrawMode { RECTANGLE, CIRCLE, FREE_DRAW }
}

data class Shape(
    val rect: RectF,
    val type: ShapeType,
    val path: MutableList<PointF>? = null
)

enum class ShapeType { RECTANGLE, CIRCLE, FREE_DRAW }

data class AnnotationData(
    var shape: Shape? = null,
    var label: String? = null,
    var labelX: Float? = null,
    var labelY: Float? = null,
)
