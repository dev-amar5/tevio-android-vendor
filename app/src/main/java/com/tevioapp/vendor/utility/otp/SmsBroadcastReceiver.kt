package com.tevioapp.vendor.utility.otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import java.util.regex.Pattern

class SmsBroadcastReceiver(
    private val onOtp: (String?) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
        val extras = intent.extras ?: return
        val status = extras[SmsRetriever.EXTRA_STATUS] as? com.google.android.gms.common.api.Status
        when (status?.statusCode) {
            CommonStatusCodes.SUCCESS ->
                onOtp(extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)?.let(::extractOtp))
            CommonStatusCodes.TIMEOUT -> onOtp(null)
        }
    }
    private fun extractOtp(msg: String): String? =
        Pattern.compile("\\b\\d{4,8}\\b").matcher(msg).takeIf { it.find() }?.group(0)
}
