package com.tevioapp.vendor.presentation.common.base.sheet

import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.presentation.common.base.BaseViewModel


/**
 * Base bott0m sheet
 */
class BaseSheet<B : ViewDataBinding>(
    @LayoutRes private val layout: Int,
) : BaseBottomSheet<B>() {
    lateinit var binding: B
    private var effect = Effect.BLUR
    private var listener: Listener<B>? = null
    override fun getLayoutResource(): Int {
        return layout
    }

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onSheetCreated(binding: B) {
        this.binding = binding
        binding.setVariable(BR.callback, listener)
        listener?.onViewCreated(binding)
    }


    fun setSheetListener(listener: Listener<B>): BaseSheet<B> {
        this.listener = listener
        listener.attachSheet(this)
        return this
    }

    fun setSheetCancelable(isCancelable: Boolean): BaseSheet<B> {
        this.isCancelable = isCancelable
        return this
    }

    fun setSheetEffect(effect: Effect): BaseSheet<B> {
        this.effect = effect
        return this
    }

    override fun getEffect(): Effect {
        return Effect.DIM
    }

    fun showSheet(fragmentManager: FragmentManager): BaseSheet<B> {
        show(fragmentManager, this.tag)
        return this
    }

    fun dismissSheet() {
        dismissAllowingStateLoss()  // or dismiss()
        listener?.onSheetDismiss()  // Notify listener that the sheet has been dismissed
    }

    abstract class Listener<B : ViewDataBinding> {
        private lateinit var sheet: BaseSheet<B>
        internal fun attachSheet(sheet: BaseSheet<B>) {
            this.sheet = sheet
        }

        fun getSheet(): BaseSheet<B> = sheet

        open fun onClick(view: View) {
            //override onClick
        }

        open fun onViewCreated(b: B) {
            //override for view created
        }

        open fun onSheetDismiss() {
            // Override when sheet is dismissed
        }

    }
}