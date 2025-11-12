package com.tevioapp.vendor.utility.security

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthUtil(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onFailure: (errorMsg: String) -> Unit
) {
    private val biometricManager = BiometricManager.from(activity)
    private val executor = ContextCompat.getMainExecutor(activity)

    private val biometricPrompt =
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure("Authentication error: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure("Authentication failed. Please try again.")
            }
        })


    fun canAuthenticate(): Boolean {
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                onFailure("No biometric hardware available.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onFailure("Biometric hardware currently unavailable.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // No biometrics enrolled - prompt to enroll
                showEnrollDialog()
                false
            }

            else -> {
                onFailure("Cannot authenticate using biometrics.")
                false
            }
        }
    }

    private fun showEnrollDialog() {
        AlertDialog.Builder(activity).setTitle("Biometric Enrollment Required")
            .setMessage("Please enroll your biometrics (fingerprint, face, etc.) in device settings to use biometric login.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openBiometricEnrollSettings()
            }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun openBiometricEnrollSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                })
            }
        } catch (e: ActivityNotFoundException) {
            // Some devices may not support ACTION_BIOMETRIC_ENROLL, fallback to security settings
            try {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                activity.startActivity(intent)
            } catch (ex: Exception) {
                onFailure("Unable to open settings to enroll biometrics.")
            }
        }
    }

    fun authenticate(
        title: String = "Login Authentication",
        message: String = "Use biometric or device PIN to login"
    ) {
        if (canAuthenticate()) {
            val promptInfo =
                BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(message)
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    ).build()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    companion object {
        fun hasBiometricHardware(activity: FragmentActivity): Boolean {
            return BiometricManager.from(activity).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            ) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        }
    }
}
