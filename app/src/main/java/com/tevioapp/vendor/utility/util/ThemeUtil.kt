package com.tevioapp.vendor.utility.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtil {

    // Enum to define theme modes
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM_DEFAULT
    }

    /**
     * Saves the selected theme mode to shared preferences.
     */
    private fun saveTheme(context: Context, mode: ThemeMode) {
        val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("theme_mode", mode.name).apply()
    }

    /**
     * Retrieves the saved theme mode from shared preferences.
     */
    fun getSavedTheme(context: Context): ThemeMode {
        val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val modeName = sharedPreferences.getString("theme_mode", ThemeMode.LIGHT.name)
        return ThemeMode.valueOf(modeName!!)
    }

    /**
     * Applies the selected theme mode.
     */
    fun applyTheme(context: Context, mode: ThemeMode) {
        saveTheme(context, mode)
        when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.SYSTEM_DEFAULT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Apply saved theme when the app starts.
     */
    fun applySavedTheme(context: Context) {
        val savedMode = getSavedTheme(context)
        applyTheme(context, savedMode)
    }

}
