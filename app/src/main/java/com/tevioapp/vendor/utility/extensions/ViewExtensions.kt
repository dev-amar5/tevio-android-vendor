package com.tevioapp.vendor.utility.extensions

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.RadioButton
import android.widget.ScrollView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.Order
import com.tevioapp.vendor.databinding.ItemOrderItemBinding
import com.tevioapp.vendor.databinding.ViewOrderSummeryBinding
import com.tevioapp.vendor.databinding.ViewShimmerLoaderBinding
import com.tevioapp.vendor.databinding.ViewTitleExpendBinding
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.transform.AsteriskPassword
import jp.wasabeef.blurry.Blurry

/**
 * add password toggle
 * @param view view
 */
fun EditText.addPasswordToggle(view: View) {
    val myTypeface = this.typeface
    view.setOnClickListener {
        it.preventDoubleClick()
        if (this.tag == null) {
            inputType = InputType.TYPE_CLASS_TEXT
            this.tag = "show"
            this.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null, ContextCompat.getDrawable(this.context, R.drawable.password_show), null
            )
            this.transformationMethod = null
            myTypeface?.let {
                this.typeface = it
            }
        } else {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            this.tag = null
            this.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null, ContextCompat.getDrawable(this.context, R.drawable.password_hide), null
            )
            this.transformationMethod = AsteriskPassword()
        }
        this.setSelection(this.text.length)
        myTypeface?.let {
            this.typeface = it
        }
    }
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    this.tag = null
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null, null, ContextCompat.getDrawable(this.context, R.drawable.password_hide), null
    )
    this.transformationMethod = AsteriskPassword()
    this.setSelection(this.text.length)
    myTypeface?.let {
        this.typeface = it
    }
}


/**
 * focus error
 * @param error error message
 */
fun EditText.focusError(error: String, selection: Boolean = true) {
    this.requestFocus()
    this.error = error
    if (selection) this.setSelection(this.text.length)
}

fun EditText.focusError(@StringRes error: Int, selection: Boolean = true) {
    focusError(this.context.getString(error), selection)
}


/**
 * prevent double click
 */
fun View?.preventDoubleClick() {
    try {
        this?.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            this?.isEnabled = true
        }, 200)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.setViewMarginsPx(
    top: Int? = null,
    bottom: Int? = null,
    left: Int? = null,
    right: Int? = null,
    animationDuration: Long = 0L
) {
    val lp = layoutParams as? MarginLayoutParams ?: return

    val startTop = lp.topMargin
    val startBottom = lp.bottomMargin
    val startLeft = lp.leftMargin
    val startRight = lp.rightMargin

    val endTop = top ?: startTop
    val endBottom = bottom ?: startBottom
    val endLeft = left ?: startLeft
    val endRight = right ?: startRight

    // No change
    if (startTop == endTop && startBottom == endBottom && startLeft == endLeft && startRight == endRight) return

    if (animationDuration == 0L) {
        lp.setMargins(endLeft, endTop, endRight, endBottom)
        layoutParams = lp
        requestLayout()
        return
    }
    ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animationDuration
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            lp.topMargin = startTop + ((endTop - startTop) * fraction).toInt()
            lp.bottomMargin = startBottom + ((endBottom - startBottom) * fraction).toInt()
            lp.leftMargin = startLeft + ((endLeft - startLeft) * fraction).toInt()
            lp.rightMargin = startRight + ((endRight - startRight) * fraction).toInt()
            layoutParams = lp
            requestLayout()
        }
        start()
    }
}

fun View.setViewMarginsRes(
    @DimenRes top: Int? = null,
    @DimenRes bottom: Int? = null,
    @DimenRes left: Int? = null,
    @DimenRes right: Int? = null,
    animationDuration: Long = 0L
) {
    val topPx = top?.let { context.resources.getDimensionPixelSize(it) }
    val bottomPx = bottom?.let { context.resources.getDimensionPixelSize(it) }
    val leftPx = left?.let { context.resources.getDimensionPixelSize(it) }
    val rightPx = right?.let { context.resources.getDimensionPixelSize(it) }
    setViewMarginsPx(
        top = topPx,
        bottom = bottomPx,
        left = leftPx,
        right = rightPx,
        animationDuration = animationDuration
    )
}


