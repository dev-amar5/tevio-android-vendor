package com.tevioapp.vendor.utility.popups

import android.app.Dialog
import android.content.Context
import android.view.View
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.databinding.DialogInfoLocationBinding
import com.tevioapp.vendor.databinding.DialogInfoSecurityBinding
import com.tevioapp.vendor.presentation.common.base.dialog.BaseCustomDialog


object PopupsUtils {

    fun showEnableSecurityPopUp(
        context: Context, onSubmit: (Dialog) -> Unit, onCancel: () -> Unit
    ) {
        BaseCustomDialog(
            mContext = context,
            layoutId = R.layout.dialog_info_security,
            effect = Effect.BLUR,
            listener = object : BaseCustomDialog.Listener<DialogInfoSecurityBinding>() {
                override fun onViewCreated(binding: DialogInfoSecurityBinding) {
                    // No usage
                }

                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.iv_cross, R.id.tv_skip -> {
                            dismissDialog()
                            onCancel()
                        }

                        R.id.btn_submit -> {
                            dialog?.let { onSubmit(it) }
                        }
                    }
                }
            }).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }.show()
    }

    fun showLocationPermissionPopUp(
        context: Context, onAllow: () -> Unit
    ) {
        BaseCustomDialog(
            mContext = context,
            layoutId = R.layout.dialog_info_location,
            effect = Effect.DIM,
            listener = object : BaseCustomDialog.Listener<DialogInfoLocationBinding>() {
                override fun onViewCreated(binding: DialogInfoLocationBinding) {
                    // No usage
                }

                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.btn_submit -> {
                            dialog?.dismiss()
                            onAllow()
                        }
                    }
                }
            }).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }.show()
    }
}