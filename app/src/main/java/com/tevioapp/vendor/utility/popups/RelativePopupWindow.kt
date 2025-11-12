package com.tevioapp.vendor.utility.popups

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.DimenRes
import androidx.annotation.IntDef
import com.tevioapp.vendor.utility.extensions.clearFocusAndHideKeyboard

/**
 * @author Arvind
 * @since 2023/01/03
 */
abstract class RelativePopupWindow(private val context: Context) : PopupWindow(context) {

    @IntDef(
        *[VerticalPosition.CENTER, VerticalPosition.ABOVE, VerticalPosition.BELOW, VerticalPosition.ALIGN_TOP, VerticalPosition.ALIGN_BOTTOM]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class VerticalPosition {
        companion object {
            const val CENTER: Int = 0
            const val ABOVE: Int = 1
            const val BELOW: Int = 2
            const val ALIGN_TOP: Int = 3
            const val ALIGN_BOTTOM: Int = 4
        }
    }

    @IntDef(
        *[HorizontalPosition.CENTER, HorizontalPosition.LEFT, HorizontalPosition.RIGHT, HorizontalPosition.ALIGN_LEFT, HorizontalPosition.ALIGN_RIGHT]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class HorizontalPosition {
        companion object {
            const val CENTER: Int = 0
            const val LEFT: Int = 1
            const val RIGHT: Int = 2
            const val ALIGN_LEFT: Int = 3
            const val ALIGN_RIGHT: Int = 4
        }
    }

    /**
     * Show at relative position to anchor View.
     * @param anchor Anchor View
     * @param vertPos Vertical Position Flag
     * @param horizontalPosition Horizontal Position Flag
     * @param asDropDown Automatically fit in screen or not
     */
    open fun showOnAnchor(
        anchor: View,
        @VerticalPosition vertPos: Int,
        @HorizontalPosition horizontalPosition: Int,
        asDropDown: Boolean
    ) {
        showOnAnchor(anchor, vertPos, horizontalPosition, 0, 0, asDropDown)
    }

    /**
     * Show at relative position to anchor View with translation.
     * @param anchor Anchor View
     * @param vertPos Vertical Position Flag
     * @param horizontalPosition Horizontal Position Flag
     * @param x Translation X in dimension resource (e.g., R.dimen.some_value)
     * @param y Translation Y in dimension resource (e.g., R.dimen.some_value)
     * @param asDropDown Automatically fit in screen or not
     */
    open fun showOnAnchor(
        anchor: View,
        @VerticalPosition vertPos: Int,
        @HorizontalPosition horizontalPosition: Int,
        @DimenRes x: Int = 0,
        @DimenRes y: Int = 0,
        asDropDown: Boolean = true
    ) {
        require(anchor.parent != null) { "Anchor view must be attached to a parent view." }
        context.clearFocusAndHideKeyboard()
        val context = anchor.context
        val xPixels = if (x != 0) context.resources.getDimensionPixelSize(x) else 0
        val yPixels = if (y != 0) context.resources.getDimensionPixelSize(y) else 0

        var xTranslation = xPixels
        var yTranslation = yPixels
        isClippingEnabled = asDropDown

        val contentView = contentView
        val windowRect = Rect()
        contentView.getWindowVisibleDisplayFrame(windowRect)

        val windowW = windowRect.width()
        val windowH = windowRect.height()

        contentView.measure(
            makeDropDownMeasureSpec(width, windowW),
            makeDropDownMeasureSpec(height, windowH)
        )

        val measuredW = contentView.measuredWidth
        val measuredH = contentView.measuredHeight
        val anchorLocation = IntArray(2)
        anchor.getLocationInWindow(anchorLocation)
        val anchorBottom = anchorLocation[1] + anchor.height

        if (!asDropDown) {
            xTranslation += anchorLocation[0]
            yTranslation += anchorBottom
        }

        when (vertPos) {
            VerticalPosition.ABOVE -> yTranslation -= measuredH + anchor.height
            VerticalPosition.ALIGN_BOTTOM -> yTranslation -= measuredH
            VerticalPosition.CENTER -> yTranslation -= anchor.height / 2 + measuredH / 2
            VerticalPosition.ALIGN_TOP -> yTranslation -= anchor.height
            VerticalPosition.BELOW -> {}
        }

        when (horizontalPosition) {
            HorizontalPosition.LEFT -> xTranslation -= measuredW
            HorizontalPosition.ALIGN_RIGHT -> xTranslation -= measuredW - anchor.width
            HorizontalPosition.CENTER -> xTranslation += anchor.width / 2 - measuredW / 2
            HorizontalPosition.ALIGN_LEFT -> {}
            HorizontalPosition.RIGHT -> xTranslation += anchor.width
        }

        // RTL Support
        if (anchor.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            xTranslation = -xTranslation
        }

        if (asDropDown) {
            if (yTranslation + anchorBottom < 0) {
                yTranslation = -anchorBottom
            } else if (yTranslation + anchorBottom + measuredH > windowH) {
                yTranslation = windowH - anchorBottom - measuredH
            }
            showAsDropDown(anchor, xTranslation, yTranslation, Gravity.NO_GRAVITY)
        } else {
            showAtLocation(anchor, Gravity.NO_GRAVITY, xTranslation, yTranslation)
        }
    }


    companion object {
        fun makeDropDownMeasureSpec(measureSpec: Int, maxSize: Int): Int {
            return View.MeasureSpec.makeMeasureSpec(
                getDropDownMeasureSpecSize(measureSpec, maxSize),
                getDropDownMeasureSpecMode(measureSpec)
            )
        }

        fun getDropDownMeasureSpecSize(measureSpec: Int, maxSize: Int): Int {
            return when (measureSpec) {
                ViewGroup.LayoutParams.MATCH_PARENT -> maxSize
                else -> View.MeasureSpec.getSize(measureSpec)
            }
        }

        fun getDropDownMeasureSpecMode(measureSpec: Int): Int {
            return when (measureSpec) {
                ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.UNSPECIFIED
                else -> View.MeasureSpec.EXACTLY
            }
        }
    }
}
