package com.tevioapp.vendor.presentation.views.location

import android.content.Context
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.util.DataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class LocationActivityVM @Inject constructor(
    @ApplicationContext val context: Context,
    private val authRepo: AuthRepo,
    private val sharedPref: SharedPref,
    val  dataProvider: DataProvider
) : BaseViewModel() {
  /*  val obrDeleteAddressApi = SingleRequestEvent<String>()
    val obrAddressListApi = SingleRequestEvent<List<UserAddress>>()
    var addressType: String = UserAddress.TYPE_HOME

    fun addUserAddress(address: UserAddress):SingleRequestEvent<UserAddress> {
        return SingleRequestEvent<UserAddress>().apply {
            authRepo.apiAddAddress(address).apiSubscription(this)
                .addToCompositeDisposable()
        }
    }

    fun editUserAddress(address: UserAddress):SingleRequestEvent<UserAddress> {
        return SingleRequestEvent<UserAddress>().apply {
            authRepo.apiEditAddress(address).apiSubscription(this)
                .addToCompositeDisposable()
        }
    }
    fun deleteUserAddress(address: UserAddress) {
        authRepo.apiDeleteAddress(address).doOnSuccess {
            getAddressList()
        }.doOnSuccess {
            if (it.isSuccessful && it.data != null) {
                val userLocation = sharedPref.getUserLocation()
                if (userLocation != null && userLocation.addressId == address.id) {
                    sharedPref.setUserLocation(
                        UserLocation.fromLatLng(
                            context, BaseApp.instance.lastLocation?.getLatLng()
                        )
                    )
                }
            }
        }.apiSubscription(obrDeleteAddressApi).addToCompositeDisposable()

    }

    fun getAddressList() {
        authRepo.apiAddressList().apiSubscription(obrAddressListApi).addToCompositeDisposable()
    }*/

}

