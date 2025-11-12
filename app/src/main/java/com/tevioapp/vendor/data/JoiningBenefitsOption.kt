package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class JoiningBenefitsOption(
    @SerializedName("amount") var amount: Double?,
    @SerializedName("enabled") var enabled: Boolean?,
    @SerializedName("id") var id: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("label") var label: String?,
    @SerializedName("position") var position: String?
)