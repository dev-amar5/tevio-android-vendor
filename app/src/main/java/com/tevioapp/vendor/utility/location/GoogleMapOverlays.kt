package com.tevioapp.vendor.utility.location

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.VehicleInfo
import com.tevioapp.vendor.data.common.RouteLegInfo
import com.tevioapp.vendor.databinding.ViewMarkerEtaBinding
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.extensions.dpToPx
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.extensions.isDarkMode
import com.tevioapp.vendor.utility.util.AppSettings

class GoogleMapOverlays(
    private val context: Context,
    private val vehicleInfo: VehicleInfo?,
    private val googleMap: GoogleMap
) {
    private var _startMarker: Marker? = null
    private var _endMarker: Marker? = null
    private var _trackPolyLine: Polyline? = null
    private var _parabolaPolyLine: Polyline? = null
    private var _currentLocation: Location? = null
    private var _destination: LatLng? = null
    private var _routeLegInfo: RouteLegInfo? = null
    private var markerAnimator: ValueAnimator? = null
    private var bitmapEmpty: Bitmap? = null
    private var recentDurationBitmap: Bitmap? = null
    private var recentDuration: Long? = null
    private val widthPolyLine =
        context.resources.getDimensionPixelSize(R.dimen._2sdp).toFloat()

    fun setCurrentLocation(location: Location?) {
        _currentLocation = location
    }

    fun setDestination(destination: LatLng?) {
        _destination = destination
    }

    fun setRouteInfo(routeLegInfo: RouteLegInfo?) {
        _routeLegInfo = routeLegInfo
    }

    fun updateMapView() {
        setStartMarker()
        val start = _currentLocation ?: return
        val destination = _destination
        if (destination == null) {
            removeEndMarker()
            removeAllPolyLines()
            cameraOnPosition(start.getLatLng())
            return
        }

        val routeInfo = _routeLegInfo
        if (routeInfo == null) {
            removeAllPolyLines()
            setOrUpdateEndMarker(destination, null)
            cameraOnLatLngBound(arrayListOf(start.getLatLng(), destination), animate = false)
            return
        }

        val points = routeInfo.points.toMutableList()
        points.add(0, start.getLatLng())
        points.add(destination)
        if (routeInfo.isMock) {
            val distance = routeInfo.distance?.value ?: 0
            if (distance > 200) {
                _parabolaPolyLine =
                    updatePolyline(_parabolaPolyLine, points, getPolyLineColor(), true)
                _trackPolyLine = updatePolyline(
                    _trackPolyLine,
                    arrayListOf(start.getLatLng(), destination),
                    ContextCompat.getColor(context, R.color.poly_line_shadow),
                    true
                )
            } else {
                _parabolaPolyLine?.remove()
                _parabolaPolyLine = null
                _trackPolyLine = updatePolyline(
                    _trackPolyLine,
                    arrayListOf(start.getLatLng(), destination),
                    getPolyLineColor(),
                    true
                )
            }
        } else {
            _parabolaPolyLine?.remove()
            _parabolaPolyLine = null
            _trackPolyLine = updatePolyline(
                _trackPolyLine, points, getPolyLineColor(), false
            )
        }
        setOrUpdateEndMarker(destination, _routeLegInfo?.duration?.value)
        cameraOnLatLngBound(arrayListOf(start.getLatLng(), destination))
    }

    private fun getPolyLineColor(): Int {
        return when (googleMap.mapType) {
            GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_TERRAIN -> return if (context.isDarkMode()) Color.WHITE else Color.BLACK
            else -> Color.WHITE
        }
    }

    private fun removeEndMarker() {
        _endMarker?.remove()
        _endMarker = null
    }

    private fun removeAllPolyLines() {
        _trackPolyLine?.remove()
        _trackPolyLine = null
        _parabolaPolyLine?.remove()
        _parabolaPolyLine = null
    }

    private fun updatePolyline(
        polyline: Polyline?, points: List<LatLng>, @ColorInt color: Int, isDotted: Boolean
    ): Polyline {
        return if (polyline == null) {
            val options =
                PolylineOptions().width(widthPolyLine).jointType(JointType.ROUND).color(color)
                    .zIndex(Z_LINE)
            if (isDotted) {
                options.pattern(listOf(Dash(15f), Gap(10f)))
            } else {
                options.startCap(RoundCap()).endCap(RoundCap())
            }
            options.addAll(points)
            googleMap.addPolyline(options)
        } else {
            // Just update existing line → no flash
            polyline.apply {
                this.color = color
                this.width = widthPolyLine
                if (isDotted) {
                    this.pattern = (listOf(Dash(15f), Gap(10f)))
                } else {
                    this.pattern = null
                }
                this.points = points
            }
            polyline
        }
    }


    private fun setOrUpdateEndMarker(position: LatLng, duration: Long?) {
        val etaBitmap: Bitmap = when {
            duration == null -> {
                bitmapEmpty ?: createEndMarkerView(null).also { bitmapEmpty = it }
            }

            duration == recentDuration && recentDurationBitmap != null -> {
                recentDurationBitmap!!
            }

            else -> {
                createEndMarkerView(duration).also {
                    recentDuration = duration
                    recentDurationBitmap = it
                }
            }
        }
        if (_endMarker == null) {
            _endMarker = googleMap.addMarker(
                MarkerOptions().position(position)
                    .icon(BitmapDescriptorFactory.fromBitmap(etaBitmap)).anchor(0.5f, 0.9f)
                    .zIndex(Z_DESTINATION)
            )
        } else {
            _endMarker?.position = position
            _endMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(etaBitmap))
        }
    }


    private fun setStartMarker() {
        val location = _currentLocation ?: run {
            removeStartMarker()
            return
        }
        val newLatLng = location.getLatLng()
        try {
            if (_startMarker == null) {
                createStartMarker(newLatLng)
            } else {
                animateMarkerMove(_startMarker!!, newLatLng)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeStartMarker() {
        try {
            markerAnimator?.cancel()
            _startMarker?.remove()
            _startMarker = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createStartMarker(position: LatLng) {
        val icon = when (vehicleInfo?.vehicleType.orEmpty()) {
            Enums.VEHICLE_TYPE_CAR -> getResizedBitmapDescriptor(
                resId = R.drawable.ic_car_map,
                height = context.dpToPx(30f),
                width = context.dpToPx(18f)
            )

            else -> getResizedBitmapDescriptor(
                resId = R.drawable.ic_scooter_map,
                height = context.dpToPx(30f),
                width = context.dpToPx(30f)
            )
        }
        try {
            _startMarker = googleMap.addMarker(
                MarkerOptions().position(position).icon(icon).anchor(0.5f, 0.5f).zIndex(Z_ORIGIN)
                    .flat(true)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateMarkerMove(marker: Marker, to: LatLng) {
        try {
            val from = marker.position
            val startRotation = marker.rotation
            val bearing = calculateBearing(from, to)
            // --- Distance in meters
            val results = FloatArray(1)
            Location.distanceBetween(
                from.latitude, from.longitude, to.latitude, to.longitude, results
            )
            val distance = results[0]
            // --- Duration (min 300ms, max 2000ms)
            val duration = distance.coerceIn(300f, 2000f).toLong()
            // Cancel previous animation
            markerAnimator?.cancel()
            markerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    try {
                        val v = anim.animatedFraction
                        // Interpolate position
                        val lat = (to.latitude - from.latitude) * v + from.latitude
                        val lng = (to.longitude - from.longitude) * v + from.longitude
                        marker.position = LatLng(lat, lng)
                        // Interpolate rotation
                        val normalizeEnd = (bearing - startRotation + 360) % 360
                        val direction = if (normalizeEnd > 180) -1 else 1
                        val rotation = if (direction > 0) {
                            startRotation + normalizeEnd * v
                        } else {
                            startRotation - (360 - normalizeEnd) * v
                        }
                        marker.rotation = (rotation + 360) % 360
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cancel()
                    }
                }
            }
            markerAnimator?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateBearing(from: LatLng, to: LatLng): Float {
        return try {
            val lat1 = Math.toRadians(from.latitude)
            val lon1 = Math.toRadians(from.longitude)
            val lat2 = Math.toRadians(to.latitude)
            val lon2 = Math.toRadians(to.longitude)

            val dLon = lon2 - lon1
            val y = Math.sin(dLon) * Math.cos(lat2)
            val x =
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

            ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0f
        }
    }

    /**
     * Normal camera bounds (used only when destination missing)
     */
    private fun cameraOnLatLngBound(list: List<LatLng>, animate: Boolean = true) {
        if (list.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        list.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        val update = CameraUpdateFactory.newLatLngBounds(/* bounds = */ bounds,/* padding = */
            context.resources.getDimensionPixelSize(R.dimen.google_map_bound_padding)
        )
        if (animate) {
            googleMap.stopAnimation()
            googleMap.animateCamera(update)
        } else {
            googleMap.moveCamera(update)
        }
    }


    fun cameraOnPosition(latLng: LatLng?, animate: Boolean = true) {
        if (latLng == null) return
        try {
            val s = CameraUpdateFactory.newLatLngZoom(
                latLng, AppConstants.MAP_ZOOM_SMALL
            )
            if (animate) googleMap.animateCamera(s)
            else googleMap.moveCamera(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createEndMarkerView(duration: Long?): Bitmap {
        val pair = GoogleMapUtil.getDurationPair(duration)
        val binding = ViewMarkerEtaBinding.inflate(LayoutInflater.from(context), null, false)
        binding.tvDuration1.text = pair?.first
        binding.tvDuration2.text = pair?.second
        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        binding.root.layout(0, 0, binding.root.measuredWidth, binding.root.measuredHeight)
        val bitmap = createBitmap(binding.root.measuredWidth, binding.root.measuredHeight)
        val canvas = Canvas(bitmap)
        binding.root.draw(canvas)
        return bitmap
    }

    private fun getResizedBitmapDescriptor(
        @DrawableRes resId: Int, width: Int, height: Int
    ): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val scaledBitmap = bitmap.scale(width, height, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }


    fun isReachedToDestination(): Boolean {
        val current = _currentLocation ?: return false
        val destination = _destination ?: return false
        val results = FloatArray(1)
        Location.distanceBetween(
            current.latitude,
            current.longitude,
            destination.latitude,
            destination.longitude,
            results
        )
        val distanceInMeters = results[0]
        return distanceInMeters <= AppSettings.getMinimumReachDistance()
    }

    companion object {
        const val Z_ORIGIN = 0.4f
        const val Z_DESTINATION = 0.4f
        const val Z_LINE = 0.1f
    }
}
