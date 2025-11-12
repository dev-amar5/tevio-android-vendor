package com.tevioapp.vendor.utility.popups

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.tevioapp.vendor.BR

/**
 * Base Popup Menu
 */
class BasePopupWindow<B : ViewDataBinding>(
    context: Context,
    val getBinding: () -> B,
    val onCreate: ((binding: B) -> Unit)? = null,
    val onViewClick: ((binding: B, view: View) -> Unit)? = null
) : RelativePopupWindow(context) {

    private val callback = object : Callback {
        override fun onClick(view: View) {
            onViewClick?.invoke(binding, view)
        }
    }
    private var binding: B = getBinding.invoke()

    init {
        contentView = binding.root
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(null)
        binding.setVariable(BR.callback, callback)
        onCreate?.invoke(binding)
    }

    interface Callback {
        fun onClick(view: View)
    }

}
