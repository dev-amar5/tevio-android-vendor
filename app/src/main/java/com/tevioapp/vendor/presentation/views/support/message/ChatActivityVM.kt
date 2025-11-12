package com.tevioapp.vendor.presentation.views.support.message

import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.data.ChatMessage
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.notification.NotificationRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.simpleSubscription
import com.tevioapp.vendor.utility.socket.SocketClient
import com.tevioapp.vendor.utility.socket.SocketUtils
import com.tevioapp.vendor.utility.socket.autoJoinRider
import com.tevioapp.vendor.utility.socket.getTypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatActivityVM @Inject constructor(
    private val socketClient: SocketClient,
    private val notificationRepo: NotificationRepo,
    private val sharePref: SharedPref,
) : BaseViewModel() {
    val obrNewMessage = SingleRequestEvent<ChatMessage>()
    val obrMessageTyping = SingleRequestEvent<Unit>()
    val obrAllMessage = SingleRequestEvent<List<ChatMessage>>()

    fun initSocket() {
        socketClient.listen<ApiResponse<ChatMessage>>(
            event = SocketUtils.EVENT_CHAT_MESSAGE, type = getTypeToken<ApiResponse<ChatMessage>>()
        ).apiSubscription(obrNewMessage).addToCompositeDisposable()

        socketClient.listen<SimpleApiResponse>(
            event = SocketUtils.EVENT_CHAT_MESSAGE_TYPING, type = getTypeToken<SimpleApiResponse>()
        ).simpleSubscription(obrMessageTyping).addToCompositeDisposable()

        autoJoinRider(socketClient, sharePref)
        socketClient.connect().subscribe().addToCompositeDisposable()
    }

    fun sendMessage(request: JSONObject) {
        socketClient.emitWithAck<ApiResponse<ChatMessage>>(
            event = SocketUtils.ACK_CHAT_SEND_MESSAGE,
            data = request,
            type = getTypeToken<ApiResponse<ChatMessage>>()
        ).apiSubscription(obrNewMessage).addToCompositeDisposable()
    }

    fun sendTyping(threadId: String) {
        socketClient.emitWithAck<SimpleApiResponse>(
            event = SocketUtils.ACK_CHAT_MESSAGE_TYPING,
            data = JSONObject().apply { put("thread_id", threadId) },
            type = getTypeToken<SimpleApiResponse>()
        ).subscribe().addToCompositeDisposable()
    }

    fun apiChatMessageList(threadId: String) {
        notificationRepo.apiChatMessageList(threadId).apiSubscription(obrAllMessage)
            .addToCompositeDisposable()
    }

    fun apiChatThread(
        restroId: String? = null,
        orderId: String? = null,
        receiverId: String? = null,
        supportTicketId: String? = null
    ) = SingleRequestEvent<String>().apply {
        notificationRepo.apiGetChatThread(
            restroId = restroId,
            orderId = orderId,
            receiverId = receiverId,
            supportTicketId = supportTicketId
        ).apiSubscription(this).addToCompositeDisposable()
    }


    fun apiThreadDetail(threadId: String) = SingleRequestEvent<ThreadDetail>().apply {
        notificationRepo.apiThreadDetail(threadId).apiSubscription(this).addToCompositeDisposable()
    }

    fun apiInitiateCall(threadId: String) = SingleRequestEvent<AudioCallData>().apply {
        notificationRepo.apiInitiateCall(threadId).apiSubscription(this).addToCompositeDisposable()
    }

    override fun onCleared() {
        socketClient.off(
            SocketUtils.EVENT_CHAT_MESSAGE,
            SocketUtils.ACK_CHAT_SEND_MESSAGE,
            SocketUtils.ACK_CHAT_MESSAGE_TYPING,
            SocketUtils.EVENT_CHAT_MESSAGE_TYPING
        )
        super.onCleared()
    }

}
