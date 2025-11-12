package com.tevioapp.vendor.presentation.common.binding

import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.tevioapp.vendor.presentation.common.base.adapter.BaseViewHolder
import com.tevioapp.vendor.utility.extensions.preventDoubleClick


object BaseViewBindings {
    @JvmStatic
    @BindingAdapter(
        value = ["base_adapter_callback", "base_adapter_animate", "base_adapter_disabled", "base_adapter_disable_message"],
        requireAll = false
    )
    fun quickAdapterCallback(
        view: View, holder: BaseViewHolder?, animate: Boolean?, disabled: Boolean?, message: String?
    ) {
        if (holder != null) {
            view.setOnClickListener {
                it.preventDoubleClick()
                if (disabled == true) {
                    if (message.orEmpty().isNotEmpty()) {
                        Toast.makeText(view.context, message.orEmpty(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    holder.onItemViewClick(view)
                }
            }
        } else {
            view.setOnClickListener(null)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["dynamic_height", "dynamic_width"], requireAll = false)
    fun setDynamicHeight(view: View, heightResId: Int?, widthResId: Int?) {
        try {
            val layoutParams = view.layoutParams
            if (heightResId != null) {
                layoutParams.height = view.context.resources.getDimensionPixelSize(heightResId)
            }
            if (widthResId != null) {
                layoutParams.width = view.context.resources.getDimensionPixelSize(widthResId)
            }
            view.layoutParams = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["square_by_width", "square_by_height"], requireAll = false)
    fun setSquareView(view: View, squareByWidth: Boolean?, squareByHeight: Boolean?) {
        try {
            val layoutParams = view.layoutParams
            if (squareByHeight == true) {
                layoutParams.width = layoutParams.height
            } else if (squareByWidth == true) {
                layoutParams.height = layoutParams.width
            }
            view.layoutParams = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    @BindingAdapter("view_isVisible")
    fun viewIsVisible(view: View, value: Boolean?) {
        view.isVisible = value ?: false
    }

    @JvmStatic
    @BindingAdapter("view_isInVisible")
    fun viewIsInVisible(view: View, value: Boolean?) {
        view.isInvisible = value ?: false
    }

    @JvmStatic
    @BindingAdapter("view_isGone")
    fun viewIsGone(view: View, isGone: Boolean?) {
        view.isGone = isGone ?: false
    }

    @JvmStatic
    @BindingAdapter("visibleIfNotEmpty")
    fun View.visibleIfNotEmpty(data: String?) {
        visibility = if (data.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("visibleIfEmpty")
    fun View.visibleIfEmpty(data: String?) {
        visibility = if (data.isNullOrEmpty()) View.VISIBLE else View.GONE
    }


}