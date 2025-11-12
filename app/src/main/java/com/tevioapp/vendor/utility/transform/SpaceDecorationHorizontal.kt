package com.tevioapp.vendor.utility.transform

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpaceDecorationHorizontal(
    val context: Context,
    @DimenRes sidePaddingRes: Int? = null,
    @DimenRes middleSpace: Int? = null,
    @DimenRes verticalSpace: Int? = null
) : ItemDecoration() {
    private var sidePadding = sidePaddingRes?.let { context.resources.getDimensionPixelSize(it) } ?: 0
    private var middlePadding = middleSpace?.let { context.resources.getDimensionPixelSize(it) / 2 }
    private var verticalPadding = verticalSpace?.let { context.resources.getDimensionPixelSize(it) }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (position == RecyclerView.NO_POSITION) return
        verticalPadding?.let {
            outRect.top = it
            outRect.bottom = it
        }
        if (position == 0) {
            // Add padding to the start of the first item
            outRect.left = sidePadding
            middlePadding?.let {
                outRect.right = it
            }
        } else if (position == itemCount - 1) {
            // Add padding to the end of the last item
            outRect.right = sidePadding
            middlePadding?.let {
                outRect.left = it
            }
        } else {
            middlePadding?.let {
                outRect.right = it
                outRect.left = it
            }
        }
    }
}