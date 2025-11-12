package com.tevioapp.vendor.utility.transform

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(private val customTypeface: Typeface) : TypefaceSpan("") {
    override fun updateDrawState(ds: TextPaint) = applyCustomTypeFace(ds)
    override fun updateMeasureState(paint: TextPaint) = applyCustomTypeFace(paint)

    private fun applyCustomTypeFace(paint: Paint) {
        paint.typeface = customTypeface
    }
}