package com.tevioapp.vendor.repositary.notification

import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.data.ChatMessage
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.network.helper.ApiResponse
import io.reactivex.Single

interface NotificationRepo {
    fun apiGetChatThread(
        restroId: String? = null,
        orderId: String? = null,
        receiverId: String? = null,
        supportTicketId: String? = null
    ): Single<ApiResponse<String>>

    fun apiChatMessageList(threadId: String): Single<ApiResponse<List<ChatMessage>>>

    fun apiThreadDetail(threadId: String): Single<ApiResponse<ThreadDetail>>
    fun apiInitiateCall(threadId: String): Single<ApiResponse<AudioCallData>>
}