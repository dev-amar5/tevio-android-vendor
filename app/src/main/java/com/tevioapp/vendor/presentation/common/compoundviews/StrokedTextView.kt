package com.tevioapp.vendor.presentation.common.compoundviews
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tevioapp.vendor.R

class StrokedTextView: AppCompatTextView {
    //region Constructors
    constructor(ctx: Context) : super(ctx, null)
    constructor(ctx: Context, attr: AttributeSet?) : super(ctx, attr, 0) {
        getStyledAttributes(attr)
    }

    constructor(ctx: Context, attr: AttributeSet?, defStyleAttr: Int) : super(ctx, attr, defStyleAttr) {
        getStyledAttributes(attr)
    }
    //endregion

    //region Members

    private var calcWidth = 0

    var strokeWidth = 4f
    var strokeColor = Color.RED

    /**
     * Static layout values are not mutable so we need to initialize it after text is set
     */
    private lateinit var staticLayout: StaticLayout

    private val staticLayoutPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@StrokedTextView.textSize
            typeface = this@StrokedTextView.typeface
        }
    }
    //endregion

    //region Methods

    private fun getStyledAttributes(attr: AttributeSet?) {
        context.obtainStyledAttributes(attr, R.styleable.StrokedTextView).apply {
            strokeWidth = getDimensionPixelSize(R.styleable.StrokedTextView_strokeThickness, 4).toFloat()
            strokeColor = getColor(R.styleable.StrokedTextView_strokeColor, Color.RED)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Just grabbing the width for static layout
        setPadding(paddingStart + strokeWidth.toInt() / 2, paddingTop, paddingRight + strokeWidth.toInt() / 2, paddingBottom)
        calcWidth = (MeasureSpec.getSize(widthMeasureSpec) - paddingStart)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        reinitializeStaticLayout()
        with(canvas) {
            save()
            translate(paddingStart.toFloat(), 0f)

            // Draw stroke first
            staticLayoutPaint.configureForStroke()
            staticLayout.draw(this)

            // Draw text
            staticLayoutPaint.configureForFill()
            staticLayout.draw(this)

            restore()
        }
    }

    private fun reinitializeStaticLayout() {
        staticLayout = StaticLayout.Builder
                .obtain(text, 0, text.length, staticLayoutPaint, calcWidth)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .build()
    }

    private fun Paint.configureForFill() {
        style = Paint.Style.FILL
        color = textColors.defaultColor
    }

    private fun Paint.configureForStroke() {
        style = Paint.Style.STROKE
        color = strokeColor
        strokeWidth = this@StrokedTextView.strokeWidth
    }
    //endregion
}