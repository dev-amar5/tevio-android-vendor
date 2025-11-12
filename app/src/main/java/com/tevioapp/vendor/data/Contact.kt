package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    @SerializedName("phone") val phoneNumber: String? = null,
    @SerializedName("country_prefix") val countryCode: String? = null,
    @SerializedName("country_iso") val isoCode: String? = null
) : Parcelable {

    fun getPhoneDetail(): PhoneDetails? {
        val phone = phoneNumber.orEmpty()
        val code = countryCode.orEmpty()
        val iso = isoCode.orEmpty()
        if (phone.isEmpty() || code.isEmpty() || iso.isEmpty()) return null
        return PhoneDetails(phone, code, iso)
    }
}
