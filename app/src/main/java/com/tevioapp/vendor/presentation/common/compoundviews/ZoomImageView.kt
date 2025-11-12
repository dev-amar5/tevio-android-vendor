package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val matrixValues = FloatArray(9)
    private var scale = 1f
    private var minScale = 1f
    private var maxScale = 5f
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var mode = NONE

    private val matrixTransform = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                lastX = x
                lastY = y
                mode = DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val dx = x - lastX
                    val dy = y - lastY
                    matrixTransform.postTranslate(dx, dy)
                    imageMatrix = matrixTransform
                    lastX = x
                    lastY = y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            scale *= scaleFactor
            scale = max(minScale, min(scale, maxScale))
            matrixTransform.postScale(
                scaleFactor, scaleFactor, detector.focusX, detector.focusY
            )
            imageMatrix = matrixTransform
            return true
        }
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
    }
}
