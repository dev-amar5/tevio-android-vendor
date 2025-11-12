package com.tevioapp.vendor.data.local


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.data.Order
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourierStatus(
    @SerializedName("status") var status: String?,
    @SerializedName("available_orders") var availableOrders: List<String>?,
    @SerializedName("active_orders") var activeOrders: List<Order>?
) : Parcelable

