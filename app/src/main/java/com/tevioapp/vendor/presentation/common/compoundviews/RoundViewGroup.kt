package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.tevioapp.vendor.R
import androidx.core.graphics.withClip

class RoundViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var topLeftRadius: Float = 0f
    private var topRightRadius: Float = 0f
    private var bottomLeftRadius: Float = 0f
    private var bottomRightRadius: Float = 0f

    private val path = Path()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RoundedViewGroup, 0, 0).apply {
            try {
                topLeftRadius = getDimension(R.styleable.RoundedViewGroup_topLeftRadius, 0f)
                topRightRadius = getDimension(R.styleable.RoundedViewGroup_topRightRadius, 0f)
                bottomLeftRadius = getDimension(R.styleable.RoundedViewGroup_bottomLeftRadius, 0f)
                bottomRightRadius = getDimension(R.styleable.RoundedViewGroup_bottomRightRadius, 0f)
                val all = getDimension(R.styleable.RoundedViewGroup_allRadius, 0f)
                if (all > 0) {
                    topLeftRadius = all
                    topRightRadius = all
                    bottomLeftRadius = all
                    bottomRightRadius = all
                }
            } finally {
                recycle()
            }
        }
        setWillNotDraw(false) // Ensures efficient drawing
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

        // Apply individual corner radii
        path.addRoundRect(
            rect, floatArrayOf(
                topLeftRadius,
                topLeftRadius,
                topRightRadius,
                topRightRadius,
                bottomRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                bottomLeftRadius
            ), Path.Direction.CW
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withClip(path) {
            super.dispatchDraw(this)
        }
    }
}
