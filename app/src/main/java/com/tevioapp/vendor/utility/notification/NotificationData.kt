package com.tevioapp.vendor.utility.notification

data class NotificationData(
    var title: String, var message: String, val type: String, val data: Map<String, String>? = null
) {
    companion object {
        const val TYPE_LOCATION_TRACKING = "tracking"
        const val TYPE_INCOMING_CALL = "audio-call"
        const val TYPE_UPLOAD_PROGRESS = "upload"
    }
}
