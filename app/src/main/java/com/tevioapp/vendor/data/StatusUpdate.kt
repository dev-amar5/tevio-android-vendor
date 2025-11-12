package com.tevioapp.vendor.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusUpdate(
    @SerializedName("status") var status: String?,
    @SerializedName("estimated_time") var estimatedTime: String?,
    @SerializedName("actual_time") var actualTime: String?,
) : Parcelable {
    fun isCompleted(): Boolean {
        return actualTime.orEmpty().isNotEmpty()
    }

    fun isValid(): Boolean {
        return if (actualTime.isNullOrEmpty().not()) true
        else if (estimatedTime.isNullOrEmpty().not()) true
        else false
    }
}
