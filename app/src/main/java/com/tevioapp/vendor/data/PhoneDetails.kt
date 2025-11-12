package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhoneDetails(
    @SerializedName("phone") val phoneNumber: String, // National number (only digits)
    @SerializedName("country_prefix") val countryCode: String, // Numeric country code without "+"
    @SerializedName("country_iso") val isoCode: String     // ISO 3166-1 alpha-2 country code, e.g., "US"
) : Parcelable {
    override fun toString(): String {
        return buildString {
            append("+")
            append(countryCode)
            append(" ")
            append(phoneNumber)
        }
    }

}


