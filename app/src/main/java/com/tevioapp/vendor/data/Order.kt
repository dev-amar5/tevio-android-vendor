package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.data.common.Profile
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    @SerializedName("id") var id: String?,
    @SerializedName("display_id") var displayId: String?,
    @SerializedName("order_status") var orderStatus: String?,
    @SerializedName("courier_status") var courierStatus: String?,
    @SerializedName("type") var cartType: String?,

    @SerializedName("customer_info") var customerInfo: Profile?,
    @SerializedName("total_amount") var orderAmount: Double?,
    @SerializedName("ordered_items") var menuItems: List<MenuItem>?,
    @SerializedName("payment_method") var paymentMethod: String?,

    @SerializedName("store_info") var storeInfo: StoreInfo?,

    @SerializedName("pickup_info") var pickupInfo: AddressInfo,
    @SerializedName("drop_info") var dropInfo: AddressInfo,

    @SerializedName("updates") var updates: List<StatusUpdate>?,
    @SerializedName("complete_by") var completeBy: String?,
    @SerializedName("accepted_at") var acceptedAt: String?,
    @SerializedName("earning_info") var earningInfo: EarningInfo?,
    @SerializedName("delivery_pin") var pinRequired: Boolean=false

    ) : Parcelable






