package com.tevioapp.vendor.utility.blurview

import com.tevioapp.vendor.utility.blurview.SizeScaler.Companion.ROUNDING_VALUE
import kotlin.math.ceil

/**
 * Scales width and height by [scaleFactor],
 * and then rounds the size proportionally so the width is divisible by [ROUNDING_VALUE]
 */
class SizeScaler(private val scaleFactor: Float) {
    fun scale(width: Int, height: Int): Size {
        val nonRoundedScaledWidth = downscaleSize(width.toFloat())
        val scaledWidth = roundSize(nonRoundedScaledWidth)
        //Only width has to be aligned to ROUNDING_VALUE
        val roundingScaleFactor = width.toFloat() / scaledWidth
        //Ceiling because rounding or flooring might leave empty space on the View's bottom
        val scaledHeight = ceil((height / roundingScaleFactor).toDouble()).toInt()

        return Size(scaledWidth, scaledHeight, roundingScaleFactor)
    }

    fun isZeroSized(measuredWidth: Int, measuredHeight: Int): Boolean {
        return downscaleSize(measuredHeight.toFloat()) == 0 || downscaleSize(measuredWidth.toFloat()) == 0
    }

    /**
     * Rounds a value to the nearest divisible by [.ROUNDING_VALUE] to meet stride requirement
     */
    private fun roundSize(value: Int): Int {
        if (value % ROUNDING_VALUE == 0) {
            return value
        }
        return value - (value % ROUNDING_VALUE) + ROUNDING_VALUE
    }

    private fun downscaleSize(value: Float): Int {
        return ceil((value / scaleFactor).toDouble()).toInt()
    }

    class Size(
        @JvmField val width: Int, @JvmField val height: Int, // TODO this is probably not needed anymore
        val scaleFactor: Float
    ) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val size = o as Size

            if (width != size.width) return false
            if (height != size.height) return false
            return java.lang.Float.compare(size.scaleFactor, scaleFactor) == 0
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + height
            result = 31 * result + (if (scaleFactor != +0.0f) java.lang.Float.floatToIntBits(
                scaleFactor
            ) else 0)
            return result
        }

        override fun toString(): String {
            return "Size{width=$width, height=$height, scaleFactor=$scaleFactor}"
        }
    }

    companion object {
        // Bitmap size should be divisible by ROUNDING_VALUE to meet stride requirement.
        // This will help avoiding an extra bitmap allocation when passing the bitmap to RenderScript for blur.
        // Usually it's 16, but on Samsung devices it's 64 for some reason.
        private const val ROUNDING_VALUE = 64
    }
}
