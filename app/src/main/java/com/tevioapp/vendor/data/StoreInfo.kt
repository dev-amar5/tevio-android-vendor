package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoreInfo(
    @SerializedName("banner_image") var bannerImage: String?,
    @SerializedName("business_category") var businessCategory: String?,
    @SerializedName("country_iso") var countryIso: String?,
    @SerializedName("country_prefix") var countryPrefix: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("latitude") var latitude: Double?,
    @SerializedName("location") var location: String?,
    @SerializedName("longitude") var longitude: Double?,
    @SerializedName("name") var name: String?,
    @SerializedName("restaurant_phone") var restaurantPhone: String?
) : Parcelable