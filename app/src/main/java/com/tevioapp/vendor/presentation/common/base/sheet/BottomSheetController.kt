package com.tevioapp.vendor.presentation.common.base.sheet

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetController(
    context: Context,
    private val bottomSheet: View,
    private val onSlide: ((View, Float) -> Unit)? = null,
    private val onStateChanged: ((View, Int) -> Unit)? = null
) {
    private val behavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(bottomSheet)

    init {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onSlide?.invoke(bottomSheet, slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                onStateChanged?.invoke(bottomSheet, newState)
            }
        })
    }

    fun setPeekHeight(height: Int) {
        behavior.peekHeight = height
    }

    fun setDraggable(enable: Boolean) {
        behavior.isDraggable = enable
    }

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapse() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hide() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
