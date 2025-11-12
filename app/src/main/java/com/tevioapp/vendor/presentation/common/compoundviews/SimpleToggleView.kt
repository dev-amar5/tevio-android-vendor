package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Checkable
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ViewSimpleToggleBinding

class SimpleToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Checkable {

    private val binding = ViewSimpleToggleBinding.inflate(LayoutInflater.from(context), this)

    private var iconSrc: Int = 0
    private var tintColor: Int = 0
    private var iconPadding: Int = 0

    private var toggleListener: ((Boolean) -> Unit)? = null

    private var _isChecked: Boolean = false

    override fun isChecked(): Boolean = _isChecked

    override fun setChecked(checked: Boolean) {
        if (_isChecked != checked) {
            _isChecked = checked
            updateView(checked)
            toggleListener?.invoke(checked)
        }
    }

    override fun toggle() {
        isChecked = !_isChecked
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.SimpleToggleView) {
            iconSrc = getResourceId(R.styleable.SimpleToggleView_stv_icon_src, 0)
            iconPadding = getDimensionPixelSize(R.styleable.SimpleToggleView_stv_icon_padding, 0)
            tintColor = getColor(
                R.styleable.SimpleToggleView_stv_tint_color,
                ContextCompat.getColor(context, R.color.dark_green)
            )
            _isChecked = getBoolean(R.styleable.SimpleToggleView_stv_checked, false)
        }

        binding.cbToggle.setImageResource(iconSrc)
        binding.cbToggle.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        ViewCompat.setBackgroundTintList(binding.cbToggle, ColorStateList.valueOf(tintColor))

        binding.cbToggle.setOnClickListener {
            animateToggle(!_isChecked)
        }

        updateView(_isChecked)
    }

    private fun animateToggle(newState: Boolean) {
        binding.cbToggle.isEnabled = false
        _isChecked = newState

        animate().scaleX(0.9f).scaleY(0.9f).alpha(0.2f).setDuration(100).withEndAction {
            updateView(_isChecked)
            animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(100).withEndAction {
                binding.cbToggle.isEnabled = true
                toggleListener?.invoke(_isChecked)
            }.start()
        }.start()
    }

    private fun updateView(checked: Boolean) {
        if (checked) {
            binding.cbToggle.setBackgroundResource(R.drawable.bg_circle_solid)
            binding.cbToggle.setColorFilter(Color.WHITE)
        } else {
            binding.cbToggle.setBackgroundResource(R.drawable.bg_circle_only_stroke)
            binding.cbToggle.setColorFilter(tintColor)
        }
        refreshDrawableState()
    }

    fun setOnToggleListener(listener: (Boolean) -> Unit) {
        toggleListener = listener
    }
}
