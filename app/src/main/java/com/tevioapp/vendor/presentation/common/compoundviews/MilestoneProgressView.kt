package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.extensions.SpanConfig
import com.tevioapp.vendor.utility.extensions.setMultiSpan

class MilestoneProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val milestones = mutableListOf<Milestone>()
    private var progressPercent: Float = 0f // 0–100

    // --- Dimensions ---
    private val circleRadius = resources.getDimension(R.dimen._7sdp)
    private val lineHeight = resources.getDimension(R.dimen._3sdp)
    private val textPadding = resources.getDimension(R.dimen._3sdp)
    private val textSize = resources.getDimension(R.dimen._10ssp)
    private val stepGap = resources.getDimension(R.dimen._50sdp)

    // --- Colors ---
    private val completedColor = ContextCompat.getColor(context, R.color.dark_green)
    private val pendingColor = ContextCompat.getColor(context, R.color.gray)
    private val textColor = ContextCompat.getColor(context, R.color.gray_white)

    // --- Paints ---
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = this@MilestoneProgressView.textSize
    }

    private val checkDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_tick)

    init {
        // Preview in XML editor
        if (isInEditMode) {
            setData(
                arrayListOf(
                    Milestone(
                        topText = "₵20", bottomText = "10\nOrders".setMultiSpan(
                            SpanConfig(
                                bold = true, typeface = ResourcesCompat.getFont(
                                    context, R.font.montserrat_medium
                                )
                            )
                        )
                    ),
                    Milestone("₵100", "20\nOrders"),
                    Milestone("₵150", "30\nOrders"),
                    Milestone("₵200", "40\nOrders")
                ), 40f
            )
        }
    }

    // --- Public API ---
    fun setData(milestones: List<Milestone>, progressPercent: Float) {
        this.milestones.clear()
        this.milestones.addAll(milestones)
        this.progressPercent = progressPercent.coerceIn(0f, 100f)
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Calculate enough height for text + circle + text
        val sampleLayout =
            createStaticLayout("Sample", stepGap.toInt())
        val textHeight = sampleLayout.height
        val desiredHeight = (textHeight * 2 + circleRadius * 2 + textPadding * 4).toInt()
        val resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec)

        val desiredWidth = if (milestones.isNotEmpty()) {
            (paddingStart + paddingEnd + (milestones.size - 1) * stepGap + milestones.size * (circleRadius * 2)).toInt()
        } else 0
        val resolvedWidth = resolveSize(desiredWidth, widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (milestones.isEmpty()) return

        val centerY = height / 2f
        val startX = paddingStart + circleRadius
        val totalLineWidth = (milestones.size - 1) * stepGap
        val progressX = startX + totalLineWidth * (progressPercent / 100f)

        // Base (gray) line
        linePaint.color = pendingColor
        linePaint.strokeWidth = lineHeight
        canvas.drawLine(startX, centerY, startX + totalLineWidth, centerY, linePaint)

        // Completed (green) line
        linePaint.color = completedColor
        canvas.drawLine(startX, centerY, progressX, centerY, linePaint)

        // --- Milestones ---
        milestones.forEachIndexed { index, milestone ->
            val x = startX + index * stepGap
            val y = centerY
            val isCompleted = x <= progressX

            // Circle
            circlePaint.color = if (isCompleted) completedColor else pendingColor
            canvas.drawCircle(x, y, circleRadius, circlePaint)

            // Tick inside completed circles
            if (isCompleted && checkDrawable != null) {
                checkDrawable.setTint(android.graphics.Color.WHITE)
                val size = circleRadius * 1.2f
                checkDrawable.setBounds(
                    (x - size / 2).toInt(),
                    (y - size / 2).toInt(),
                    (x + size / 2).toInt(),
                    (y + size / 2).toInt()
                )
                checkDrawable.draw(canvas)
            }

            // --- Top text (just above circle) ---
            milestone.topText?.let {
                val layout = createStaticLayout(it, (stepGap * 0.9f).toInt())
                val topY = y - circleRadius - textPadding - layout.height
                canvas.withTranslation(x - layout.width / 2f, topY) {
                    layout.draw(this)
                }
            }

            // --- Bottom text (just below circle, multiline supported, no extra space) ---
            milestone.bottomText?.let {
                val layout = createStaticLayout(it, (stepGap * 0.9f).toInt())
                val bottomY = y + circleRadius + textPadding
                canvas.withTranslation(x - layout.width / 2f, bottomY) {
                    layout.draw(this)
                }
            }
        }
    }

    private fun createStaticLayout(
        text: CharSequence,
        width: Int,
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(false).setLineSpacing(0f, 1f)
            .build()
    }
}

// --- Model ---
data class Milestone(
    val topText: CharSequence?, val bottomText: CharSequence?
)
