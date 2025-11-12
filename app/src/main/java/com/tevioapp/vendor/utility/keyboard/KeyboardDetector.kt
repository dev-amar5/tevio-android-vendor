package com.tevioapp.vendor.utility.keyboard

import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.FragmentActivity
import com.tevioapp.vendor.utility.log.Logger
import io.reactivex.Observable

class KeyboardDetector(private val activity: FragmentActivity) {

    companion object {
        const val TAG = "KeyboardDetector"
        const val MIN_KEYBOARD_HEIGHT_RATIO = 0.15
    }

    fun getObserver(): Observable<KeyboardStatus> {
        val rootView = (activity.findViewById<View>(android.R.id.content) as ViewGroup?)
        val windowHeight = DisplayMetrics().let {
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                windowMetrics.bounds.let {
                    displayMetrics.widthPixels = it.width()
                    displayMetrics.heightPixels = it.height()
                }
            } else {
                @Suppress("DEPRECATION") activity.windowManager.defaultDisplay.getMetrics(
                    displayMetrics
                )
            }
            displayMetrics.heightPixels
        }

        return Observable.create<KeyboardStatus> { emitter ->
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                if (rootView == null) {
                    Log.w(TAG, "Root view is null")
                    Logger.w("Root view is null")
                    emitter.onNext(KeyboardStatus(false))
                    return@OnGlobalLayoutListener
                }

                val rect = Rect().apply { rootView.getWindowVisibleDisplayFrame(this) }
                val keyboardHeight = windowHeight - rect.height()

                if (keyboardHeight > windowHeight * MIN_KEYBOARD_HEIGHT_RATIO) {
                    emitter.onNext(KeyboardStatus(true, keyboardHeight))
                } else {
                    emitter.onNext(KeyboardStatus(false))
                }
            }

            rootView?.let {
                it.viewTreeObserver.addOnGlobalLayoutListener(listener)

                emitter.setCancellable {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                }
            }
        }.distinctUntilChanged()
    }

}
