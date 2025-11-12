package com.tevioapp.vendor.presentation.views.main.dashboard

import com.tevioapp.vendor.data.HeatMapData
import com.tevioapp.vendor.data.Order
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.data.local.CourierStatus
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.repositary.order.OrderRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.event.helper.Resource
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.getLatLngObject
import com.tevioapp.vendor.utility.socket.SocketClient
import com.tevioapp.vendor.utility.socket.SocketUtils
import com.tevioapp.vendor.utility.socket.autoJoinRider
import com.tevioapp.vendor.utility.socket.getTypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashBoardFragmentVM @Inject constructor(
    private val orderRepo: OrderRepo,
    private val socketClient: SocketClient,
    private val sharePref: SharedPref,
    private val authRepo: AuthRepo
) : BaseViewModel() {

    val obrHeatMapData = SingleRequestEvent<List<HeatMapData>>()
    val obrCourierStatus by lazy { SingleRequestEvent<CourierStatus>() }

    init {
        addEventListeners()
        autoJoinRider(socketClient, sharePref)
        socketClient.connect().subscribe()
    }

    private fun addEventListeners() {
        socketClient.listen<ApiResponse<Order>>(
            event = SocketUtils.EVENT_NEW_ORDER, type = getTypeToken<ApiResponse<Order>>()
        ).subscribe { apiResponse ->
            apiResponse.data?.id?.let {
                updateCourierStatus(it, true)
            }
        }.addToCompositeDisposable()

        socketClient.listen<ApiResponse<String>>(
            SocketUtils.EVENT_ORDER_TAKEN, getTypeToken<ApiResponse<String>>()
        ).subscribe { apiResponse ->
            apiResponse.data?.let {
                updateCourierStatus(it, false)
            }
        }.addToCompositeDisposable()
    }

    private fun updateCourierStatus(orderId: String, isRemoved: Boolean) {
        val courierStatus = obrCourierStatus.value?.data ?: return
        val availableOrders = courierStatus.availableOrders.orEmpty().toMutableSet()
        if (isRemoved) {
            availableOrders.remove(orderId)
        } else {
            availableOrders.add(orderId)
        }
        courierStatus.availableOrders = availableOrders.toList()
        obrCourierStatus.postValue(
            Resource.success(
                courierStatus, "Available orders updated"
            )
        )
    }


    fun changeRiderStatus(status: String) {
        val request =
            BaseApp.instance.lastLocation?.getLatLngObject()?.put("status", status) ?: return
        socketClient.emitWithAck<ApiResponse<CourierStatus>>(
            event = SocketUtils.ACK_JOIN_RIDER,
            data = request,
            type = getTypeToken<ApiResponse<CourierStatus>>()
        ).doOnSuccess { response ->
            response?.data?.status?.let { savedStatus ->
                sharePref.saveRiderStatus(savedStatus)
            }
        }.apiSubscription(obrCourierStatus).addToCompositeDisposable()
    }

    fun apiHeatMap() {
        orderRepo.apiHeatMap().apiSubscription(obrHeatMapData).addToCompositeDisposable()
    }

    fun apiGetPersonalInfo() = SingleRequestEvent<Profile>().apply {
        authRepo.apiGetPersonalInfo().apiSubscription(this).addToCompositeDisposable()
    }

    override fun onCleared() {
        socketClient.offAll()
        socketClient.disconnect().subscribe()
        super.onCleared()
    }
}
