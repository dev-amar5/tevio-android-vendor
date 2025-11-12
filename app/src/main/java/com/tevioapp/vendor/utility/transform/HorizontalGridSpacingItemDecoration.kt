package com.tevioapp.vendor.utility.transform

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class HorizontalGridSpacingItemDecoration(
    context: Context,
    private val spanCount: Int,
    @DimenRes spacingHorizontal: Int,
    @DimenRes spacingVertical: Int,
    private val includeEdge: Boolean = true
) : RecyclerView.ItemDecoration() {

    private val horizontal = context.resources.getDimensionPixelSize(spacingHorizontal)
    private val vertical = context.resources.getDimensionPixelSize(spacingVertical)

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val row = position / spanCount

        if (includeEdge) {
            if (row == 0) {
                outRect.left = horizontal*4
                outRect.right = horizontal/ 2
            }
            else {
                outRect.left = horizontal / 2
                outRect.right = horizontal / 2
            }
        } else {
            outRect.left = horizontal / 2
            outRect.right = horizontal / 2
        }



        outRect.bottom = vertical / 2
    }
}