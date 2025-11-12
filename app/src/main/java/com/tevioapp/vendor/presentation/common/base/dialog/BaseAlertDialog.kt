package com.tevioapp.vendor.presentation.common.base.dialog

import android.content.Context
import android.text.SpannedString
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.databinding.DialogAlertBinding

class BaseAlertDialog(
    context: Context,
    private val title: String,
    private val message: Any? = null,
    private val positive: String,
    private val negative: String? = null,
    private val cancelable: Boolean = true,
    private val icon: Int? = null,
    private val onPositiveButtonClick: (dialog: BaseCustomDialog<DialogAlertBinding>) -> Unit,
    private val onNegativeButtonClick: ((dialog: BaseCustomDialog<DialogAlertBinding>) -> Unit)? = null
) {
    private var dialogAlert: BaseCustomDialog<DialogAlertBinding>

    init {
        dialogAlert = BaseCustomDialog(
            mContext = context,
            layoutId = R.layout.dialog_alert,
            effect = Effect.DIM,
            listener = object : BaseCustomDialog.Listener<DialogAlertBinding>() {
                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.btn_ok -> {
                            dialog?.let { onPositiveButtonClick.invoke(it) }
                        }

                        R.id.tv_close -> {
                            dialog?.let { onNegativeButtonClick?.invoke(it) }
                        }

                        R.id.iv_cross -> {
                            dismiss()
                        }
                    }
                }

                override fun onViewCreated(binding: DialogAlertBinding) {
                    binding.ivCross.isVisible = cancelable
                    binding.tvClose.isGone = negative.isNullOrEmpty()
                    if (message == null) {
                        binding.tvSubHeader.isGone = true
                    } else {
                        when (message) {
                            is String -> binding.tvSubHeader.text = message
                            is SpannedString -> binding.tvSubHeader.text = message
                        }
                    }
                    binding.tvHeader.text = title
                    binding.btnOk.text = positive
                    binding.tvClose.text = negative
                    icon?.let {
                        binding.ivIcon.setImageResource(it)
                    }
                }
            }).apply {
            setCancelable(cancelable)
        }
    }

    fun show(): BaseAlertDialog {
        dialogAlert.show()
        return this
    }

    fun dismiss() {
        dialogAlert.dismiss()
    }

    fun isDialogVisible(): Boolean {
        return dialogAlert.isShowing
    }
}
