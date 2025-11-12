package com.tevioapp.vendor.utility.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.tevioapp.vendor.R
import com.tevioapp.vendor.presentation.views.splash.SplashActivity
import com.tevioapp.vendor.presentation.views.support.audio.IncomingAudioCallActivity
import com.tevioapp.vendor.utility.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class NotificationProvider @Inject constructor(@ApplicationContext private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private fun ensureChannel(channel: NotificationChannel) {
        val existing = notificationManager.getNotificationChannel(channel.id)
        if (existing == null) notificationManager.createNotificationChannel(channel)
    }

    fun getDefaultIntent(notificationData: NotificationData): Intent {
        return Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            notificationData.data?.forEach { (key, value) -> putExtra(key, value) }
        }
    }

    fun showDefaultNotification(notificationData: NotificationData): Int {
        val channel = getNotificationChannel(notificationData.type)
        ensureChannel(channel)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(context, R.color.orange))
            .setContentTitle(notificationData.title).setContentText(notificationData.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationData.message))
            .setSound(soundUri).setAutoCancel(true).setContentIntent(
                PendingIntent.getActivity(
                    context,
                    Random.nextInt(1000, 10000),
                    getDefaultIntent(notificationData),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
        val notificationId = Random.Default.nextInt(1000, 10000)
        notificationManager.notify(notificationId, notification)
        return notificationId
    }

    fun showCallNotification(data: NotificationData): Int {
        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val attr =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
        val channel = NotificationChannel(
            "call_channel", "In-App Call", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Used for incoming in-app calls"
            setSound(ringtone, attr)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        ensureChannel(channel)
        val notificationId = Random.nextInt(1000, 9999)
        val intent = Intent(context, IncomingAudioCallActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.data?.forEach { (k, v) -> putExtra(k, v) }
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(context, R.color.orange)).setContentTitle(data.title)
            .setContentText(data.message).setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setSound(ringtone)
            .setTimeoutAfter(IncomingAudioCallActivity.RING_TIME_OUT)
            .setContentIntent(pendingIntent).setAutoCancel(true).setOngoing(true).build()
        notificationManager.notify(notificationId, notification)
        context.startActivity(intent)
        return notificationId
    }


    fun removeNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun createLocationNotification(): Notification {
        val channel = getNotificationChannel(NotificationData.TYPE_LOCATION_TRACKING)
        ensureChannel(channel)

        val moreInfoIntent = Intent(Intent.ACTION_VIEW).apply {
            data = AppConstants.URL_PRIVACY_POLICY.toUri()
        }
        val moreInfoPendingIntent = PendingIntent.getActivity(
            context, 1, moreInfoIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val stopPendingIntent = PendingIntent.getActivity(
            context, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channel.id).setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked").setSmallIcon(R.drawable.ic_location)
            .setOngoing(true).setSilent(true).setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.ic_security_info, "More Info", moreInfoPendingIntent)
            .addAction(R.drawable.ic_close_circle, "Stop", stopPendingIntent).build()
    }

    private fun getNotificationChannel(type: String?): NotificationChannel {
        return when (type) {
            NotificationData.TYPE_LOCATION_TRACKING -> NotificationChannel(
                "location_channel", "Location Tracking", NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
                description = "Used for background location tracking"
            }

            NotificationData.TYPE_UPLOAD_PROGRESS -> NotificationChannel(
                "upload_channel", "Upload Notifications", NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
                description = "Used for showing files upload progress and results"
            }

            else -> NotificationChannel(
                "default", "General Notifications", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Used for general app notifications"
            }
        }
    }
}
