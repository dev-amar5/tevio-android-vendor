package com.tevioapp.vendor.data

import com.google.gson.annotations.SerializedName

data class HeatMapData(
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double
)