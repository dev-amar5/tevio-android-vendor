package com.tevioapp.vendor.utility.util

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.DialogLoaderBinding

class LoadingUtils(val context: Context, var binding: ViewDataBinding) {
    private var shimmerMain: FrameLayout? = binding.root.findViewById(R.id.fl_shimmer_main)
    private var shimmerFrame: ShimmerFrameLayout? =
        binding.root.findViewById(R.id.shimmer_frame_layout)
    private val handler = Handler(Looper.getMainLooper())
    private var dialog: AlertDialog? = null
    private var dialogCancelable: Boolean = true
    fun setMainLayout(layout: FrameLayout) {
        this.shimmerMain = layout
    }

    fun setShimmerLayout(layout: ShimmerFrameLayout) {
        this.shimmerFrame = layout
    }

    private val runnableDialog = Runnable {
        val builder = AlertDialog.Builder(context)
        val dialogView = DataBindingUtil.inflate<DialogLoaderBinding>(
            LayoutInflater.from(context), R.layout.dialog_loader, null, false
        )
        dialogView.lvMain.apply {
            LottieUtil.loadAnimation(this, LottieAnimationType.LOADING)
        }
        builder.setView(dialogView.root)
        builder.setCancelable(dialogCancelable)
        dialog = builder.create()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
    }

    fun showLoading(cancelable: Boolean = true) {
        if (dialog?.isShowing == true) {
            return
        }
        dialogCancelable = cancelable
        handler.postDelayed(runnableDialog, 500)
    }


    fun hideLoading() {
        handler.removeCallbacks(runnableDialog)
        dialog?.dismiss()
    }

    fun startShimmer() {
        if (shimmerMain?.isVisible == true) return
        shimmerMain?.apply {
            alpha = 1f
            isVisible = true
        }
        shimmerFrame?.startShimmer()
    }

    fun stopShimmer() {
        shimmerMain?.apply {
            shimmerFrame?.stopShimmer()
            isVisible = false
        }
    }

    fun release() {
        hideLoading()
    }
}