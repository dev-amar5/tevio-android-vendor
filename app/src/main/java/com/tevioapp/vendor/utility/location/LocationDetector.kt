package com.tevioapp.vendor.utility.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.utility.log.Logger


/**
 * Created by Arvind on 13-09-2017.
 */
class LocationDetector(private val activity: Context) {
    private var maxGpsRequestCount = 3
    private var gpsRequestCount = 0
    private var detectorType = 0
    private var locationAccuracy: Float? = null
    var running: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                listener?.onDetectorStatusChanged(value)
                Logger.d("onDetectorStatusChanged $field")
            }
        }
    private val mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
    private val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private val mLocationRequestDefault =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMinUpdateDistanceMeters(0f).setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(true).build()

    private var mLocationRequest = mLocationRequestDefault
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.apply {
                val neededAccuracy = locationAccuracy ?: Float.MAX_VALUE
                Logger.d(
                    "onLocationResult needAcc=$neededAccuracy accuracyAccepted=${this.accuracy <= neededAccuracy} $this"
                )
                if (this.accuracy <= neededAccuracy) {
                    if (detectorType == DETECTOR_ONE_SHOT) stopLocationDetector()
                    BaseApp.instance.lastLocation = this
                    listener?.onLocationFound(this)
                }
            }
        }
    }

    private var listener: Listener? = null
    fun withListener(listener: Listener): LocationDetector {
        this.listener = listener
        return this
    }

    fun lastLocation(): Location? {
        return BaseApp.instance.lastLocation
    }

    private fun doInitialization() {
        Logger.d("doInitialization")
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listener?.onPermissionPending()
            Logger.d("onPermissionPending")
            return
        }
        mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null && isLocationAccurate(location)) {
                Logger.d("Use last known location")
                BaseApp.instance.lastLocation = location
                listener?.onLocationFound(location)
                if (detectorType == DETECTOR_UPDATES) {
                    requestNewLocation()
                }
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            requestNewLocation()
        }

    }

    private fun requestNewLocation() {
        Logger.d("requestNewLocation")
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val mLocationSettingsRequest = builder.build()
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(successListener)
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnFailureListener(onFailureListener)
    }

    /**
     * Check if the last known location is accurate (≤10m & updated within 2 minutes)
     */
    private fun isLocationAccurate(location: Location?): Boolean {
        val accuracy = locationAccuracy ?: 50 // Accuracy ≤ 10 meters
        return location != null && location.accuracy <= accuracy.toInt() && System.currentTimeMillis() - location.time < 2 * 60 * 1000 // Less than 2 minutes old
    }

    fun onActivityResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_CANCELED) {
            listener?.onGpsPermissionDeny()
            Logger.d("onGpsPermissionDeny")
        } else doInitialization()
    }


    fun startLocationDetector(
        detectorType: Int,
        locationRequest: LocationRequest = mLocationRequestDefault,
        locationAccuracy: Float? = null,
        resetGpsRequestCount: Boolean = false
    ) {
        this.detectorType = detectorType
        if (resetGpsRequestCount) {
            gpsRequestCount = 0
        }
        this.locationAccuracy = locationAccuracy
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        mLocationRequest = locationRequest
        doInitialization()
    }

    fun stopLocationDetector() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener { _ ->
            running = false
        }
    }

    fun fetchLastLocation(onSuccess: ((Location) -> Unit)? = null) {
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                BaseApp.instance.lastLocation = location
                onSuccess?.invoke(location)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private val successListener: OnSuccessListener<LocationSettingsResponse> = OnSuccessListener {
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            running = true
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback, Looper.myLooper()
            )
        } else {
            listener?.onPermissionPending()
            Logger.d("onPermissionPending")
        }
    }
    private val onFailureListener = OnFailureListener { e: Exception ->
        when ((e as ApiException).statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                if (e is ResolvableApiException) {
                    gpsRequestCount++
                    if (gpsRequestCount <= maxGpsRequestCount) {
                        Logger.d("onGpsPermissionPending")
                        listener?.onGpsPermissionPending(
                            IntentSenderRequest.Builder(e.resolution).build()
                        )
                    }
                }
            }

            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                val errorMessage =
                    "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                listener?.onError(errorMessage)
                Logger.e("onError $errorMessage")
            }
        }
    }


    companion object {
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 0
        const val DETECTOR_ONE_SHOT = 1
        const val DETECTOR_UPDATES = 2
    }

    interface Listener {
        fun onDetectorStatusChanged(loading: Boolean) {
            // Optional override
        }

        fun onError(message: String) {
            // Optional override
        }

        fun onGpsPermissionDeny() {
            // Optional override
        }

        fun onLocationFound(location: Location)
        fun onPermissionPending()
        fun onGpsPermissionPending(request: IntentSenderRequest)
    }

    fun isSuspectLocation(location: Location): Boolean {
        // 1. Basic mock provider check
        if (location.isFromMockProvider) {
            Logger.w("Location is from mock provider")
            return true
        }
        // 3. Unusually high accuracy (e.g., too good to be true)
        if (location.accuracy < 2.0f) {
            Logger.w("Accuracy too perfect: ${location.accuracy}")
            return true
        }
        // 4. Unrealistic speed (e.g., teleportation)
        if (location.speed > 150) { // > 150 m/s ~540 km/h
            Logger.w("Speed too high: ${location.speed} m/s")
            return true
        }
        return false // Passed all checks
    }

}