package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.tevioapp.vendor.R

class DottedLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        enum class Orientation { HORIZONTAL, VERTICAL }

        private var orientation: Orientation = Orientation.VERTICAL
        private var dashColor: Int = Color.GRAY
        private var dashWidth: Float = 10f
        private var dashGap: Float = 10f
        private var dashHeight: Float = 4f

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        init {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DottedLineView,
                0, 0
            ).apply {
                try {
                    val orientationIndex = getInt(R.styleable.DottedLineView_dlv_orientation, 1)
                    orientation = if (orientationIndex == 0) Orientation.HORIZONTAL else Orientation.VERTICAL

                    dashColor = getColor(R.styleable.DottedLineView_dlv_dash_color, Color.GRAY)
                    dashWidth = getDimension(R.styleable.DottedLineView_dlv_dash_height, 10f)
                    dashGap = getDimension(R.styleable.DottedLineView_dlv_dash_gap, 10f)
                    dashHeight = getDimension(R.styleable.DottedLineView_dlv_dash_width, 4f)
                } finally {
                    recycle()
                }
            }

            paint.color = dashColor
            paint.strokeWidth = dashHeight
            paint.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (orientation == Orientation.VERTICAL) {
                val x = width / 2f
                canvas.drawLine(x, 0f, x, height.toFloat(), paint)
            } else {
                val y = height / 2f
                canvas.drawLine(0f, y, width.toFloat(), y, paint)
            }
        }
    }

