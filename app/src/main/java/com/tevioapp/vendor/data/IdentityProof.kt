package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdentityProof(
    @SerializedName("back_image") var backImage: String?,
    @SerializedName("document_type") var documentType: String?,
    @SerializedName("front_image") var frontImage: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("user_id") var userId: String?
) : Parcelable