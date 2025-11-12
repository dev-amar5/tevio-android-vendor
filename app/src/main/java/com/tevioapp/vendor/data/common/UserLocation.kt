package com.tevioapp.vendor.data.common

import android.content.Context
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.utility.location.GoogleMapUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLocation(
    val type: Type, val title: String, val address: String, val latLng: LatLng
) : Parcelable {
    fun getShortAddress(): String {
        val address = GoogleMapUtil.getAddressObjectLatLng(BaseApp.instance, latLng) ?: return ""
        return listOfNotNull(
            address.subLocality?.takeIf { it.isNotBlank() },
            address.locality?.takeIf { it.isNotBlank() },
            address.adminArea?.takeIf { it.isNotBlank() }).distinct().take(2).joinToString(", ")
    }

    fun getFullAddress(): String {
        return buildString {
            if (title != "Unknown") {
                append(title, " ")
            }
            append(address)
        }
    }

    enum class Type {
        GPS
    }

    companion object {
        fun fromLatLng(context: Context, latLng: LatLng?): UserLocation? {
            if (latLng == null) return null
            val address = GoogleMapUtil.getAddressObjectLatLng(context, latLng) ?: return null
            // Build filtered list excluding countryName
            val addressParts = listOf(
                address.featureName,
                address.thoroughfare,
                address.subLocality,
                address.locality,
                address.adminArea,
                address.postalCode
            ).filterNot { it.isNullOrBlank() }
            // Clean full address from getAddressLine(0), remove country name if present
            val fullAddress = address.getAddressLine(0)?.split(",")?.map { it.trim() }
                ?.filterNot { it.equals(address.countryName, ignoreCase = true) }
                ?.let { if (it.firstOrNull() == address.featureName) it.drop(1) else it }
                ?.joinToString(", ")
                ?: addressParts.joinToString(", ") // fallback if getAddressLine(0) is null
            val title = address.featureName ?: address.subLocality ?: "Unknown"
            return UserLocation(
                type = Type.GPS, title = title, address = fullAddress, latLng = latLng
            )
        }
    }
}
