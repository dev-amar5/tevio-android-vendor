package com.tevioapp.vendor.data
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioCallData(
    @SerializedName("role") var role: String?,
    @SerializedName("app_id") var appId: String,
    @SerializedName("thread_id") var threadId: String,
    @SerializedName("token") var token: String,
    @SerializedName("uid") var uId: Int
) : Parcelable {
    override fun toString(): String {
        return "AudioCallData(appId='$appId', threadId='$threadId', token='$token', uId=$uId role=$role )"
    }
}