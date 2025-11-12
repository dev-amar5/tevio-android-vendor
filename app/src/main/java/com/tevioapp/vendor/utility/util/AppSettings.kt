package com.tevioapp.vendor.utility.util

import com.tevioapp.vendor.BuildConfig

object AppSettings {
    fun isMockDirection(): Boolean = BuildConfig.FLAVOR=="dev"
    fun isLoggingEnabled(): Boolean = true
    fun getMinimumReachDistance(): Int =
        if(BuildConfig.FLAVOR=="dev")50000 else 50   // Minimum distance in meter that rider mark as reached to destination location default 50 meter
}

