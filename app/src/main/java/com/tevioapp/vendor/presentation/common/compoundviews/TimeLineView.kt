package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ItemTimelineStepBinding

class TimeLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val colorOrange = ContextCompat.getColor(context, R.color.orange)
    private val colorBlack = ContextCompat.getColor(context, R.color.black)
    private val colorGray = "#E0E1E7".toColorInt()

    init {
        orientation = VERTICAL
        if (isInEditMode) {

            setStepList(
                listOf(
                    TimelineStep(
                        R.drawable.ic_shop,
                        buildSpannedString { append("Alian Resto") },
                        buildSpannedString {
                            append("Order Prepared by")
                        },
                        true
                    ), TimelineStep(
                        R.drawable.ic_shop,
                        buildSpannedString { append("Alian Resto") },
                        buildSpannedString {
                            append("Order Prepared by")
                        },
                        false
                    )
                )
            )
        }
    }

    fun setStepList(steps: List<TimelineStep>) {
        removeAllViews()
        var lastCompleted = false
        steps.forEachIndexed { index, step ->
            val binding = ItemTimelineStepBinding.inflate(LayoutInflater.from(context), this, false)
            binding.tvTitle.text = step.title
            binding.tvDesc.text = step.subtitle
            binding.lineTop.isVisible = index != 0
            binding.lineBottom.isVisible = index != steps.lastIndex
            binding.stepIcon.setImageResource(step.iconRes)
            if (step.isCompleted) {
                binding.stepIcon.setColorFilter(Color.WHITE)
                binding.stepIcon.setBackgroundResource(R.drawable.bg_circle_solid)
                binding.stepIcon.setBackgroundTintList(ColorStateList.valueOf(colorOrange))
                binding.lineTop.setBackgroundColor(colorOrange)
                binding.lineBottom.setBackgroundColor(colorOrange)
            } else {
                binding.stepIcon.background = null
                binding.stepIcon.setColorFilter(colorBlack)
                if (lastCompleted) binding.lineTop.setBackgroundColor(colorOrange)
                else binding.lineTop.setBackgroundColor(colorGray)
                binding.lineBottom.setBackgroundColor(colorGray)
            }
            lastCompleted = step.isCompleted
            addView(binding.root)
        }
    }


    data class TimelineStep(
        @DrawableRes val iconRes: Int,
        var title: SpannedString? = null,
        var subtitle: SpannedString? = null,
        var isCompleted: Boolean = false
    )
}