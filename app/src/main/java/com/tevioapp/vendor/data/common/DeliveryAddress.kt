package com.tevioapp.vendor.data.common

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryAddress(
    val buildingName: String, var latLng: LatLng, val address: String
) : Parcelable {
    fun displayAddress(): String {
        return buildString {
            if (buildingName.isNotEmpty()) {
                append(buildingName, ",")
            }
            append(address)
        }
    }
}