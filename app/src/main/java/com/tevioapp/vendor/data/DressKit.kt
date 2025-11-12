package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DressKit(
    @SerializedName("id")
    var id: String?,
    @SerializedName("image_url")
    var imageUrl: String?,
    @SerializedName("measurement")
    var measurement: Measurement?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("price")
    var price: Double?,
    @SerializedName("quantity")
    var quantity: String?,
    @SerializedName("size")
    var size: String?
) : Parcelable {
    @Parcelize
    data class Measurement(
        @SerializedName("chest")
        var chest: String?,
        @SerializedName("length")
        var length: String?
    ) : Parcelable
}