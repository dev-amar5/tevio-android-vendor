package com.tevioapp.vendor.data.common

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.data.Contact
import com.tevioapp.vendor.data.PhoneDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    @SerializedName("id") var id: String,
    @SerializedName("first_name") var firstName: String?,
    @SerializedName("last_name") var lastName: String?,
    @SerializedName("email") var email: String?,
    @SerializedName("country_iso") var countryIso: String?,
    @SerializedName("country_prefix") var countryPrefix: String?,
    @SerializedName("phone") var phone: String?,
    @SerializedName("d_o_b") var dateOfBirth: String?,
    @SerializedName("is_email_verified") var isEmailVerified: Boolean = false,
    @SerializedName("gender") var gender: String?,
    @SerializedName("profile_pic") var profilePic: String?,
    @SerializedName("role") var role: String?,
    @SerializedName("registration_status") val registrationStatus: String?,
    @SerializedName("emergency_contacts") var emergencyContacts: List<Contact>?,
    @SerializedName("address_info") var addressInfo: Address?,
    @SerializedName("createdAt") var createdAt: String?,
) : Parcelable {
    @Parcelize
    data class Address(
        @SerializedName("address") var address: String? = null,
        @SerializedName("latitude") var latitude: Double? = null,
        @SerializedName("longitude") var longitude: Double? = null
    ) : Parcelable {
        fun getLatLng() = LatLng(latitude ?: 0.0, longitude ?: 0.0)
    }

    fun getFullName() = buildString {
        append(firstName.orEmpty())
        if (lastName.orEmpty().isNotEmpty()) {
            append(" ")
            append(lastName)
        }
    }

    fun getPhoneDetail(): PhoneDetails? {
        val phone = phone.orEmpty()
        val code = countryPrefix.orEmpty()
        val iso = countryIso.orEmpty()
        if (phone.isEmpty() || code.isEmpty() || iso.isEmpty()) return null
        return PhoneDetails(phone, code, iso)
    }
}

