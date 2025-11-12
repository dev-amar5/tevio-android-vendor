package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import com.tevioapp.vendor.R

class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var maxHeight = 0

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView, 0, 0).apply {
            try {
                maxHeight = getDimensionPixelSize(R.styleable.MaxHeightScrollView_mhs_maxHeight, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newHeightMeasureSpec = heightMeasureSpec
        if (maxHeight > 0) {
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}
