package com.tevioapp.vendor.utility.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.views.splash.SplashActivity
import com.tevioapp.vendor.utility.util.ThemeUtil


/**
 * show soft keyboard
 */
fun Activity.showKeyboard(view: View) {
    try {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * hide soft keyboard
 */
fun Activity.hideKeyboard() {
    try {
        currentFocus?.let {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


/**
 * hide soft keyboard
 */
fun Activity.hideKeyboard(view: View) {
    try {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Fragment.hideKeyboard(view: View) {
    try {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.isDarkMode(): Boolean {
    val savedTheme = ThemeUtil.getSavedTheme(this)
    return when (savedTheme) {
        ThemeUtil.ThemeMode.SYSTEM_DEFAULT -> {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> false
            }
        }

        ThemeUtil.ThemeMode.DARK -> {
            true
        }

        else -> {
            false
        }
    }
}

fun BaseActivity<*>.clearUserAndRestart() {
    sharePref.clearPref()
    WorkManager.getInstance(this).cancelAllWork()
    startActivity(Intent(this, SplashActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    })
    finishAffinity()
}

fun BaseActivity<*>.withDelay(delay: Long, block: () -> Unit) {
    baseHandler.postDelayed({
        block()
    }, delay)
}
fun BaseFragment<*>.withDelay(delay: Long, block: () -> Unit) {
    baseHandler.postDelayed({
        block()
    }, delay)
}

@Suppress("DEPRECATION")
fun BaseActivity<*>.drawBelowStatusBar(
    isStatusIconDark: Boolean = isDarkMode().not(), onStatusBarInsets: (Insets) -> Unit
) {
    enableEdgeToEdge()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Use WindowInsetsControllerCompat for Android R+
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            isStatusIconDark
    } else {
        // Use systemUiVisibility flags for API < 30
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility = if (isStatusIconDark) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Enable light icons
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() // Disable light icons
        }
    }
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
        onStatusBarInsets.invoke(insets.getInsets(WindowInsetsCompat.Type.systemBars()))
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, null)
        insets
    }
}

fun BaseFragment<*>.setSystemBarInsetListener(
    onStatusBarInsets: (Insets) -> Unit
) {
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        onStatusBarInsets.invoke(systemBarsInsets)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, null)
        insets
    }
}


fun Context.clearFocusAndHideKeyboard() {
    try {
        val activity = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext as? Activity
            else -> null
        }
        activity?.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.setStatusBarColor(@ColorRes color: Int, lightColor: Boolean = true) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.statusBarColor = ContextCompat.getColor(this, color)
    // Use WindowInsetsControllerCompat to support API 30+ and handle light/dark icons
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = lightColor
}

fun BaseActivity<*>.toggleFullScreen(isFullScreen: Boolean): Boolean {
    if (isFullScreen) {
        // Exit fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window, window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (this as? AppCompatActivity)?.supportActionBar?.show()
        // Prevent system padding (to avoid padding after orientation change)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        return false
    } else {
        // Enter fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        (this as? AppCompatActivity)?.supportActionBar?.hide()
        return true
    }
}

// 1. Convert a dimen resource directly (good for values from dimens.xml)
fun Context.dpToPx(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

// 2. Convert raw dp values (Float) to px
fun Context.dpToPx(dp: Float): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()

// 3. Convert px back to dp
fun Context.pxToDp(px: Float): Int = (px / resources.displayMetrics.density + 0.5f).toInt()


