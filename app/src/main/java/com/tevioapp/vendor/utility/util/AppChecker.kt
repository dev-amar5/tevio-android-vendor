package com.tevioapp.vendor.utility.util

import com.tevioapp.vendor.BuildConfig

class AppChecker {
    private val list = arrayListOf<BUILD>()
    fun add(build: BUILD): AppChecker {
        list.add(build)
        return this
    }

    fun match(): Boolean {
        val currentFlavor = BuildConfig.FLAVOR.uppercase()
        val currentBuild = BuildConfig.BUILD_TYPE.uppercase()
        return when {
            list.isEmpty() -> false
            list.any { it.name == "${currentFlavor}_${currentBuild}" } -> true
            list.any { it.name == currentBuild } -> true
            else -> false
        }
    }

    enum class BUILD {
        DEV_DEBUG, QA_DEBUG, PROD_DEBUG, DEV_RELEASE, QA_RELEASE, PROD_RELEASE, DEBUG, RELEASE
    }
}
