package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ThreadDetail(
    @SerializedName("contact_info") var contactInfo: Contact?,
    @SerializedName("id") var id: String?,
    @SerializedName("image") var image: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("role") var role: String?,
    @SerializedName("thread_id") var threadId: String?,
    @SerializedName("token") var token: String?
) : Parcelable

