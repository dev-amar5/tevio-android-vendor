package com.tevioapp.vendor.utility.util

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation
import android.widget.ProgressBar

class ProgressBarAnimation : Animation {
    private var progressBar: ProgressBar
    private var from: Float
    private var to: Float

    constructor(progressBar: ProgressBar, from: Float, to: Float) : super() {
        this.progressBar = progressBar
        this.from = from
        this.to = to
        duration = 1000
    }

    constructor(progressBar: ProgressBar, to: Float) : super() {
        this.progressBar = progressBar
        from = progressBar.progress.toFloat()
        this.to = to
        duration = 1000
    }

    override fun getInterpolator(): Interpolator {
        return AccelerateDecelerateInterpolator()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
    }
}