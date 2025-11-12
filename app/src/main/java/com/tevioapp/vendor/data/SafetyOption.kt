package com.tevioapp.vendor.data

import com.google.gson.annotations.SerializedName

data class SafetyOption(
    @SerializedName("color_code") var colorCode: String?,
    @SerializedName("description") var description: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("position") var position: String?,
    @SerializedName("title") var title: String?
)