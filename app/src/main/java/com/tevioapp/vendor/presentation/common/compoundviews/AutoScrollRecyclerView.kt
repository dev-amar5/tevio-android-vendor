package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class AutoScrollRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val scrollHandler = Handler(Looper.getMainLooper())
    private var isAutoScrolling = false
    private var isUserScrolling = false
    private var scrollDuration = 500L // Default duration (0.5 second)
    private var autoScrollInterval = 5000L // Default interval between scrolls (5 seconds)
    private var lastVisiblePosition = 0

    private val scrollRunnable = Runnable {
        if (isUserScrolling.not())
            smoothScrollToNextPosition()
        recreateHandler()
    }

    private fun recreateHandler() {
        scrollHandler.postDelayed(scrollRunnable, autoScrollInterval)
    }

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = layoutManager as? LinearLayoutManager
                if (newState == SCROLL_STATE_IDLE) {
                    isUserScrolling = false
                    lastVisiblePosition =
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                } else {
                    isUserScrolling = true
                }
            }
        })
    }

    /**
     * Start auto-scrolling.
     */
    fun startAutoScroll(interval: Long = autoScrollInterval, duration: Long = scrollDuration,startDelay:Long=0) {
        autoScrollInterval = interval
        scrollDuration = duration
        if (!isAutoScrolling) {
            isAutoScrolling = true
            scrollHandler.postDelayed(scrollRunnable,0)
        }
    }

    /**
     * Stop auto-scrolling.
     */
    fun stopAutoScroll() {
        isAutoScrolling = false
        scrollHandler.removeCallbacks(scrollRunnable)
    }

    /**
     * Scroll to the next position with acceleration and deceleration.
     */
    private fun smoothScrollToNextPosition() {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return
        val adapterItemCount = adapter?.itemCount ?: 0
        // Ensure there are items in the adapter
        if (adapterItemCount == 0) return
        // Use last visible position as the base for next position
        val nextPosition = (lastVisiblePosition + 1) % adapterItemCount
        val smoothScroller = object : LinearSmoothScroller(context) {
            private val interpolator = AccelerateDecelerateInterpolator()
            override fun calculateTimeForScrolling(dx: Int): Int {
                if (width == 0) return scrollDuration.toInt() // Handle zero width gracefully
                return (scrollDuration * interpolator.getInterpolation(dx.toFloat() / width)).toInt()
            }
        }

        smoothScroller.targetPosition = nextPosition
        layoutManager.startSmoothScroll(smoothScroller)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoScroll()
    }
}
