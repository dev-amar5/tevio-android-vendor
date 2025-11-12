package com.tevioapp.vendor.network.api

import com.tevioapp.vendor.data.common.DirectionResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface GoogleApi {
    @GET("directions/json")
    fun getDirection(@QueryMap map: Map<String, String>): Single<DirectionResponse>
}