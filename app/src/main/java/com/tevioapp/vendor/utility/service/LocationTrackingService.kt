package com.tevioapp.vendor.utility.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.tevioapp.vendor.utility.location.LocationDetector
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.notification.NotificationProvider
import com.tevioapp.vendor.utility.rx.EventBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service(), LocationDetector.Listener {

    @Inject
    lateinit var notificationProvider: NotificationProvider
    private lateinit var locationDetector: LocationDetector

    override fun onCreate() {
        super.onCreate()
        locationDetector = LocationDetector(applicationContext).withListener(this)
        val notification = notificationProvider.createLocationNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Logger.w("onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationDetector.startLocationDetector(
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8_000)
                .setMinUpdateDistanceMeters(15f)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setWaitForAccurateLocation(true).build(),
            detectorType = LocationDetector.DETECTOR_UPDATES,
            locationAccuracy = 15f,
            resetGpsRequestCount = true
        )
        Logger.w("LocationService started")
        return START_STICKY
    }

    override fun onDestroy() {
        Logger.w("LocationService destroyed")
        locationDetector.stopLocationDetector()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLocationFound(location: Location) {
        EventBus.post(Callback(type = Callback.Type.LOCATION_FOUND, location = location))
    }

    override fun onPermissionPending() {
        Logger.w("Location permission pending")
        EventBus.post(Callback(type = Callback.Type.PERMISSION_REQUIRED))
        stopSelf()
    }

    override fun onGpsPermissionDeny() {
        Logger.w("GPS prompt denied")
        EventBus.post(Callback(type = Callback.Type.GPS_DENY))
        stopSelf()
    }

    override fun onGpsPermissionPending(request: IntentSenderRequest) {
        Logger.w("GPS resolution needed (ignored in Service)")
        EventBus.post(Callback(type = Callback.Type.GPS_REQUIRED, request = request))
        stopSelf()
    }

    override fun onError(message: String) {
        Logger.e("Error: $message")
        EventBus.post(Callback(type = Callback.Type.ERROR, message = message))
        stopSelf()
    }

    override fun onDetectorStatusChanged(loading: Boolean) {
        Logger.d("Detector running = $loading")
    }

    data class Callback(
        val type: Type,
        val request: IntentSenderRequest? = null,
        val message: String? = null,
        val location: Location? = null
    ) {
        enum class Type {
            PERMISSION_REQUIRED, GPS_REQUIRED, GPS_DENY, ERROR, LOCATION_FOUND
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart service if notification is swiped away or task is removed
        val restartServiceIntent =
            Intent(applicationContext, LocationTrackingService::class.java).apply {
                setPackage(packageName)
            }
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 10000, // restart after 10 second
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
