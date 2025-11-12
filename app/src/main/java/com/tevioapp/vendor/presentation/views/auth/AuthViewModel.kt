package com.tevioapp.vendor.presentation.views.auth

import android.content.Context
import com.tevioapp.vendor.data.AuthResponse
import com.tevioapp.vendor.data.DeliveryKitDetail
import com.tevioapp.vendor.data.DeliveryTimingOption
import com.tevioapp.vendor.data.DeliveryTypeOption
import com.tevioapp.vendor.data.DressKit
import com.tevioapp.vendor.data.JoiningBenefitsOption
import com.tevioapp.vendor.data.PhoneDetails
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.repositary.auth.AuthRepo
import com.tevioapp.vendor.repositary.order.OrderRepo
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.SingleRequestEvent
import com.tevioapp.vendor.utility.extensions.apiSubscription
import com.tevioapp.vendor.utility.extensions.isDarkMode
import com.tevioapp.vendor.utility.extensions.simpleSubscription
import com.tevioapp.vendor.utility.otp.AppHashHelper
import com.tevioapp.vendor.utility.rx.FcmUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Observable
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    private val orderRepo: OrderRepo,
    @ApplicationContext private val context: Context,
) : BaseViewModel() {

    val obrSendOtpApi = SingleRequestEvent<Unit>()
    val obrVerifyOtpApi = SingleRequestEvent<AuthResponse>()

    fun sendOtpApi(phoneDetails: PhoneDetails) {
        val appHashHelper = AppHashHelper(context)
        authRepo.sendOtpApi(JSONObject().apply {
            put("country_iso", phoneDetails.isoCode)
            put("country_prefix", phoneDetails.countryCode)
            put("phone", phoneDetails.phoneNumber)
            put("role", Enums.ROLE_COURIER)
            put("hashes", appHashHelper.getAppSignatures())
        }).simpleSubscription(obrSendOtpApi).addToCompositeDisposable()
    }

    fun verifyOtpApi(phoneDetails: PhoneDetails, otp: String) {
        FcmUtils.getCurrentToken().flatMap { fcmToken ->
            authRepo.verifyOtpApi(JSONObject().apply {
                put("country_iso", phoneDetails.isoCode)
                put("country_prefix", phoneDetails.countryCode)
                put("phone", phoneDetails.phoneNumber)
                put("role", Enums.ROLE_COURIER)
                put("otp", otp)
                put("fcm_token", fcmToken)
            })
        }.apiSubscription(obrVerifyOtpApi).addToCompositeDisposable()
    }

    fun apiSetPersonalInfo(jsonObject: JSONObject) = SingleRequestEvent<String>().apply {
        authRepo.apiSetPersonalInfo(jsonObject).apiSubscription(this).addToCompositeDisposable()
    }

    fun apiGetPersonalInfo() = SingleRequestEvent<Profile>().apply {
        authRepo.apiGetPersonalInfo().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiDeliveryKitPayment() = SingleRequestEvent<String>().apply {
        orderRepo.apiDeliveryKitPayment(JSONObject().apply {
            put("dark_mode", context.isDarkMode())
            put("payment_method_id", null)
        }).apiSubscription(this).addToCompositeDisposable()
    }

    fun apiDeliveryTypeOptionList() = SingleRequestEvent<List<DeliveryTypeOption>>().apply {
        authRepo.apiDeliveryTypeOptionList().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiDeliveryTimingOptionList() = SingleRequestEvent<List<DeliveryTimingOption>>().apply {
        authRepo.apiDeliveryTimingOptionList().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiJoiningBenefitsOptionList() = SingleRequestEvent<List<JoiningBenefitsOption>>().apply {
        authRepo.apiJoiningBenefitsOptionList().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiDressKitList() = SingleRequestEvent<List<DressKit>>().apply {
        authRepo.apiDressKitList().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiDeliveryKitOrderDetail() = SingleRequestEvent<DeliveryKitDetail>().apply {
        orderRepo.apiDeliveryKitOrderDetail().apiSubscription(this).addToCompositeDisposable()
    }

    fun apiOrderDeliveryKit(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
        orderRepo.apiOrderDeliveryKit(jsonObject).simpleSubscription(this)
            .addToCompositeDisposable()
    }

    fun apiSetPayoutInfo(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
        authRepo.apiSetPayoutInfo(jsonObject).simpleSubscription(this).addToCompositeDisposable()
    }

    fun apiSetDocumentsInfo(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
        authRepo.apiSetDocumentsInfo(jsonObject).simpleSubscription(this).addToCompositeDisposable()
    }

    fun apiSetWorkSettings(jsonObject: JSONObject) = SingleRequestEvent<Unit>().apply {
        authRepo.apiSetWorkSettings(jsonObject).simpleSubscription(this).addToCompositeDisposable()
    }

    fun apiRegistrationStatus() = SingleRequestEvent<RegistrationStepsActivity.Steps>().apply {
        authRepo.apiRegistrationStatus().apiSubscription(
            this, Observable.interval(0, 3, TimeUnit.SECONDS).filter { period ->
                if (period > 0L) {
                    this.hasActiveObservers()
                } else true
            }).addToCompositeDisposable()
    }

}