package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class DeliveryKitDetail(
    @SerializedName("address")
    var address: String?,
    @SerializedName("apartment")
    var apartment: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("delivered_at")
    var deliveredAt: Any?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("items")
    var items: List<Item>?,
    @SerializedName("landmark")
    var landmark: String?,
    @SerializedName("latitude")
    var latitude: String?,
    @SerializedName("location_type")
    var locationType: Any?,
    @SerializedName("longitude")
    var longitude: String?,
    @SerializedName("payment_status")
    var paymentStatus: String?,
    @SerializedName("status")
    var status: String?,
    @SerializedName("total_amount")
    var totalAmount: Double?,
    @SerializedName("updated_at")
    var updatedAt: String?,
    @SerializedName("user_id")
    var userId: String?
) {
    data class Item(
        @SerializedName("id")
        var id: String?,
        @SerializedName("item_id")
        var itemId: String?,
        @SerializedName("item_name")
        var itemName: String?,
        @SerializedName("measurement")
        var measurement: Measurement?,
        @SerializedName("order_id")
        var orderId: String?,
        @SerializedName("price")
        var price: Double?,
        @SerializedName("quantity")
        var quantity: String?,
        @SerializedName("size")
        var size: String? ,
        @SerializedName("image_url")
        var imageUrl: String?
    ) {
        data class Measurement(
            @SerializedName("chest")
            var chest: String?,
            @SerializedName("length")
            var length: String?
        )

        fun isTShirt(): Boolean {
            return size != "other"
        }

        fun isFree(): Boolean {
            return (price ?: 0.0) == 0.0
        }
    }
}