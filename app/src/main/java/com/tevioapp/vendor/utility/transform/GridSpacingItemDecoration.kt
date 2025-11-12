package com.tevioapp.vendor.utility.transform

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    context: Context,
    private val spanCount: Int,
    @DimenRes spacingHorizontal: Int,
    @DimenRes spacingVertical: Int,
    private val skipEdges: Boolean
) : RecyclerView.ItemDecoration() {

    private val horizontal = context.resources.getDimensionPixelSize(spacingHorizontal)
    private val vertical = context.resources.getDimensionPixelSize(spacingVertical)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val column = position % spanCount

        if (skipEdges) {
            outRect.left = horizontal * column / spanCount
            outRect.right = horizontal * (spanCount - 1 - column) / spanCount
            if (position >= spanCount) {
                outRect.top = vertical
            }
        } else {
            outRect.left = horizontal / 2
            outRect.right = horizontal / 2
            outRect.top = vertical / 2
            outRect.bottom = vertical / 2
        }
    }

}
