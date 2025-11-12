package com.tevioapp.vendor.presentation.common.base.dialog

import android.content.Context
import androidx.core.net.toUri
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.databinding.DialogFullscreenImageBinding
import com.tevioapp.vendor.presentation.common.compoundviews.GlideApp

class FullScreenImageDialog(
    context: Context, private val imageUrl: String
) {
    private var baseDialog: BaseCustomDialog<DialogFullscreenImageBinding> = BaseCustomDialog(
        mContext = context,
        layoutId = R.layout.dialog_fullscreen_image,
        effect = Effect.DIM,
        isFullScreen = true,
        listener = object : BaseCustomDialog.Listener<DialogFullscreenImageBinding>() {
            override fun onViewCreated(binding: DialogFullscreenImageBinding) {
                if (imageUrl.startsWith("http")) {
                    GlideApp.with(binding.root).load(imageUrl).into(binding.zoomImageView)
                } else {
                    binding.zoomImageView.setImageURI(imageUrl.toUri())
                }
                binding.root.setOnClickListener { dismissDialog() }
            }
        }).apply {
        setCancelable(true)
    }

    fun show() {
        baseDialog.show()
    }

    fun dismiss() {
        baseDialog.dismiss()
    }
}
