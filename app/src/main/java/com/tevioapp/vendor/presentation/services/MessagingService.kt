package com.tevioapp.vendor.presentation.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.notification.NotificationData
import com.tevioapp.vendor.utility.notification.NotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationProvider: NotificationProvider
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(
            "MessagingService",
            "From: ${remoteMessage.from} ${remoteMessage.notification.toString()}  ${remoteMessage.data}"
        )
        val notificationData = NotificationData(
            title = remoteMessage.notification?.title ?: "New Notification",
            message = remoteMessage.notification?.body ?: "A new notification received.",
            type = remoteMessage.data["type"].orEmpty(),
            data = remoteMessage.data
        )
        if (notificationData.type == NotificationData.TYPE_INCOMING_CALL) {
            notificationProvider.showCallNotification(notificationData)
        } else {
            notificationProvider.showDefaultNotification(notificationData)
        }
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Logger.e("Refreshed token: $token")

    }


}