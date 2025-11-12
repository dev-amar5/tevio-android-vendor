package com.tevioapp.vendor.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.extensions.setMultiSpan
import com.tevioapp.vendor.utility.log.Logger
import java.io.File
import java.util.Locale


object CommonMethods {

    /**
     * resolve media source
     * @param url url string
     */
    fun resolveMediaSource(url: String?): Any {
        if (url == null) return ""
        try {
            if (url.startsWith("http", true)) return url
            if (url.contains("base64,", true)) {
                return url
            }
            if (url.startsWith("/")) {  // Handling local file paths
                return Uri.fromFile(File(url))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return url
    }

    fun openEmailClientChooser(
        context: Context, recipients: Array<String>, subject: String, body: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri() // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, recipients)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        // Create chooser
        try {
            val chooser = Intent.createChooser(intent, "Send Email")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Logger.e(e.message.orEmpty())
        }
    }


    fun openUrlInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = url.toUri()
            val chooser = Intent.createChooser(intent, "Open")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") || Build.MODEL.contains(
            "google_sdk"
        ) || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains(
            "Genymotion"
        ) || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith(
            "generic"
        )) || "google_sdk" == Build.PRODUCT
    }

    fun shareLocation(context: Context, latitude: Double, longitude: Double) {
        val locationUri = "https://www.google.com/maps?q=$latitude,$longitude"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this location: $locationUri")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share location via"))
    }

    fun openLocation(context: Context, latitude: Double, longitude: Double, label: String?) {
        val uri = if (label == null) {
            "geo:$latitude,$longitude"
        } else {
            "geo:0,0?q=$latitude,$longitude($label)"
        }
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    }

    fun updateResendLabel(context: Context, textView: TextView, milliSec: Long?) {
        if (milliSec == null) {
            textView.isVisible = false
            return
        }
        textView.isVisible = true
        val totalSeconds = milliSec / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        if (totalSeconds > 0) {
            val blackCoral = ContextCompat.getColor(context, R.color.gray)
            val label = "Resend Code (${
                String.format(
                    Locale.US, "%02d", minutes
                )
            }:${String.format(Locale.US, "%02d", seconds)})"
            textView.text = label.setMultiSpan(color = blackCoral)
            textView.isEnabled = false
        } else {
            val orangeColor = ContextCompat.getColor(context, R.color.orange)
            val label = "Resend Code"
            textView.text =
                label.setMultiSpan(color = orangeColor, underLine = true, bold = true)
            textView.isEnabled = true
        }
    }

    fun shareContent(context: Context, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Tevio Invite")
        }

        val chooser = Intent.createChooser(shareIntent, "Share via")
        context.startActivity(chooser)
    }

    fun openEmailApp(
        context: Context,
        recipient: String = "",
        subject: String = "Add Subject",
        content: String = ""
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$recipient".toUri() // Use empty string if no recipient
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMessageApp(context: Context, message: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "smsto:".toUri()
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    }

    fun openDialerApp(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
        context.startActivity(intent)
    }

    fun playDefaultNotificationSound(context: Context) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone: Ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fixAdjustResizeIssue(
        activity: Activity, onHeightChange: (Int) -> Unit
    ) {
        if (!Build.MANUFACTURER.equals("samsung", ignoreCase = true)) return

        try {
            val contentView = activity.findViewById<View>(android.R.id.content) ?: return

            fun getStatusBarHeight(): Int {
                val resId =
                    activity.resources.getIdentifier("status_bar_height", "dimen", "android")
                return if (resId > 0) activity.resources.getDimensionPixelSize(resId) else 0
            }

            fun getNavigationBarHeight(): Int {
                val resId =
                    activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
                return if (resId > 0) activity.resources.getDimensionPixelSize(resId) else 0
            }

            val statusBarHeight = getStatusBarHeight()
            val navBarHeight = getNavigationBarHeight()

            contentView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                private var lastKeyboardHeight = -1

                override fun onGlobalLayout() {
                    val rect = Rect()
                    contentView.getWindowVisibleDisplayFrame(rect)
                    val visibleHeight = rect.height()
                    val rootHeight = contentView.rootView.height

                    var heightDiff = rootHeight - visibleHeight
                    if (heightDiff > statusBarHeight) heightDiff -= statusBarHeight
                    if (heightDiff > navBarHeight) heightDiff -= navBarHeight

                    val isKeyboardVisible = heightDiff > rootHeight * 0.15
                    val keyboardHeight = if (isKeyboardVisible) heightDiff else 0

                    if (keyboardHeight != lastKeyboardHeight) {
                        lastKeyboardHeight = keyboardHeight
                        onHeightChange.invoke(keyboardHeight)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

