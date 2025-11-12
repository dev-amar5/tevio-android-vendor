package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class PayoutResponse(
    @SerializedName("method") var method: String?,
    @SerializedName("withdrawal_type") var withdrawalType: String?,
    @SerializedName("bank_detail") var bankDetail: BankDetail?,
    @SerializedName("mobile_money") var mobileMoney: MobileMoney?
)
