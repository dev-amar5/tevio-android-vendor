package com.tevioapp.vendor.presentation.views.profile

import com.tevioapp.vendor.data.DocumentResponse
import com.tevioapp.vendor.data.PayoutResponse
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.simpleSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : BaseViewModel() {

        fun apiPayoutDetail() = SingleRequestEvent<PayoutResponse>().apply {
            authRepo.apiGetPayoutInfo().apiSubscription(this).addToCompositeDisposable()
        }

        fun apiPersonalInfo() = SingleRequestEvent<Profile>().apply {
            authRepo.apiGetPersonalInfo().apiSubscription(this).addToCompositeDisposable()
        }

        fun apiGetDocumentsInfo() = SingleRequestEvent<DocumentResponse>().apply {
            authRepo.apiGetDocumentsInfo().apiSubscription(this).addToCompositeDisposable()
        }

        fun apiGetPersonalInfo() = SingleRequestEvent<Profile>().apply {
            authRepo.apiGetPersonalInfo().apiSubscription(this).addToCompositeDisposable()
        }

        fun apiSetPersonalInfo(jsonObject: JSONObject) = SingleRequestEvent<String>().apply {
            authRepo.apiSetPersonalInfo(jsonObject).apiSubscription(this).addToCompositeDisposable()
        }

        fun apiSetPayoutInfo(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
            authRepo.apiSetPayoutInfo(jsonObject).simpleSubscription(this).addToCompositeDisposable()
        }

        fun apiSetDocumentsInfo(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
            authRepo.apiSetDocumentsInfo(jsonObject).simpleSubscription(this).addToCompositeDisposable()
        }

        fun apiValidatePhone(request: JSONObject) = SingleRequestEvent<String>().apply {
            authRepo.apiValidatePhone(request).apiSubscription(this).addToCompositeDisposable()
        }

        fun apiChangePhone(request: JSONObject) = SingleRequestEvent<Unit>().apply {
            authRepo.apiChangePhone(request).simpleSubscription(this).addToCompositeDisposable()
        }



}