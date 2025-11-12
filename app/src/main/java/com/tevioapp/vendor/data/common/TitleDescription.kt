package com.tevioapp.vendor.data.common


import com.google.gson.annotations.SerializedName

data class TitleDescription(
    @SerializedName("description") var description: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("title") var title: String?
)