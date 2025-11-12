package com.tevioapp.vendor.utility.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.*

private const val AUTO_SCROLL_TAG_KEY = -1234567 // Unique tag key

private var ViewPager2.autoScrollJob: Job?
    get() = getTag(AUTO_SCROLL_TAG_KEY) as? Job
    set(value) = setTag(AUTO_SCROLL_TAG_KEY, value)

fun ViewPager2.enableAutoScroll(
    lifecycleOwner: LifecycleOwner,
    enable: Boolean,
    interval: Long = 3000L
) {
    autoScrollJob?.cancel()
    if (!enable) return

    autoScrollJob = lifecycleOwner.lifecycleScope.launch {
        while (isActive) {
            delay(interval)
            val adapter = this@enableAutoScroll.adapter ?: return@launch
            if (adapter.itemCount > 1) {
                val nextItem = (currentItem + 1) % adapter.itemCount
                setCurrentItem(nextItem, true)
            }
        }
    }
}
