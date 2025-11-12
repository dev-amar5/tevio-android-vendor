package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class EarningInfo(
    @SerializedName("bonus") var bonus: Double?,
    @SerializedName("earnings") var earnings: Double?,
    @SerializedName("tip") var tip: Double?,
    @SerializedName("total") var total: Double?
): Parcelable