package com.tevioapp.vendor.utility.util

import android.os.Build
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import com.tevioapp.vendor.BuildConfig
import com.tevioapp.vendor.utility.extensions.setMultiSpan

object DeviceUtils {
    fun isEmulator(): Boolean {
        val buildProperties = listOf(
            Build.FINGERPRINT,
            Build.MODEL,
            Build.MANUFACTURER,
            Build.BRAND,
            Build.HARDWARE,
            Build.PRODUCT
        )

        val emulatorIndicators = listOf(
            "generic", "unknown", "emulator", "sdk", "sdk_gphone", "vbox86", "emulator64", "x86"
        )

        return buildProperties.any { prop ->
            emulatorIndicators.any { indicator -> prop.contains(indicator, ignoreCase = true) }
        }
    }

    fun getAppVersionName(): SpannedString {
        return buildSpannedString {
            append("Version ")
            append(BuildConfig.VERSION_NAME.setMultiSpan(bold = true))
        }
    }
}

