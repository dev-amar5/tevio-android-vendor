package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes


class VoiceWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var amplitudes = IntArray(0) { 10 }

    private var barWidthPx = 0f
    private var barGapPx = 0f
    private var barCount = 0

    private var cornerRadius = 8f // px

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculateBarCount()
    }

    /** Set bar width and gap from dimension resources */
    fun setBarWidthAndGap(@DimenRes barWidthRes: Int, @DimenRes barGapRes: Int) {
        barWidthPx = resources.getDimension(barWidthRes)
        barGapPx = resources.getDimension(barGapRes)
        recalculateBarCount()
        invalidate()
    }

    /** Set corner radius in dp */
    fun setCornerRadius(radiusDp: Float) {
        cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, radiusDp, resources.displayMetrics
        )
        invalidate()
    }

    /** Set bar color dynamically */
    fun setBarColor(@ColorInt color: Int) {
        paint.color = color
        invalidate()
    }

    private fun recalculateBarCount() {
        if (barWidthPx + barGapPx > 0) {
            barCount = (width / (barWidthPx + barGapPx)).toInt()
            amplitudes = IntArray(barCount) { 10 }
        }
    }

    fun updateAmplitude(amp: Int) {
        val normalized = (amp / 4000f).coerceIn(0f, 1f)
        for (i in 0 until barCount - 1) {
            amplitudes[i] = amplitudes[i + 1]
        }
        amplitudes[barCount - 1] = (normalized * height).toInt().coerceAtLeast(10)
        invalidate()
    }
    /** Clear all wave amplitudes (reset to minimum) */
    fun clearWave() {
        if (barCount == 0) return
        amplitudes = IntArray(barCount) { 10 } // reset to baseline height
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        for (i in amplitudes.indices) {
            val barHeight = amplitudes[i] / 2f
            val left = i * (barWidthPx + barGapPx)
            val right = left + barWidthPx

            val rect = RectF(left, centerY - barHeight, right, centerY + barHeight)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }
}
