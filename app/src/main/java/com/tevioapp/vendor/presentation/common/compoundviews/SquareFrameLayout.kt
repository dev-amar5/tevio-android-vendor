package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.tevioapp.vendor.R

class SquareFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var adjustSide: AdjustSide = AdjustSide.BY_WIDTH
        set(value) {
            field = value
            invalidate()
        }

    init {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SquareFrameLayout, 0, 0)
        adjustSide = attributes.getInteger(
            R.styleable.SquareFrameLayout_sl_adjust_side, adjustSide.value
        ).toAdjustSide()

        attributes.recycle()
    }

    private fun Int.toAdjustSide(): AdjustSide = when (this) {
        1 -> AdjustSide.BY_WIDTH
        2 -> AdjustSide.BY_HEIGHT
        else -> throw IllegalArgumentException("This value is not supported for Adjustside: $this")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (adjustSide) {
            AdjustSide.BY_WIDTH -> super.onMeasure(widthMeasureSpec, widthMeasureSpec)
            AdjustSide.BY_HEIGHT -> super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * ProgressDirection enum class to set the direction of the progress in progressBar
     */
    enum class AdjustSide(val value: Int) {
        BY_WIDTH(1), BY_HEIGHT(2)
    }
}