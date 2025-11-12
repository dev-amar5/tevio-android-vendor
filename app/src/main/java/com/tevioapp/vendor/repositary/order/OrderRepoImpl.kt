package com.tevioapp.vendor.repositary.order

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.data.HeatMapData
import com.tevioapp.vendor.data.common.RouteLegInfo
import com.tevioapp.vendor.network.api.GoogleApi
import com.tevioapp.vendor.network.api.OrderApi
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.utility.extensions.asCommaSeparatedString
import com.tevioapp.vendor.utility.extensions.toApplicationJsonBody
import com.tevioapp.vendor.utility.location.GoogleMapUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import org.json.JSONObject
import javax.inject.Inject

class OrderRepoImpl @Inject constructor(
    private val orderApi: OrderApi,
    private val googleApi: GoogleApi,
    @ApplicationContext val context: Context
) : OrderRepo {

    override fun apiHeatMap(): Single<ApiResponse<List<HeatMapData>>> {
        return orderApi.apiHeatMap()
    }

    override fun apiOrderDeliveryKit(request: JSONObject): Single<SimpleApiResponse> {
        return orderApi.apiOrderDeliveryKit(request.toApplicationJsonBody())
    }

    override fun apiDeliverOrder(
        orderId: String,request:JSONObject
    ): Single<SimpleApiResponse> {
        return orderApi.apiDeliverOrder(orderId, request.toApplicationJsonBody())
    }

    override fun apiDeliveryKitPayment(request: JSONObject): Single<ApiResponse<String>> {
        return orderApi.apiDeliveryKitPayment(request.toApplicationJsonBody())
    }

    override fun getDirection(
        origin: LatLng, destination: LatLng, isMockDirection: Boolean
    ): Single<ApiResponse<RouteLegInfo>> {
        return if (isMockDirection) {
            Single.just(getMockDirection(origin, destination))
        } else {
            // Live API call
            val params = hashMapOf(
                "origin" to origin.asCommaSeparatedString().orEmpty(),
                "destination" to destination.asCommaSeparatedString().orEmpty(),
                "alternatives" to "false",
                "key" to getGoogleApiKey()
            )

            googleApi.getDirection(params).map { response ->
                val legs = response.routes.getOrNull(0)?.legs?.getOrNull(0)
                if (legs == null) {
                    getMockDirection(origin, destination)
                } else {
                    val points = mutableListOf<LatLng>()
                    legs.steps.forEach { step ->
                        points.addAll(GoogleMapUtil.decodePoly(step.polyline.points))
                    }
                    ApiResponse<RouteLegInfo>().apply {
                        isSuccessful = true
                        data = RouteLegInfo(
                            points = ArrayList(points),
                            distance = legs.distance,
                            duration = legs.duration,
                            isMock = false
                        )
                        message = "Live route loaded!"
                    }
                }
            }
        }
    }


    /** Helper to get Google Maps API Key from manifest */
    private fun getGoogleApiKey(): String {
        val ai = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        return ai.metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    private fun getMockDirection(
        origin: LatLng, destination: LatLng
    ): ApiResponse<RouteLegInfo> {
        val distance = GoogleMapUtil.calculateHaversineDistance(arrayListOf(origin, destination))
        val duration = GoogleMapUtil.calculateDuration(distance.value.toDouble())
        val parabolicPoints = GoogleMapUtil.getParabolicCurvePoints(
            origin, destination, distance.value.toInt()
        )

        return ApiResponse<RouteLegInfo>().apply {
            isSuccessful = true
            data = RouteLegInfo(
                points = parabolicPoints, distance = distance, duration = duration, isMock = true
            )
            message = "Mock data loaded"
        }
    }

}
