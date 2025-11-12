package com.tevioapp.vendor.data


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DropOption(
    @SerializedName("drop_type_name") var dropTypeName: String?,
    @SerializedName("icon") var icon: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("type") var type: String?
) : Parcelable