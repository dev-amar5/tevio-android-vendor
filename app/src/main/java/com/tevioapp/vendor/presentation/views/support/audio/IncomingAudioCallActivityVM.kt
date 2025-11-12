package com.tevioapp.vendor.presentation.views.support.audio

import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.notification.NotificationRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.simpleSubscription
import com.tevioapp.vendor.utility.socket.SocketClient
import com.tevioapp.vendor.utility.socket.SocketUtils
import com.tevioapp.vendor.utility.socket.getTypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class IncomingAudioCallActivityVM @Inject constructor(
    private val notificationRepo: NotificationRepo, private val socketClient: SocketClient
) : BaseViewModel() {
    val obrCallStatus by lazy { SingleRequestEvent<String>() }
    init {
        socketClient.listen<ApiResponse<String>>(
            event = SocketUtils.EVENT_CALL_STATUS, type = getTypeToken<ApiResponse<String>>()
        ).apiSubscription(obrCallStatus).addToCompositeDisposable()
        socketClient.connect().subscribe()
    }

    fun sendCallStatus(threadId: String, status: String) = SingleRequestEvent<Unit>().apply {
        socketClient.emitWithAck<SimpleApiResponse>(
            event = SocketUtils.ACK_CALL_STATUS, data = JSONObject().apply {
                put("thread_id", threadId)
                put("status", status)
            }, type = getTypeToken<SimpleApiResponse>()
        ).simpleSubscription(this).addToCompositeDisposable()
    }

    fun apiThreadDetail(threadId: String) = SingleRequestEvent<ThreadDetail>().apply {
        notificationRepo.apiThreadDetail(threadId).apiSubscription(this).addToCompositeDisposable()
    }

}
