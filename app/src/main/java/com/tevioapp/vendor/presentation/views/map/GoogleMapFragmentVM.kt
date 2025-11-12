package com.tevioapp.vendor.presentation.views.map

import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.data.common.RouteLegInfo
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.order.OrderRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import javax.inject.Inject


@HiltViewModel
class GoogleMapFragmentVM @Inject constructor(
    private val orderRepo: OrderRepo
) : BaseViewModel() {
    private var disposableRouteInfo: Disposable? = null
    val obrRouteLegInfo by lazy { SingleRequestEvent<RouteLegInfo>() }

    fun getRouteInfo(origin: LatLng, destination: LatLng, isMockDirection: Boolean) {
        disposableRouteInfo?.dispose()
        disposableRouteInfo = orderRepo.getDirection(origin, destination,isMockDirection).apiSubscription(obrRouteLegInfo)
    }

    override fun onCleared() {
        disposableRouteInfo?.dispose()
        super.onCleared()
    }
}

