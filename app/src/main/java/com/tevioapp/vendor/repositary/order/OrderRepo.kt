package com.tevioapp.vendor.repositary.order

import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.data.DeliveryKitDetail
import com.tevioapp.vendor.data.HeatMapData
import com.tevioapp.vendor.data.Order
import com.tevioapp.vendor.data.common.RouteLegInfo
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import io.reactivex.Single
import org.json.JSONObject

interface OrderRepo {
    fun apiDeliveryKitOrderDetail(): Single<ApiResponse<DeliveryKitDetail>>
    fun apiOrderDeliveryKit(request: JSONObject): Single<SimpleApiResponse>
    fun apiDeliveryKitPayment(request: JSONObject): Single<ApiResponse<String>>
    fun apiHeatMap(): Single<ApiResponse<List<HeatMapData>>>
    fun apiDeliverOrder(orderId: String,request:JSONObject): Single<SimpleApiResponse>
    fun getDirection(
        origin: LatLng,
        destination: LatLng,
        isMockDirection: Boolean
    ): Single<ApiResponse<RouteLegInfo>>

    fun apiOrderList(type: String, pageNo: Int, limit: Int): Single<PagingResponse<List<Order>>>
    fun apiOrderDetail(orderId: String): Single<ApiResponse<Order>>
}