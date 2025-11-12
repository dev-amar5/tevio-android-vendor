package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressInfo(
    @SerializedName("address") var address: String?,
    @SerializedName("latitude") var latitude: Double?,
    @SerializedName("longitude") var longitude: Double?,
    @SerializedName("name") var name: String?,
    @SerializedName("delivery_instructions") var deliveryInstructions: String?,
    @SerializedName("dropoff_option") var dropOption: DropOption?,
) : Parcelable {
    fun getLatLng(): LatLng {
        return LatLng(latitude ?: 0.0, longitude ?: 0.0)
    }

    fun hasDeliveryInstruction(): Boolean {
        return deliveryInstructions.isNullOrEmpty().not()
    }
}
