package com.tevioapp.vendor.utility.transform

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class SpaceDecorationVertical(
    context: Context, @DimenRes topBottomPaddingRes: Int? = null, // Top & Bottom padding
    @DimenRes sidePaddingRes: Int? = null, // Left & Right padding
    @DimenRes middleSpace: Int? = null // Space between items
) : RecyclerView.ItemDecoration() {

    private val topBottomPadding =
        topBottomPaddingRes?.let { context.resources.getDimensionPixelSize(it) }
    private val sidePadding = sidePaddingRes?.let { context.resources.getDimensionPixelSize(it) }
    private val middlePadding = middleSpace?.let { context.resources.getDimensionPixelSize(it) }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (position == RecyclerView.NO_POSITION) return

        // Apply left and right padding for all items
        sidePadding?.let {
            outRect.left = it
            outRect.right = it
        }
        when (position) {
            0 -> {
                // First item: Add top padding and middle spacing
                topBottomPadding?.let { outRect.top = it }
                middlePadding?.let { outRect.bottom = it }
            }

            itemCount - 1 -> {
                // Last item: Add bottom padding
                topBottomPadding?.let { outRect.bottom = it }
            }

            else -> {
                // Middle items: Apply only bottom spacing
                middlePadding?.let { outRect.bottom = it }
            }
        }
    }
}
