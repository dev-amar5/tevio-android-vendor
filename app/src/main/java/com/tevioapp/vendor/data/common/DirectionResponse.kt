package com.tevioapp.vendor.data.common

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

class DirectionResponse(
    val routes: List<Route>
) {
    data class Route(
        val legs: List<Leg>, @SerializedName("overview_polyline") val overviewPolyline: Polyline
    )

    data class Leg(
        val steps: List<Step>, val distance: Distance, val duration: Duration
    )

    data class Step(
        val polyline: Polyline
    )

    data class Polyline(
        val points: String
    )
}


data class Distance(
    /**
     * text : 4.9 km
     * value : 4896
     */
    val text: String? = null, val value: Long = 0L
)

data class Duration(
    /**
     * value : 721 in seconds
     */
    val value: Long = 0
)

data class RouteLegInfo(
    val points: List<LatLng>,
    val distance: Distance?,
    val duration: Duration?,
    val isMock: Boolean
)
