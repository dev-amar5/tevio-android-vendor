package com.tevioapp.vendor.presentation.common.base.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.utility.extensions.addBlurEffect
import com.tevioapp.vendor.utility.extensions.removeBlurEffect


open class BaseCustomDialog<B : ViewDataBinding>(
    private val mContext: Context,
    @LayoutRes private val layoutId: Int,
    private val effect: Effect? = null,
    private val isFullScreen: Boolean = false,
    private var listener: Listener<B>? = null
) : Dialog(mContext, R.style.Dialog) {

    private lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, null, false)
        setContentView(binding.root)

        // Notify listener
        listener?.let {
            it.attachDialog(this) // Attach the dialog reference
            binding.setVariable(BR.callback, it)
            it.onViewCreated(binding)
        }
        if (isFullScreen) {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val marginInPixels = (20 * context.resources.displayMetrics.density).toInt()
            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels)
            binding.root.layoutParams = layoutParams
        }
        if (effect != Effect.DIM) {
            window?.setDimAmount(0f)
        }

        setOnDismissListener {
            (mContext as? Activity)?.removeBlurEffect()
        }

        if (effect == Effect.BLUR) {
            (mContext as? Activity)?.addBlurEffect(R.color.dialog_bg_tint)
        }
    }

    fun getViewBinding(): B? {
        return if (::binding.isInitialized) binding else null
    }

    // Listener class with dismiss support
    abstract class Listener<B : ViewDataBinding> {
        var dialog: BaseCustomDialog<B>? = null

        internal fun attachDialog(dialog: BaseCustomDialog<B>) {
            this.dialog = dialog
        }

        fun dismissDialog() {
            dialog?.dismiss()
        }

        open fun onClick(view: View) {}
        open fun onViewCreated(binding: B) {}
    }
}

fun BaseCustomDialog<*>?.isDialogVisible(): Boolean {
    return this?.isShowing == true
}

