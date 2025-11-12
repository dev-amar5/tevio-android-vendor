package com.tevioapp.vendor.utility.animation

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


object AnimationUtils {


    fun animateScaleInOut(view: View, onEnd: (() -> Unit)? = null) {
        view.animate().scaleX(0.995f).scaleY(0.995f).alpha(0.995f).setDuration(50).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(50).withEndAction {
                onEnd?.invoke()
            }.start()
        }.start()
    }

    fun animateHeight(view: ViewGroup) {
        val initialHeight = view.height
        // Measure the new height dynamically
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight
        if (initialHeight == targetHeight) return // No animation needed
        ValueAnimator.ofInt(initialHeight, targetHeight).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                view.layoutParams.height = animatedValue
                view.requestLayout()
            }
            start()
        }
    }

    private var searchHintJob: Job? = null // Holds the running coroutine

    fun LifecycleCoroutineScope.animateSearchHint(
        etSearch: EditText, fixedText: String, animatedTexts: List<String>
    ) {
        val delayMillis = 2500L // Delay before switching words
        val animationSpeed = 100L // Delay between each letter appearing
        searchHintJob?.cancel() // Cancel any existing animation before starting a new one
        searchHintJob = launch {
            while (isActive) { // Ensures the loop stops when lifecycle is destroyed
                for (animatedText in animatedTexts) { // Iterate through different words
                    for (i in 0..animatedText.length) {
                        etSearch.hint = fixedText + animatedText.substring(0, i)
                        delay(animationSpeed)
                    }
                    delay(delayMillis) // Pause before switching words
                }
            }
        }
    }
}