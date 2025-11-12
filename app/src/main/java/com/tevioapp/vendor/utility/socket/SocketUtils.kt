package com.tevioapp.vendor.utility.socket

import com.tevioapp.vendor.data.local.CourierStatus
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.getLatLngObject
import io.reactivex.Single
import java.util.concurrent.TimeUnit

object SocketUtils {
    const val ACK_AVAILABLE_ORDERS = "courier:available-orders:ack"
    const val ACK_ACCEPT_ORDER = "courier:accept-order:ack"
    const val ACK_JOIN_RIDER = "courier:join-rider:ack"
    const val ACK_REJECT_ORDER = "courier:reject-order:ack"
    const val ACK_ACTIVE_ORDER = "courier:active-order:ack"
    const val ACK_UPDATE_COURIER_STATUS = "courier:update-status:ack"
    const val ACK_CHAT_SEND_MESSAGE = "chat-message:ack"
    const val ACK_CHAT_MESSAGE_TYPING = "typing:ack"
    const val ACK_CALL_STATUS = "call:status:ack"

    const val EVENT_NEW_ORDER = "courier:new-order"
    const val EVENT_ORDER_TAKEN = "courier:order-taken"
    const val EVENT_ACTIVE_ORDER = "courier:active-order"
    const val EVENT_CHAT_MESSAGE = "chat-message"
    const val EVENT_CHAT_MESSAGE_TYPING = "typing"
    const val EVENT_CALL_STATUS = "call:status"

}

fun BaseViewModel.autoJoinRider(
    socketClient: SocketClient,
    sharedPref: SharedPref,
    observer: SingleRequestEvent<CourierStatus> = SingleRequestEvent()
) {
    socketClient.getConnectionState().filter {
        it is ConnectionState.Reconnected && sharedPref.isRiderOnline()
    }.debounce(2, TimeUnit.SECONDS).flatMapSingle {
        val location = BaseApp.instance.lastLocation?.getLatLngObject()
        if (location == null) {
            Single.just(ApiResponse<CourierStatus>().apply {
                isSuccessful = false
                message = "Location not found"
            })
        } else {
            location.put("status", Enums.RIDER_STATUS_ONLINE)
            socketClient.emitWithAck<ApiResponse<CourierStatus>>(
                event = SocketUtils.ACK_JOIN_RIDER,
                data = location,
                type = getTypeToken<ApiResponse<CourierStatus>>()
            )
        }
    }.apiSubscription(observer).addToCompositeDisposable()
}

