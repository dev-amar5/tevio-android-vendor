package com.tevioapp.vendor.repositary.notification

import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.data.ChatMessage
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.network.api.NotificationApi
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.utility.extensions.toApplicationJsonBody
import io.reactivex.Single
import org.json.JSONObject
import javax.inject.Inject

class NotificationRepoImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepo {
    override fun apiGetChatThread(
        restroId: String?,
        orderId: String?,
        receiverId: String?,
        supportTicketId: String?
    ): Single<ApiResponse<String>> {
        val request = JSONObject().apply {
            put("restaurant_id", restroId)
            put("order_id", orderId)
            put("receiver_id", receiverId)
            put("support_ticket_id", supportTicketId)
        }
        return notificationApi.apiGetChatThread(request.toApplicationJsonBody())
    }

    override fun apiChatMessageList(threadId: String): Single<ApiResponse<List<ChatMessage>>> {
        return notificationApi.apiGetChatList(threadId)
    }

    override fun apiThreadDetail(threadId: String): Single<ApiResponse<ThreadDetail>> {
        return notificationApi.apiThreadDetail(threadId)
    }

    override fun apiInitiateCall(threadId: String): Single<ApiResponse<AudioCallData>> {
        return notificationApi.apiInitiateCall(threadId)
    }
}
