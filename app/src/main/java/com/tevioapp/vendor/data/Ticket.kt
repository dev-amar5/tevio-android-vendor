package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.data.common.TitleDescription

data class Ticket(
    @SerializedName("display_id") var displayId: String?,
    @SerializedName("id") var id: String?,
    @SerializedName("issue_type") var issueType: TitleDescription?,
    @SerializedName("order_details") var orderDetails: Order?,
    @SerializedName("status") var status: String?,
    @SerializedName("time") var time: String?,
    @SerializedName("details") var details: String?,
    @SerializedName("image") var image: String?,
    @SerializedName("updates") var updates: List<StatusUpdate>?,
) {
    fun ticketNumberDisplay(): String {
        return "Ticket No: $displayId"
    }

    fun hasImage(): Boolean {
        return image.isNullOrEmpty().not()
    }
}
