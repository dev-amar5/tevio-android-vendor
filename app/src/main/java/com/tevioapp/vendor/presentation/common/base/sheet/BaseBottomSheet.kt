package com.tevioapp.vendor.presentation.common.base.sheet

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tevioapp.vendor.BR
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.extensions.addBlurEffect
import com.tevioapp.vendor.utility.extensions.removeBlurEffect
import com.tevioapp.vendor.utility.util.LoadingUtils
import javax.inject.Inject


abstract class BaseBottomSheet<B : ViewDataBinding> : BottomSheetDialogFragment() {
    private lateinit var binding: B
    lateinit var loadingUtils: LoadingUtils
    var baseActivity: BaseActivity<*>? = null
    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel?
    protected abstract fun onSheetCreated(binding: B)


    @Inject
    lateinit var sharePref: SharedPref

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) baseActivity = context
    }

    fun getViewBinding(): B = binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, getLayoutResource(), container, false)
        loadingUtils = LoadingUtils(requireContext(), binding)
        binding.setVariable(BR.vm, getViewModel())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val effect = getEffect()
        if (effect != Effect.DIM) dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        if (effect == Effect.BLUR) activity?.addBlurEffect(R.color.dialog_bg_tint)
        onSheetCreated(binding)
    }

    override fun onStop() {
        super.onStop()
        activity?.removeBlurEffect()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.window?.setWindowAnimations(R.style.BottomSheetAnimations)
        return dialog
    }

    fun showSheet(activity: FragmentActivity) {
        show(activity.supportFragmentManager, this.tag)
    }

    fun showSheet(fragment: Fragment) {
        show(fragment.childFragmentManager, this.tag)
    }

    protected open fun getEffect(): Effect {
        return Effect.BLUR
    }



    protected fun extendToFullHeight() {
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    protected fun extendToHeightPercent(percent: Float = 0.8f) {
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val layoutParams = sheet.layoutParams
                layoutParams.height =
                    (Resources.getSystem().displayMetrics.heightPixels * percent).toInt()
                sheet.layoutParams = layoutParams

                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    protected fun setSheetDraggable(enabled: Boolean) {
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.isDraggable = enabled
        }
    }

}
