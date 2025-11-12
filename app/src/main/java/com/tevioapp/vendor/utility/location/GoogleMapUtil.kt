package com.tevioapp.vendor.utility.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.PolyUtil
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.Distance
import com.tevioapp.vendor.data.common.Duration
import com.tevioapp.vendor.presentation.common.compoundviews.GlideApp
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.extensions.asCommaSeparatedString
import com.tevioapp.vendor.utility.extensions.isDarkMode
import com.tevioapp.vendor.utility.log.Logger
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt


object GoogleMapUtil {
    fun getCountryCodeFromLatLng(context: Context, latLng: LatLng?): String? {
        val location=latLng?:return null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                return addresses[0].countryCode
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getAddressObjectLatLng(context: Context, latLng: LatLng): Address? {
        try {
            return Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1)
                ?.firstOrNull()?.also {
                    Logger.e(it.toString())
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun Address.getFullAddress(): String {
        val message = "Address not found"
        return try {
            getAddressLine(0) ?: message
        } catch (e: Exception) {
            message
        }
    }

    fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Int {
        val results = FloatArray(1) // Array to store the result
        // Calculate the distance in meters
        Location.distanceBetween(
            latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, results
        )
        return results[0].toInt() // Return the distance in meters
    }

    fun getParabolicCurvePoints(
        start: LatLng, end: LatLng, numberOfPoints: Int = 200
    ): List<LatLng> {
        val points = mutableListOf<LatLng>()
        // Calculate a curve factor based on the distance between the two points
        val curveHeight =
            (end.latitude - start.latitude) / 4  // This controls the sharpness of the curve
        for (i in 0..numberOfPoints) {
            val t = i.toDouble() / numberOfPoints
            // Linearly interpolate between the start and end points
            val lat = (1 - t) * start.latitude + t * end.latitude
            val lng = (1 - t) * start.longitude + t * end.longitude
            // Apply parabolic offset to the latitude
            // The offset should peak at t = 0.5, so adjust the parabola formula
            val offset = 4 * curveHeight * t * (1 - t) // Parabolic shape formula, peaks at t = 0.5
            val curvedLat = lat + offset
            points.add(LatLng(curvedLat, lng))
        }
        return points
    }

    fun setStaticMapInImageView(
        imageView: ImageView, latLng: LatLng?, zoom: Int = 18
    ) {
        if (latLng == null) {
            imageView.setImageDrawable(
                AppCompatResources.getDrawable(
                    imageView.context, R.drawable.ic_placeholder_horizontal
                )
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        } else {
            imageView.post {
                val width = imageView.width
                val height = imageView.height
                GlideApp.with(imageView.context).load(buildString {
                    append("https://maps.googleapis.com/maps/api/staticmap?")
                    append("center=${latLng.latitude},${latLng.longitude}")
                    append("&zoom=$zoom")
                    append("&size=${width}x${height}")
                    append("&key=${imageView.context.getString(R.string.google_map_key)}")
                }).centerCrop().placeholder(R.drawable.ic_placeholder_horizontal).into(imageView)
            }
        }
    }

    fun setDefaultUISettings(map: GoogleMap) {
        map.uiSettings.apply {
            isScrollGesturesEnabledDuringRotateOrZoom = false
            isRotateGesturesEnabled = false
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = false
            isMyLocationButtonEnabled = false
        }
        map.isBuildingsEnabled = false
        map.setMinZoomPreference(AppConstants.MAP_MAX_ZOOM_OUT)
    }

    fun enableCurrentLocation(context: Context, map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true
    }

    fun setMapStyle(context: Context, map: GoogleMap) {
        try {
            if (context.isDarkMode()) {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                )
            }
        } catch (e: Exception) {
            Logger.e("MapsActivity Style not found. Error: ", e)
        }
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    fun decodePoly(encoded: String?): ArrayList<LatLng> {
        if (encoded == null) return arrayListOf()
        val poly = arrayListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    /**
     * Check if a LatLng point is on the given polyline.
     *
     * @param point The LatLng point to check.
     * @param polyline The list of LatLng points representing the polyline.
     * @param tolerance Tolerance in meters (adjust as needed)
     * @return true if the point is on the polyline, false otherwise.
     */
    fun isPointOnPolyline(point: LatLng, polyline: List<LatLng>, tolerance: Double): Boolean {
        return PolyUtil.isLocationOnPath(point, polyline, true, tolerance)
    }

    /**
     * get bound point for map
     * @param radius
     * @param centerLatLng
     * @return List<LatLng>
     */
    fun getBoundPoint(radius: Int, centerLatLng: LatLng): List<LatLng> {
        val radiusInDegrees = radius / 111000.0
        val north: Double = centerLatLng.latitude + radiusInDegrees
        val south: Double = centerLatLng.latitude - radiusInDegrees
        val east: Double = centerLatLng.longitude + radiusInDegrees
        val west: Double = centerLatLng.longitude - radiusInDegrees
        return arrayListOf(LatLng(south, west), LatLng(north, east))
    }

    fun calculateHaversineDistance(points: List<LatLng>): Distance {
        if (points.size < 2) return Distance("0 km", 0)
        val radius = 6371000.0 // Earth's radius in meters
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            val lat1 = points[i].latitude
            val lon1 = points[i].longitude
            val lat2 = points[i + 1].latitude
            val lon2 = points[i + 1].longitude
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a =
                sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                    dLon / 2
                ).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            totalDistance += radius * c
        }
        val text = if (totalDistance >= 1000) {
            String.format(Locale.US, "%.1f km", totalDistance / 1000)
        } else {
            String.format(Locale.US, "%.0f m", totalDistance)
        }
        return Distance(text = text, value = totalDistance.toLong())
    }

    fun calculateDuration(distanceMeter: Double, speedMeterSecond: Double = 5.0): Duration {
        val seconds = (distanceMeter / speedMeterSecond).roundToLong()
        return Duration(value = seconds)
    }

    fun getDurationPair(value: Long?): Pair<String, String>? {
        val seconds = value ?: return null
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours >= 1 && minutes > 0 -> {
                val decimalHour = seconds / 3600.0
                Pair(
                    String.format(Locale.US, "%.1f", decimalHour), "Hour"
                )
            }

            hours >= 1 -> {
                Pair(
                    hours.toString(), "Hour"
                )
            }

            else -> {
                // Only minutes
                Pair(minutes.toString(), "Min")
            }
        }
    }


    /**
     * Opens Google Maps navigation to the given destination.
     *
     * @param context The context used to start the intent.
     * @param latLng The destination latitude,longitude.
     * @param mode Navigation mode: "d" for driving, "w" for walking, "b" for biking.
     */
    fun openGoogleMapsNavigation(context: Context, latLng: LatLng, mode: String = "d") {
        try {
            val uri = "google.navigation:q=${latLng.asCommaSeparatedString()}&mode=$mode".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Google Maps is not installed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to start navigation", Toast.LENGTH_SHORT).show()
        }
    }

    /** Helper to get Google Maps API Key from manifest */
    fun getGoogleApiKey(context: Context): String {
        val ai = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        return ai.metaData.getString("com.google.android.geo.API_KEY").orEmpty()
    }

    fun isLocationUpdated(old: Location?, new: Location?): Boolean {
        return when {
            old == null && new == null -> false // both null → same
            old == null || new == null -> true // one is null → different
            else -> {
                // compare distance in meters
                val distance = old.distanceTo(new)
                distance > 5f // if within 5 meters, treat as same
            }
        }
    }
}