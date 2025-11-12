package com.tevioapp.vendor.network.helper

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class SimpleApiResponse(
    @SerializedName("success") var isSuccessful: Boolean = true,
    @SerializedName("status") var statusCode: Int = 200,
    @SerializedName("message" , alternate = ["error"]) var message: String? = null
) : Serializable