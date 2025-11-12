package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ViewLottieButtonBinding

class LottieButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLottieButtonBinding.inflate(LayoutInflater.from(context), this)
    private var isLoading = false
    private var buttonText: String? = null
    private var buttonStyleRes: Int = R.style.ButtonSolid_Icon

    @DrawableRes
    private var iconRes: Int = 0

    @ColorInt
    private var lottieTint: Int = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.LottieButton) {
            buttonText = getString(R.styleable.LottieButton_lb_button_text)
            buttonStyleRes =
                getResourceId(R.styleable.LottieButton_lb_button_style, R.style.ButtonSolid)
            iconRes = getResourceId(R.styleable.LottieButton_lb_icon_res, 0)
            lottieTint = getColor(
                R.styleable.LottieButton_lb_lottie_tint,
                ContextCompat.getColor(context, R.color.white)
            )
        }
        applyStyle()
    }

    private fun applyStyle() {
        binding.btnMain.apply {
            setTextAppearance(context, buttonStyleRes)
            text = buttonText
            setIconResource(iconRes)
        }
        binding.lottieLoader.setColorFilter(lottieTint)
    }

    fun setButtonText(text: String?) {
        buttonText = text
        binding.btnMain.text = buttonText
    }

    fun setIcon(@DrawableRes resId: Int) {
        iconRes = resId
        binding.btnMain.setIconResource(resId)
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            binding.btnMain.apply {
                text = null
                setIconResource(0)
            }
            binding.lottieLoader.apply {
                isVisible = true
                playAnimation()
            }
        } else {
            binding.btnMain.apply {
                text = buttonText
                setIconResource(iconRes)
            }
            binding.lottieLoader.apply {
                isVisible = false
                cancelAnimation()
            }
        }
    }

    fun isLoading(): Boolean = isLoading
}
