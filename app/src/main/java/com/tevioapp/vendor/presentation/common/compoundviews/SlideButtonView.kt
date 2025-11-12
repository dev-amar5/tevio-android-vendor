package com.tevioapp.vendor.presentation.common.compoundviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ViewSlideButtonBinding
import com.tevioapp.vendor.utility.extensions.animateVisibility

@SuppressLint("ClickableViewAccessibility")
class SlideButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = ViewSlideButtonBinding.inflate(LayoutInflater.from(context), this, true)
    private var downX = 0f
    private val paddingHorizontal = context.resources.getDimensionPixelSize(R.dimen._5sdp).toFloat()
    private var isSliding = false
    private var isButtonEnabled = true

    private var mSlideComplete: (() -> Unit)? = null
    private var mSlideStatusChange: ((Boolean) -> Unit)? = null

    init {
        binding.ivSlider.setOnTouchListener { _, event ->
            if (!isButtonEnabled) return@setOnTouchListener false // Ignore touch when disabled

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    isSliding = true
                    mSlideStatusChange?.invoke(true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isSliding) return@setOnTouchListener true
                    val moveX = event.rawX
                    val deltaX = (moveX - downX).toInt()
                    val newX = binding.ivSlider.x + deltaX
                    val limit = (width - binding.ivSlider.width - 16).toFloat()

                    if (newX in 0f..limit) {
                        binding.ivSlider.x = newX
                        downX = moveX
                        binding.text.alpha = 1 - (binding.ivSlider.x / limit)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    isSliding = false
                    mSlideStatusChange?.invoke(false)
                    val threshold = width * 0.80
                    if (binding.ivSlider.x >= threshold) {
                        completeSlide()
                    } else {
                        resetSlider()
                    }
                    true
                }

                else -> false
            }
        }
    }

    fun setOnSlideComplete(listener: () -> Unit) {
        mSlideComplete = listener
    }

    fun setOnSlideStatusChange(listener: (Boolean) -> Unit) {
        mSlideStatusChange = listener
    }

    private fun completeSlide() {
        val endX = (width - binding.ivSlider.width - paddingHorizontal).toFloat()
        binding.ivSlider.animate().x(endX).setDuration(100).withEndAction {
            mSlideComplete?.invoke()
        }.start()
    }

    fun resetSlider(animate: Boolean = true) {
        if (animate) {
            binding.ivSlider.animate().x(paddingHorizontal).setDuration(200).withEndAction {
                binding.text.alpha = 1f
            }.start()
        } else {
            binding.ivSlider.x = paddingHorizontal
            binding.text.alpha = 1f
        }
    }

    fun setText(text: String) {
        binding.text.text = text
    }

    fun setLoading(loading: Boolean, animate: Boolean = true) = with(binding) {
        if (loading) {
            lottie.playAnimation()
        } else {
            lottie.pauseAnimation()
        }
        binding.pbOne.animateVisibility(loading, if (animate) 500L else 0)
    }

    /** Enable or disable the slider **/
    fun setSlideEnabled(enabled: Boolean) {
        isButtonEnabled = enabled
        binding.ivSlider.isEnabled = enabled
        binding.main.alpha = if (enabled) 1f else 0.5f
        if (enabled) {
            setSlideTextColor(Color.WHITE)
            setSlideBackgroundColor(ContextCompat.getColor(context, R.color.orange))
        } else {
            setSlideTextColor(Color.BLACK)
            setSlideBackgroundColor(ContextCompat.getColor(context, R.color.gray))
        }
    }

    fun setSlideTextColor(@ColorInt textColor: Int) {
        binding.text.setTextColor(textColor)
    }

    fun setSlideBackgroundColor(@ColorInt backgroundColor: Int) {
        binding.main.setCardBackgroundColor(backgroundColor)
    }

    fun isSlideEnabled(): Boolean = isButtonEnabled
}
