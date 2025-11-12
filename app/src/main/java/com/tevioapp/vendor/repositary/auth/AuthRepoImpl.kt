package com.tevioapp.vendor.repositary.auth

import android.content.Context
import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.AuthResponse
import com.tevioapp.vendor.data.DeliveryTimingOption
import com.tevioapp.vendor.data.DocumentResponse
import com.tevioapp.vendor.data.PayoutResponse
import com.tevioapp.vendor.data.SafetyOption
import com.tevioapp.vendor.data.common.FileUploadResource
import com.tevioapp.vendor.data.common.MediaFile
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.data.common.TitleDescription
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.network.api.AuthApi
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.network.utils.RetroUtils
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.presentation.views.country.CountryCode
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.extensions.toApplicationJsonBody
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val api: AuthApi,
    private val sharedPref: SharedPref,
    @ApplicationContext val context: Context
) : AuthRepo {

    override fun sendOtpApi(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiSendOtp(request.toApplicationJsonBody())
    }

    override fun apiLogout(): Single<SimpleApiResponse> {
        return api.apiLogout()
    }

    override fun verifyOtpApi(request: JSONObject): Single<ApiResponse<AuthResponse>> {
        return api.apiVerifyOtp(request.toApplicationJsonBody())
    }

    override fun apiUpdateLocation(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiUpdateLocation(request.toApplicationJsonBody())
    }

    override fun apiDeleteMedia(urls: JSONArray): Single<SimpleApiResponse> {
        return api.apiDeleteMedia(JSONObject().apply {
            put("urls", urls)
        }.toApplicationJsonBody())
    }

    override fun apiValidatePhone(request: JSONObject): Single<ApiResponse<String>> {
        return api.apiValidatePhone(request.toApplicationJsonBody())
    }

    override fun apiChangePhone(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiChangePhone(request.toApplicationJsonBody())
    }


    override fun apiRegistrationStatus(): Single<ApiResponse<RegistrationStepsActivity.Steps>> {
        return api.apiRegistrationStatus()
    }

    override fun apiSetPersonalInfo(request: JSONObject): Single<ApiResponse<String>> {
        return api.apiSetPersonalInfo(request.toApplicationJsonBody()).doOnSuccess { apiResponse ->
            apiResponse.data?.let {
                sharedPref.saveToken(it)
                sharedPref.saveRegistrationStatus(Enums.REGISTRATION_STATUS_IN_PROCESS)
            }

        }
    }

    override fun apiSetPayoutInfo(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiSetPayoutInfo(request.toApplicationJsonBody())

    }

    override fun apiGetPayoutInfo(): Single<ApiResponse<PayoutResponse>> {
        return api.apiGetPayoutInfo()
    }

    override fun apiSetWorkSettings(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiSetWorkSettings(request.toApplicationJsonBody())

    }


    override fun apiSetDocumentsInfo(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiSetDocumentsInfo(request.toApplicationJsonBody())

    }

    override fun apiGetDocumentsInfo(): Single<ApiResponse<DocumentResponse>> {
        return api.apiGetDocumentsInfo().doOnSuccess { response ->
            response.data?.let { documentResponse ->
                sharedPref.saveVehicleInfo(documentResponse.vehicleInfo)
            }
        }
    }

    override fun apiUpdateProfilePic(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiUpdateProfilePic(request.toApplicationJsonBody())

    }

    override fun apiGetPersonalInfo(): Single<ApiResponse<Profile>> {
        return api.apiGetPersonalInfo().doOnSuccess { response ->
            response.data?.let { profile ->
                sharedPref.saveProfile(profile)
                profile.registrationStatus?.let {
                    sharedPref.saveRegistrationStatus(it)
                }
            }
        }
    }

    override fun apiDeleteAccount(): Single<SimpleApiResponse> {
        return api.apiDeleteAccount()
    }

    override fun apiDeliveryTimingOptionList(): Single<ApiResponse<List<DeliveryTimingOption>>> {
        return api.apiDeliveryTimingOptionList()
    }

    override fun apiUploadMedia(file: MediaFile): Flowable<FileUploadResource<String>> {
        return Flowable.create({ emitter ->
            val part = RetroUtils().getMultipartBody(file.localUrl, "file", emitter)
            if (part == null) {
                emitter.tryOnError(NullPointerException("File is null"))
            } else {
                val response = api.apiUploadMedia(part).blockingGet()
                if (response.isSuccessful) {
                    emitter.onNext(
                        FileUploadResource(
                            status = FileUploadResource.Status.SUCCESS,
                            message = response.message,
                            result = response.data
                        )
                    )
                } else {
                    emitter.onNext(
                        FileUploadResource(
                            status = FileUploadResource.Status.ERROR, message = response.message
                        )
                    )

                }
            }
            emitter.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun apiCountryCodeList(): Single<ApiResponse<List<CountryCode>>> {
        return api.apiCountryCode()
    }

    override fun apiSafetyOptionList(): Single<ApiResponse<List<SafetyOption>>> {
        return api.apiSafetyOptionList()
    }

    override fun apiGetPreferences(): Single<ApiResponse<AppPreferences>> {
        return api.apiGetPreferences().doOnSuccess { response ->
            response.data?.let {
                sharedPref.saveAppPreference(it)
            }
        }
    }

    override fun apiSafetyFaq(): Single<ApiResponse<List<TitleDescription>>> {
        return api.apiSafetyFaq()
    }

    override fun apiReasonTypeList(type: String): Single<ApiResponse<List<TitleDescription>>> {
        return api.apiReasonTypeList(type)
    }

    override fun apiReportSafetyIssue(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiReportSafetyIssue(request.toApplicationJsonBody())
    }

    override fun apiCancelDelivery(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiCancelDelivery(request.toApplicationJsonBody())
    }

    override fun apiSaveRecording(request: JSONObject): Single<SimpleApiResponse> {
        return api.apiSaveRecording(request.toApplicationJsonBody())
    }


}
