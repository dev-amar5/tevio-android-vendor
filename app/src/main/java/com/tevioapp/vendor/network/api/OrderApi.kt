package com.tevioapp.vendor.network.api

import com.tevioapp.vendor.data.HeatMapData
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface OrderApi {



    @GET("orders/courier/orders/heatmap")
    fun apiHeatMap(): Single<ApiResponse<List<HeatMapData>>>

    @POST("orders/courier/kit/order")
    fun apiOrderDeliveryKit(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("orders/courier/kit/payment-url")
    fun apiDeliveryKitPayment(@Body map: RequestBody): Single<ApiResponse<String>>


    @POST("orders/courier/orders/{orderId}/delivery-images")
    fun apiDeliverOrder(
        @Path("orderId") orderId: String, @Body map: RequestBody
    ): Single<SimpleApiResponse>



}