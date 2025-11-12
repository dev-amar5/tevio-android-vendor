package com.tevioapp.vendor.utility.calender

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.Locale

/**
 * Helper class, provides [Locale] specific methods.
 */
object LocaleHelper {
    /**
     * Retrieves the string from resources by specific [Locale].
     *
     * @param context    The context.
     * @param locale     The requested locale.
     * @param resourceId The string resource id.
     *
     * @return The string.
     */
	@JvmStatic
	fun getString(context: Context, locale: Locale, @StringRes resourceId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config).getString(resourceId)
    }
}
