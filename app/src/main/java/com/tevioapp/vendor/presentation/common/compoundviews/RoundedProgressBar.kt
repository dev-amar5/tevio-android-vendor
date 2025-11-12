package com.tevioapp.vendor.presentation.common.compoundviews

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import com.tevioapp.vendor.R
class RoundedProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.progressBarStyleHorizontal
) : ProgressBar(context, attrs, defStyleAttr) {
    private var progressAnimated: Int = 0
        set(value) {
            field = value
            super.setProgress(value)
            invalidate()
        }
    private var cornerRadius: Float = 0f
    private var bgColor: Int = 0xFFE0E0E0.toInt()
    private var fgColor: Int = 0xFF4CAF50.toInt()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    init {
        isIndeterminate = false
        max = 100

        context.obtainStyledAttributes(attrs, R.styleable.RoundedProgressBar).apply {
            cornerRadius = getDimension(R.styleable.RoundedProgressBar_rp_cornerRadius, 0f)
            bgColor = getColor(R.styleable.RoundedProgressBar_rp_backgroundColor, bgColor)
            fgColor = getColor(R.styleable.RoundedProgressBar_rp_foregroundColor, fgColor)
            recycle()
        }

        bgPaint.color = bgColor
        fgPaint.color = fgColor
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        invalidate()
    }


    fun setProgressWithAnimation(target: Int, duration: Long = 1500L) {
        ObjectAnimator.ofInt(this, "progressAnimated", progress, target).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    fun setBackgroundColorInt(@ColorInt color: Int) {
        bgColor = color
        bgPaint.color = color
        invalidate()
    }

    fun setForegroundColorInt(@ColorInt color: Int) {
        fgColor = color
        fgPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val r = if (cornerRadius > 0) cornerRadius else height / 2f

        val fullRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(fullRect, r, r, bgPaint)

        val pct = progress.toFloat() / max.toFloat()
        val pw = (width * pct).coerceAtLeast(r)

        val progressRect = RectF(0f, 0f, pw, height.toFloat())
        canvas.drawRoundRect(progressRect, r, r, fgPaint)
    }
}
