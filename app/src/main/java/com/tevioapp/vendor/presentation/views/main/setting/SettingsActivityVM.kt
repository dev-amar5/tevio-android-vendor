package com.tevioapp.vendor.presentation.views.main.setting

import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.simpleSubscription
import com.tevioapp.vendor.utility.socket.SocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SettingsActivityVM @Inject constructor(
    private val socketClient: SocketClient, private val authRepo: AuthRepo
) : BaseViewModel() {

    fun disconnectSocket() {
        socketClient.disconnect().subscribe()
    }

    fun apiLogout() = SingleRequestEvent<Unit>().apply {
        authRepo.apiLogout().simpleSubscription(this).addToCompositeDisposable()
    }

    fun apiDeleteAccount() = SingleRequestEvent<Unit>().apply {
        authRepo.apiDeleteAccount().simpleSubscription(this).addToCompositeDisposable()
    }
}

