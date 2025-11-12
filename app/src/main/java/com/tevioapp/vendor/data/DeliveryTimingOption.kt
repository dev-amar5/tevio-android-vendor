package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class DeliveryTimingOption(
    @SerializedName("description")
    var description: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("image_url")
    var imageUrl: Any?,
    @SerializedName("title")
    var title: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("url")
    var url: String?,
    @SerializedName("url_title")
    var urlTitle: String?,
    @SerializedName("validity")
    var validity: Int?,
    @SerializedName("weekly_earning")
    var weeklyEarning: Double?
)