package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("id") var id: String?,
    @SerializedName("sender_id") var senderId: String?,
    @SerializedName("thread_id") var threadId: String?,
    @SerializedName("message") var message: String?,
    @SerializedName("type") var type: String?,
    @SerializedName("url") var url: String?,
    @SerializedName("createdAt") var createdAt: String?
)
