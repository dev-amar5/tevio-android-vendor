package com.tevioapp.vendor.presentation.common.binding

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.request.RequestOptions
import com.tevioapp.vendor.presentation.common.compoundviews.GlideApp
import com.tevioapp.vendor.utility.CommonMethods


object ImageViewBindings {


    @JvmStatic
    @BindingAdapter(value = ["simple_resource"], requireAll = false)
    fun simpleResource(imageView: ImageView, image: Int?) {
        try {
            if (image != null) imageView.setImageResource(image)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
        imageView.setImageDrawable(null)
    }


    @SuppressLint("CheckResult")
    @JvmStatic
    @BindingAdapter(
        value = ["image_url", "image_round", "image_disable_crop", "image_place_holder"],
        requireAll = false
    )
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        isRound: Boolean? = false,
        disableCrop: Boolean? = false,
        placeholder: Drawable? = null
    ) {
        val context = imageView.context
        val crop = disableCrop?.not() ?: true
        val round = isRound ?: false

        val mainRequest = GlideApp.with(context).load(CommonMethods.resolveMediaSource(imageUrl))

        // If image should be round and placeholder is provided, use thumbnail trick
        if (round && placeholder != null) {
            val placeholderRequest = GlideApp.with(context).load(placeholder).circleCrop()
            mainRequest.circleCrop().thumbnail(placeholderRequest).into(imageView)
        } else {
            // Use standard request with transformations
            val requestOptions = RequestOptions().apply {
                when {
                    round -> circleCrop()
                    crop -> centerCrop()
                }
                placeholder?.let { placeholder(it) }
            }
            mainRequest.apply(requestOptions).into(imageView)
        }
    }

}