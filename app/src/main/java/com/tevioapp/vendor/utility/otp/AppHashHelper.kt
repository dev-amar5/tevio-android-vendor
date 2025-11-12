package com.tevioapp.vendor.utility.otp

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.tevioapp.vendor.utility.log.Logger
import org.json.JSONArray
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class AppHashHelper(private val context: Context) {

    /**
     * Generates and logs all app signature hashes used by SMS Retriever API.
     * Works for both debug and release builds automatically.
     * Compatible with Android 13+ (GET_SIGNING_CERTIFICATES)
     */
    fun getAppSignatures(): JSONArray {
        val jsonArray = JSONArray()
        try {
            val packageName = context.packageName
            val packageInfo = context.packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNING_CERTIFICATES
            )

            val signatures = packageInfo.signingInfo?.apkContentsSigners

            signatures?.forEach { signature ->
                val hash = generateHash(packageName, signature.toCharsString())
                jsonArray.put(hash)
                Logger.d("App hash for [$packageName]: $hash")
            }
        } catch (e: Exception) {
            Logger.e("Error generating hash: ${e.message}", e)
        }
        return jsonArray
    }

    private fun generateHash(packageName: String, signature: String): String {
        val appInfo = "$packageName $signature"
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
        val hashSignature = messageDigest.digest().copyOfRange(0, 9)
        val base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
        return base64Hash.substring(0, 11)
    }
}
