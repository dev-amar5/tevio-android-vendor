package com.tevioapp.vendor.utility.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
object PermissionsUtil {

    fun getDeniedPermissions(permissions: Array<String>, grantResults: IntArray): Array<String> =
        permissions.filterIndexed { index, s ->
            grantResults[index] == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

    fun getPermanentlyDeniedPermissions(
        fragment: Fragment, permissions: Array<String>, grantResults: IntArray
    ): Array<String> = permissions.filterIndexed { index, s ->
        grantResults[index] == PackageManager.PERMISSION_DENIED && !fragment.shouldShowRequestPermissionRationale(
            s
        )
    }.toTypedArray()

    /**
     * Returns true if the Activity has access to all given permissions.
     * Always returns true on platforms below M.
     *
     * @see Activity.checkSelfPermission
     */
    fun hasSelfPermission(activity: Context?, permissions: Array<String>): Boolean {
        // Verify that all required permissions have been granted
        activity?.let {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        activity, permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }


    fun getForPicker(): Array<String> {
        val permissions = arrayListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.toTypedArray()
    }

    fun requestPostNotificationPermission(context: Context, onDone: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.runWithPermissions(Manifest.permission.POST_NOTIFICATIONS) {
                onDone.invoke()
            }
        } else {
            onDone.invoke()
        }
    }

    fun getForLocation(): Array<String> {
        val permissions = arrayListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        return permissions.toTypedArray()
    }


    fun getForAudioCalling(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.BLUETOOTH_CONNECT
            permissions += Manifest.permission.READ_PHONE_STATE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.FOREGROUND_SERVICE
        }

        return permissions.toTypedArray()
    }


    fun getRecording(): Array<String> {
        val permissions = arrayListOf<String>()
        permissions.add(Manifest.permission.RECORD_AUDIO)
        return permissions.toTypedArray()
    }


}