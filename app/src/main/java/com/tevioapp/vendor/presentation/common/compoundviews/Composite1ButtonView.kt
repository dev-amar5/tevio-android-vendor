package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tevioapp.vendor.databinding.ViewCompositeButton1Binding


class Composite1ButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding = ViewCompositeButton1Binding.inflate(LayoutInflater.from(context), this, true)
    fun setText(text: String) {
        binding.tvLabel.text = text
    }
}

