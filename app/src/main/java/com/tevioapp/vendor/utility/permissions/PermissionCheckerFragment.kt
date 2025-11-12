package com.tevioapp.vendor.utility.permissions


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tevioapp.vendor.utility.log.Logger

/**
 * This fragment holds the single permission request and holds it until the flow is completed
 */
class PermissionCheckerFragment : Fragment() {

    private var quickPermissionsRequest: QuickPermissionsRequest? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openAppSettingsLauncher: ActivityResultLauncher<Intent>

    interface QuickPermissionsCallback {
        fun shouldShowRequestPermissionsRationale(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsGranted(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsPermanentlyDenied(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsDenied(quickPermissionsRequest: QuickPermissionsRequest?)
    }

    companion object {
        fun newInstance(): PermissionCheckerFragment = PermissionCheckerFragment()
    }

    private var mListener: QuickPermissionsCallback? = null

    fun setListener(listener: QuickPermissionsCallback) {
        mListener = listener
        Logger.d("onCreate: listeners set")
    }

    private fun removeListener() {
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the launcher with the permission request contract
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Permissions result callback
            val permissionsArray = permissions.keys.toTypedArray()
            val grantResults =
                IntArray(permissionsArray.size) { if (permissions[permissionsArray[it]] == true) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED }
            handlePermissionResult(permissionsArray, grantResults)
        }

        openAppSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val permissions = quickPermissionsRequest?.permissions ?: emptyArray()
            val grantResults = IntArray(permissions.size)
            permissions.forEachIndexed { index, s ->
                grantResults[index] = context?.let { ActivityCompat.checkSelfPermission(it, s) }
                    ?: PackageManager.PERMISSION_DENIED
            }
            handlePermissionResult(permissions, grantResults)

        }

        Logger.d("onCreate: permission fragment created")
    }

    fun setRequestPermissionsRequest(quickPermissionsRequest: QuickPermissionsRequest?) {
        this.quickPermissionsRequest = quickPermissionsRequest
    }

    private fun removeRequestPermissionsRequest() {
        quickPermissionsRequest = null
    }

    fun clean() {
        if (quickPermissionsRequest != null) {
            // permission request flow is finishing
            // let the caller receive callback about it
            if ((quickPermissionsRequest?.deniedPermissions?.size
                    ?: 0) > 0
            ) mListener?.onPermissionsDenied(quickPermissionsRequest)

            removeRequestPermissionsRequest()
            removeListener()
        } else {
            Logger.w(
                "clean: QuickPermissionsRequest has already completed its flow. " + "No further callbacks will be called for the current flow."
            )
        }
    }


    fun requestPermissionsFromUser() {
        val permissionsToRequest = quickPermissionsRequest?.permissions.orEmpty()

        val permissionsToRequestFiltered = permissionsToRequest.filter { permission ->
            activity?.applicationContext?.let {
                ContextCompat.checkSelfPermission(
                    it, permission
                )
            } != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequestFiltered.isNotEmpty()) {
            // Request permissions using the launcher
            requestPermissionLauncher.launch(permissionsToRequestFiltered)
        } else {
            Logger.d("All required permissions are already granted.")
        }
    }

    private fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        // add a check with the permissions list
        // if the permissions list is empty, that means system has told that permissions request
        // is invalid somehow or discarded the previous request
        // this can happen in case when the multiple permissions requests are sent
        // simultaneously to the system
        if (permissions.isEmpty()) {
            Logger.w(
                "handlePermissionResult: Permissions result discarded. You might have called multiple permissions request simultaneously"
            )
            return
        }

        if (PermissionsUtil.hasSelfPermission(context, permissions)) {

            // set the denied permissions to empty as all the permissions are granted
            // this is required as clean will be called which can invoke on permissions denied
            // if it finds some permissions in the denied list
            quickPermissionsRequest?.deniedPermissions = emptyArray()

            // we are good to go!
            mListener?.onPermissionsGranted(quickPermissionsRequest)

            // flow complete
            clean()
        } else {
            // we are still missing permissions
            val deniedPermissions = PermissionsUtil.getDeniedPermissions(permissions, grantResults)
            quickPermissionsRequest?.deniedPermissions = deniedPermissions

            // check if rationale dialog should be shown or not
            var shouldShowRationale = true
            var isPermanentlyDenied = false
            for (element in deniedPermissions) {
                val rationale = shouldShowRequestPermissionRationale(element)
                if (!rationale) {
                    shouldShowRationale = false
                    isPermanentlyDenied = true
                    break
                }
            }

            if (quickPermissionsRequest?.handlePermanentlyDenied == true && isPermanentlyDenied) {

                quickPermissionsRequest?.permanentDeniedMethod?.let {
                    // get list of permanently denied methods
                    quickPermissionsRequest?.permanentlyDeniedPermissions =
                        PermissionsUtil.getPermanentlyDeniedPermissions(
                            this, permissions, grantResults
                        )
                    mListener?.onPermissionsPermanentlyDenied(quickPermissionsRequest)
                    return
                }

                val alert = activity?.createAlertDialog(
                    title = "Alert", // You can provide a title or leave it as null
                    message = quickPermissionsRequest?.permanentlyDeniedMessage.orEmpty(),
                    positiveButtonText = "SETTINGS"
                ) { openAppSettings() }

                alert?.show()
                return
            }

            // if should show rationale dialog
            if (quickPermissionsRequest?.handleRationale == true && shouldShowRationale) {

                quickPermissionsRequest?.rationaleMethod?.let {
                    mListener?.shouldShowRequestPermissionsRationale(quickPermissionsRequest)
                    return
                }
                val alert = activity?.createAlertDialog(
                    title = "Alert", // You can provide a title or leave it as null
                    message = quickPermissionsRequest?.rationaleMessage.orEmpty(),
                    positiveButtonText = "TRY AGAIN"
                ) { requestPermissionsFromUser() }

                alert?.show()
                return
            }

            // if handlePermanentlyDenied = false and handleRationale = false
            // This will call permissionsDenied method
            clean()
        }
    }

    fun openAppSettings() {
        if (quickPermissionsRequest != null) {
            val intent = Intent(
                ACTION_APPLICATION_DETAILS_SETTINGS,
                fromParts("package", requireActivity().packageName, null)
            )
            openAppSettingsLauncher.launch(intent)
        } else {
            Logger.w(
                "openAppSettings: QuickPermissionsRequest has already completed its flow. Cannot open app settings"
            )
        }
    }

    private fun FragmentActivity.createAlertDialog(
        title: String?, message: String, positiveButtonText: String, onPositiveClick: () -> Unit
    ): AlertDialog {
        return AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> onPositiveClick() }.setCancelable(true)
            .create()
    }
}

