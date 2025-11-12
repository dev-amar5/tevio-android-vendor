package com.tevioapp.vendor.network.helper

import com.google.gson.annotations.SerializedName

open class ApiResponse<T> : SimpleApiResponse() {
    @SerializedName("data")
    var data: T? = null
    override fun toString(): String {
        return "ApiResponse{data=$data}"
    }

}