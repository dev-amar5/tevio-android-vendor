package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class MobileMoney(
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("mobile_money_name")
    var mobileMoneyName: String?,
    @SerializedName("network")
    var network: String?,
    @SerializedName("network_name")
    var networkName: String?,
    @SerializedName("number")
    var number: String?,
    @SerializedName("payout_info_id")
    var payoutInfoId: String?,
    @SerializedName("updated_at")
    var updatedAt: String?
)