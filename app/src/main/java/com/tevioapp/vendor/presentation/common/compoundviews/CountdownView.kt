package com.tevioapp.vendor.presentation.common.compoundviews

import android.animation.ArgbEvaluator
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ViewCountDownBinding

class CountdownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewCountDownBinding
    private var timer: CountDownTimer? = null
    private var totalTime: Long = 0
    private var remainingTime: Long = 0
    private var isPaused = false
    private var hasFinished = false

    private var onClick: (() -> Unit)? = null
    private var onFinish: (() -> Unit)? = null

    private val startColor = ContextCompat.getColor(context, R.color.orange)
    private val endColor = ContextCompat.getColor(context, R.color.white_fix)
    private val argbEvaluator = ArgbEvaluator()

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewCountDownBinding.inflate(inflater, this, true)
        isClickable = true
        isFocusable = true

        setOnClickListener {
            onClick?.invoke()
            timer?.cancel()
        }
    }

    /** Setter for onClick callback */
    fun setOnCountdownClickListener(callback: () -> Unit) {
        onClick = callback
    }

    /** Setter for onFinish callback */
    fun setOnCountdownFinishListener(callback: () -> Unit) {
        onFinish = callback
    }

    fun startCountdown(label: String, duration: Long) {
        binding.text.text = label
        totalTime = duration
        remainingTime = duration
        binding.pbOne.max = duration.toInt()
        binding.pbOne.progress = 0
        isPaused = false
        hasFinished = false

        timer?.cancel()
        startTimer(remainingTime)
    }

    private fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, 10) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val progress = (totalTime - remainingTime).toInt()
                binding.pbOne.progress = progress
                updateColor()
            }

            override fun onFinish() {
                if (!hasFinished) {
                    hasFinished = true
                    remainingTime = 0
                    binding.pbOne.progress = totalTime.toInt()
                    updateColor()
                    onFinish?.invoke()
                }
            }
        }.start()
    }

    fun pauseCountdown() {
        if (!isPaused) {
            timer?.cancel()
            isPaused = true
        }
    }

    fun resumeCountdown() {
        if (isPaused && remainingTime > 0) {
            isPaused = false
            startTimer(remainingTime)
        }
    }

    fun cancelCountdown() {
        timer?.cancel()
        binding.pbOne.progress = 0
        remainingTime = totalTime
        isPaused = false
        hasFinished = false
        binding.text.setTextColor(startColor)
    }

    private fun updateColor() {
        if (totalTime == 0L) return
        val fraction = binding.pbOne.progress.toFloat() / totalTime
        val color = argbEvaluator.evaluate(fraction, startColor, endColor) as Int
        binding.text.setTextColor(color)
    }
}
