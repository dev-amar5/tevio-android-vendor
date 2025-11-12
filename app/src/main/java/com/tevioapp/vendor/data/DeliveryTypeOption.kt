package com.tevioapp.vendor.data

import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.utility.currency.CurrencyUtils

data class DeliveryTypeOption(
    @SerializedName("description") var description: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("joining_bonus") var joiningBonus: Double?,
    @SerializedName("title") var title: String?,
    @SerializedName("type") var type: String?,
    @SerializedName("url") var url: String?,
    @SerializedName("url_title") var urlTitle: String?,
    @SerializedName("validity") var validity: Int?
) {
    fun getBonusString(): String {
        return buildString {
            append("Joining Bonus of up to ")
            append(CurrencyUtils.formatAsCurrency(joiningBonus))
        }
    }
}