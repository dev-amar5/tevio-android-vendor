package com.tevioapp.vendor.utility.util

import com.airbnb.lottie.LottieAnimationView

object LottieUtil {

    fun loadAnimation(
        animationView: LottieAnimationView, type: LottieAnimationType, shouldPlay: Boolean = true
    ) {
        try {
            if (type.source.startsWith("http", true)) animationView.setAnimationFromUrl(type.source)
            else animationView.setAnimation(type.source)
            if (shouldPlay) animationView.playAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class LottieAnimationType(val source: String) {
    SPLASH("anim_splash2.json"), LOADING("anim_loading2.json"),DELIVERY_TIP("delivery_tip.json")
}

