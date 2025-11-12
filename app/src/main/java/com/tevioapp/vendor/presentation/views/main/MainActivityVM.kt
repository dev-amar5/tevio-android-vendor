package com.tevioapp.vendor.presentation.views.main

import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    private val authRepo: AuthRepo
) : BaseViewModel() {
    fun apiGetPersonalInfo() = SingleRequestEvent<Profile>().apply {
        authRepo.apiGetPersonalInfo().apiSubscription(this).addToCompositeDisposable()
    }
}

