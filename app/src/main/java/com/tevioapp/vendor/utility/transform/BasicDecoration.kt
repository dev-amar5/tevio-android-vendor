package com.tevioapp.vendor.utility.transform

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.extensions.setCustomCorners
class CardDecoration(
    val context: Context,
    @DimenRes val corner: Int = R.dimen.card_corner_radius_large,
    @DimenRes val elevation: Int = R.dimen.card_corner_radius_small,
    private val dividerConfig: DividerConfig? = null
) : RecyclerView.ItemDecoration() {

    private val dividerSize =
        dividerConfig?.let { context.resources.getDimensionPixelSize(it.thickness) } ?: 0
    private val marginStart =
        dividerConfig?.let { context.resources.getDimensionPixelSize(it.marginStart) } ?: 0
    private val marginEnd =
        dividerConfig?.let { context.resources.getDimensionPixelSize(it.marginEnd) } ?: 0
    private val paint = Paint().apply {
        color =
            dividerConfig?.let { ContextCompat.getColor(context, it.color) } ?: Color.TRANSPARENT
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val isHorizontal = parent.layoutManager is LinearLayoutManager &&
                (parent.layoutManager as LinearLayoutManager).orientation == RecyclerView.HORIZONTAL

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val itemCount = state.itemCount
            val isFirst = position == 0
            val isLast = position == (itemCount - 1)

            // Draw divider
            if (isLast.not() && dividerConfig != null) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                if (isHorizontal) {
                    val left = child.right + params.rightMargin
                    val right = left + dividerSize
                    val top = child.top + marginStart
                    val bottom = child.bottom - marginEnd
                    c.drawRect(
                        left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint
                    )
                } else {
                    val left = child.left + marginStart
                    val right = child.right - marginEnd
                    val top = child.bottom + params.bottomMargin
                    val bottom = top + dividerSize
                    c.drawRect(
                        left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint
                    )
                }
            }

            // Ensure child is a MaterialCardView
            if (child is MaterialCardView) {
                child.cardElevation = child.context.resources.getDimension(elevation)
                if (isFirst && isLast) {
                    child.setCustomCorners(
                        topLeftRadiusRes = corner,
                        topRightRadiusRes = corner,
                        bottomLeftRadiusRes = corner,
                        bottomRightRadiusRes = corner
                    )
                } else if (isFirst) {
                    if (isHorizontal) {
                        child.setCustomCorners(
                            topLeftRadiusRes = corner, bottomLeftRadiusRes = corner
                        )
                    } else {
                        child.setCustomCorners(
                            topLeftRadiusRes = corner, topRightRadiusRes = corner
                        )
                    }
                } else if (isLast) {
                    if (isHorizontal) {
                        child.setCustomCorners(
                            topRightRadiusRes = corner, bottomRightRadiusRes = corner
                        )
                    } else {
                        child.setCustomCorners(
                            bottomLeftRadiusRes = corner, bottomRightRadiusRes = corner
                        )
                    }
                } else {
                    child.setCustomCorners()
                }
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(this)
    }
}

class DividerDecoration(
    val context: Context,
    private val dividerConfig: DividerConfig
) : RecyclerView.ItemDecoration() {

    private val dividerSize =
        dividerConfig.let { context.resources.getDimensionPixelSize(it.thickness) }
    private val marginStart =
        dividerConfig.let { context.resources.getDimensionPixelSize(it.marginStart) }
    private val marginEnd =
        dividerConfig.let { context.resources.getDimensionPixelSize(it.marginEnd) }
    private val paint = Paint().apply {
        color = dividerConfig.let { ContextCompat.getColor(context, it.color) }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val isHorizontal = parent.layoutManager is LinearLayoutManager &&
                (parent.layoutManager as LinearLayoutManager).orientation == RecyclerView.HORIZONTAL

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val itemCount = state.itemCount
            val isFirst = position == 0
            val isLast = position == (itemCount - 1)

            // Draw divider
            if (isLast.not()) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                if (isHorizontal) {
                    val left = child.right + params.rightMargin
                    val right = left + dividerSize
                    val top = child.top + marginStart
                    val bottom = child.bottom - marginEnd
                    c.drawRect(
                        left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint
                    )
                } else {
                    val left = child.left + marginStart
                    val right = child.right - marginEnd
                    val top = child.bottom + params.bottomMargin
                    val bottom = top + dividerSize
                    c.drawRect(
                        left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint
                    )
                }
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(this)
    }
}

data class DividerConfig(
    @DimenRes val thickness: Int = R.dimen._1sdp,
    @ColorRes val color: Int = R.color.gray,
    @DimenRes val marginStart: Int = R.dimen._10sdp,
    @DimenRes val marginEnd: Int = R.dimen._10sdp
) {
    companion object {
        val DEFAULT = DividerConfig()
    }
}
