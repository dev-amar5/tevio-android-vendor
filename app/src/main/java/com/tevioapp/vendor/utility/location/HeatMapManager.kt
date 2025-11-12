package com.tevioapp.vendor.utility.location

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider

class HeatMapManager(
    private val googleMap: GoogleMap, private val maxPoints: Int = 10000, // limit to avoid OOM
    private val radius: Int = 10, private val opacity: Double = 0.7
) {
    private var heatmapProvider: HeatmapTileProvider? = null
    private var tileOverlay: TileOverlay? = null
    private var allPoints: List<LatLng> = emptyList()

    // For weighted heatmap, keep a list of WeightedLatLng instead
    fun setPoints(points: List<LatLng>) {
        this.allPoints = points
        updateHeatmap() // Initial render
    }

    fun enableDynamicUpdate() {
        googleMap.setOnCameraIdleListener {
            updateHeatmap()
        }
    }

    private fun updateHeatmap() {
        if (allPoints.isEmpty()) return

        // Get visible region bounds
        val visibleBounds = googleMap.projection.visibleRegion.latLngBounds

        // Filter points within visible region
        var visiblePoints = allPoints.filter { visibleBounds.contains(it) }

        // If still too many points, sample
        if (visiblePoints.size > maxPoints) {
            visiblePoints = visiblePoints.shuffled().take(maxPoints)
        }

        if (visiblePoints.isEmpty()) {
            // Remove heatmap if no points visible
            tileOverlay?.remove()
            tileOverlay = null
            return
        }

        // Prepare gradient
        val colors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val startPoints = floatArrayOf(0.2f, 0.5f, 1.0f)
        val gradient = Gradient(colors, startPoints)

        // Remove previous overlay
        tileOverlay?.remove()

        // Create new heatmap provider
        heatmapProvider = HeatmapTileProvider.Builder()
            .data(visiblePoints) // Use .weightedData() for weighted points
            .radius(radius).opacity(opacity).gradient(gradient).build().apply {
                // Add overlay
                tileOverlay = googleMap.addTileOverlay(TileOverlayOptions().tileProvider(this))
            }
    }
}
