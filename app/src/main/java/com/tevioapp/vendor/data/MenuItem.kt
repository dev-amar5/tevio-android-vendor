package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    @SerializedName("amount") var amount: Double?,
    @SerializedName("discounted_amount") var discountedAmount: Double?,
    @SerializedName("id") var id: String?,
    @SerializedName("images") var images: List<String>?,
    @SerializedName("name") var name: String?,
    @SerializedName("purchased_item_id") var purchasedItemId: String?,
    @SerializedName("quantity") var quantity: Int?
) : Parcelable {
    fun getImage(): String {
        return images?.firstOrNull().orEmpty()
    }

    fun getQtyDisplay(): String {
        return buildString { append(quantity, " QTY") }
    }
}