package com.tevioapp.vendor.utility.animation

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class LocationPinAnimation(private val view: View) {
    private var running = false
    private fun translateYUpDown() {
        view.animate().translationY(-50f).setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        if (running) translateYUpDown()
                        else finishingAnimation()
                    }
            }
    }

    private fun finishingAnimation() {
        view.animate().translationY(-120f).setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator())
            }
    }

    fun startAnimation() {
       /* running = true
        view.clearAnimation()
        translateYUpDown()*/
    }

    fun stopAnimation() {
       // running = false
    }

}