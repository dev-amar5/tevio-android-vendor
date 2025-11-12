package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("registration_status") val registrationStatus: String?,
    @SerializedName("expiry_time") val expiryTime: Long?
):Parcelable