fun View.setMarginBottom(margin: Int) {
    (layoutParams as? MarginLayoutParams)?.let { lp ->
        lp.bottomMargin = margin
        layoutParams = lp
        requestLayout()
    }
}


fun RecyclerView.setVerticalSpacingDecorator(@ColorRes color: Int = R.color.gray) {
    addItemDecoration(
        MaterialDividerItemDecoration(
            context, MaterialDividerItemDecoration.VERTICAL
        ).apply {
            isLastItemDecorated = false
            setDividerColorResource(context, color)
        })
    if (layoutManager == null) layoutManager = LinearLayoutManager(context)
}


fun Activity.addBlurEffect(@ColorRes color: Int? = null) {
    window?.decorView?.post {
        val decorView = this.window?.decorView as? ViewGroup ?: return@post

        try {
            val blurry = Blurry.with(this).radius(15) // Set the blur radius
                .sampling(1)

            if (color != null) {
                blurry.color(ContextCompat.getColor(this, color))
            }

            blurry.onto(decorView)
            Logger.d("Blur applied successfully")

        } catch (e: Exception) {
            Logger.e("Error applying blur effect", e)
        }
    }
}


fun Activity.removeBlurEffect() {
    val decorView = window?.decorView as? ViewGroup
    decorView?.let {
        Blurry.delete(it) // Remove blur effect
    }
}

fun ScrollView.attachElevationControl(view: View) {
    setOnScrollChangeListener { _, _, i2, _, _ ->
        val elevation = if (i2 > 10) {
            10
        } else {
            i2
        }
        view.elevation = elevation.toFloat()
    }
}

fun RecyclerView.scrollCenterItemByPosition(position: Int) {
    if (position == RecyclerView.NO_POSITION) return
    val viewHolder = this.findViewHolderForAdapterPosition(position)

    if (viewHolder != null) {
        // ViewHolder is visible, scroll to center
        val itemView = viewHolder.itemView
        val itemCenter = itemView.left + itemView.width / 2
        val recyclerCenter = this.width / 2
        val scrollDistance = itemCenter - recyclerCenter
        this.smoothScrollBy(scrollDistance, 0)
    } else {
        // If not visible, scroll to position first
        this.smoothScrollToPosition(position)

        // Wait until scrolling finishes, then center the item
        this.post {
            val vh = this.findViewHolderForAdapterPosition(position)
            vh?.let {
                val itemView = it.itemView
                val itemCenter = itemView.left + itemView.width / 2
                val recyclerCenter = this.width / 2
                val scrollDistance = itemCenter - recyclerCenter
                this.smoothScrollBy(scrollDistance, 0)
            }
        }
    }
}

fun EditText.getTrimText(): String {
    return text.toString().trim()
}

fun MaterialCardView.setCustomCorners(
    @DimenRes topLeftRadiusRes: Int? = null,
    @DimenRes topRightRadiusRes: Int? = null,
    @DimenRes bottomLeftRadiusRes: Int? = null,
    @DimenRes bottomRightRadiusRes: Int? = null
) {
    val topLeftRadius = topLeftRadiusRes?.let { context.resources.getDimension(it) } ?: 0f
    val topRightRadius = topRightRadiusRes?.let { context.resources.getDimension(it) } ?: 0f
    val bottomLeftRadius = bottomLeftRadiusRes?.let { context.resources.getDimension(it) } ?: 0f
    val bottomRightRadius = bottomRightRadiusRes?.let { context.resources.getDimension(it) } ?: 0f

    val shapeAppearanceModel =
        ShapeAppearanceModel.builder().setTopLeftCorner(CornerFamily.ROUNDED, topLeftRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, topRightRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRightRadius).build()

    this.shapeAppearanceModel = shapeAppearanceModel
}

fun MotionLayout.updateProgressWithDynamicStates(
    position: Int, totalItems: Int, getStateIdForProgress: (progress: Float) -> Int
) {
    if (totalItems <= 1) return
    val progress = position.toFloat() / (totalItems - 1).coerceAtLeast(1)
    val targetState = getStateIdForProgress(progress)
    if (this.currentState != targetState) {
        this.transitionToState(targetState)
    }
}

fun View.setWidthInDp(dp: Int) {
    val scale = context.resources.displayMetrics.density
    layoutParams = layoutParams.apply {
        width = (dp * scale).toInt()
    }
    requestLayout()
}

fun View.setWidthPercentage(percentage: Float) {
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    layoutParams = layoutParams.apply {
        width = (screenWidth * percentage).toInt()
    }
    requestLayout()
}

fun RecyclerView.scrollToPositionTopEdge(position: Int) {
    val layoutManager = layoutManager as? LinearLayoutManager ?: return
    post {
        this.animate().alpha(0f).setDuration(100).withEndAction {
            layoutManager.scrollToPositionWithOffset(position, 0)
            this.animate().alpha(1f).setDuration(200)
        }
    }
}

fun ViewShimmerLoaderBinding.startShimmer() {
    flShimmerMain.apply {
        alpha = 1f
        isVisible = true
    }
    shimmerFrameLayout.startShimmer()
}

fun ViewShimmerLoaderBinding.stopShimmer() {
    flShimmerMain.animate().alpha(0f).setDuration(300).withEndAction {
        shimmerFrameLayout.stopShimmer()
        flShimmerMain.isVisible = false
    }
}

fun RadioButton.setStyle(isBold: Boolean, textSizeRes: Int) {
    context.apply {
        typeface = ResourcesCompat.getFont(
            this, if (isBold) R.font.montserrat_bold else R.font.montserrat_regular
        )
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(textSizeRes))
    }
}

fun RecyclerView.removeAllItemDecoration() {
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}

fun View.animateRotation(fromRotation: Float, toRotation: Float, duration: Long = 300) {
    ObjectAnimator.ofFloat(this, "rotation", fromRotation, toRotation).apply {
        this.duration = duration
        start()
    }
}

fun View.animateVisibility(isVisible: Boolean, duration: Long = 500, onEnd: (() -> Unit)? = null) {
    if (this.isVisible == isVisible) return
    if (duration in 1..2000) {
        if (isVisible) {
            this.alpha = 0f
            this.isVisible = true
            animate().alpha(1f).setDuration(duration).withEndAction {
                onEnd?.invoke()
            }.start()
        } else {
            animate().alpha(0f).setDuration(duration).withEndAction {
                this.isVisible = false
                onEnd?.invoke()
            }.start()
        }
    } else {
        this.isVisible = isVisible
        onEnd?.invoke()
    }
}

fun View.setTopMarginFromInsets(inset: Insets, @DimenRes extraMargin: Int = 0) {
    var marginTop = inset.top
    if (extraMargin != 0) {
        marginTop += resources.getDimensionPixelSize(extraMargin)
    }
    val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.topMargin = marginTop
    this.layoutParams = layoutParams
}


fun View.setBlur(isBlur: Boolean, radius: Float = 20f) {
    if (isBlur) {
        // Apply blur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            this.setRenderEffect(blurEffect)
        } else {
            this.alpha = 0.5f // fallback for older devices
        }
    } else {
        // Remove blur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.setRenderEffect(null)
        } else {
            this.alpha = 1f
        }
    }
}

fun ViewTitleExpendBinding.attachExpendView(view: View, expend: Boolean = true) {
    fun setExpend(enable: Boolean) {
        ivMore.rotation = if (enable) -90f else 90f
        view.isVisible = enable
    }
    setExpend(expend)
    ivMore.setOnClickListener {
        view.isVisible = view.isVisible.not()
        setExpend(view.isVisible)
    }
}

fun ViewOrderSummeryBinding.setOrderDetail(order: Order?) {
    this.order = order
    val storeInfo = order?.storeInfo
    if (storeInfo != null) {
        store.isVisible = true
        container.removeAllViews()
        order.menuItems?.forEach { orderItem ->
            ItemOrderItemBinding.inflate(
                LayoutInflater.from(
                    root.context
                ), container, false
            ).apply {
                item = orderItem
                root.setViewMarginsRes(R.dimen._1sdp)
                container.addView(root)
            }
        }
    } else {
        container.removeAllViews()
        store.isVisible = false
    }

}
