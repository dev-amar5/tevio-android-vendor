package com.tevioapp.vendor.repositary.auth

import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.AuthResponse
import com.tevioapp.vendor.data.Campaigns
import com.tevioapp.vendor.data.DeliveryTimingOption
import com.tevioapp.vendor.data.DeliveryTypeOption
import com.tevioapp.vendor.data.DocumentResponse
import com.tevioapp.vendor.data.DressKit
import com.tevioapp.vendor.data.JoiningBenefitsOption
import com.tevioapp.vendor.data.PayoutResponse
import com.tevioapp.vendor.data.SafetyOption
import com.tevioapp.vendor.data.Ticket
import com.tevioapp.vendor.data.TrainingVideo
import com.tevioapp.vendor.data.VideoStats
import com.tevioapp.vendor.data.common.FileUploadResource
import com.tevioapp.vendor.data.common.MediaFile
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.data.common.TitleDescription
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.presentation.views.country.CountryCode
import io.reactivex.Flowable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject

interface AuthRepo {
    fun sendOtpApi(request: JSONObject): Single<SimpleApiResponse>
    fun verifyOtpApi(request: JSONObject): Single<ApiResponse<AuthResponse>>
    fun apiUploadMedia(file: MediaFile): Flowable<FileUploadResource<String>>
    fun apiCountryCodeList(): Single<ApiResponse<List<CountryCode>>>
    fun apiGetPreferences(): Single<ApiResponse<AppPreferences>>

    fun apiSetPersonalInfo(request: JSONObject): Single<ApiResponse<String>>
    fun apiRegistrationStatus(): Single<ApiResponse<RegistrationStepsActivity.Steps>>
    fun apiGetPersonalInfo(): Single<ApiResponse<Profile>>
    fun apiDeleteAccount(): Single<SimpleApiResponse>
    fun apiSetPayoutInfo(request: JSONObject): Single<SimpleApiResponse>
    fun apiGetPayoutInfo(): Single<ApiResponse<PayoutResponse>>
    fun apiSetDocumentsInfo(request: JSONObject): Single<SimpleApiResponse>
    fun apiGetDocumentsInfo(): Single<ApiResponse<DocumentResponse>>
    fun apiUpdateProfilePic(request: JSONObject): Single<SimpleApiResponse>
    fun apiDeliveryTypeOptionList(): Single<ApiResponse<List<DeliveryTypeOption>>>

    fun apiDeliveryTimingOptionList(): Single<ApiResponse<List<DeliveryTimingOption>>>
    fun apiSetWorkSettings(request: JSONObject): Single<SimpleApiResponse>
    fun apiJoiningBenefitsOptionList(): Single<ApiResponse<List<JoiningBenefitsOption>>>
    fun apiDressKitList(): Single<ApiResponse<List<DressKit>>>
    fun apiTrainingVideoList(): Single<ApiResponse<List<TrainingVideo>>>
    fun apiUpdateTrainingVideoStats(list: List<VideoStats>): Single<SimpleApiResponse>
    fun apiUpdateLocation(request: JSONObject): Single<SimpleApiResponse>
    fun apiDeleteMedia(urls: JSONArray): Single<SimpleApiResponse>

    fun apiValidatePhone(request: JSONObject): Single<ApiResponse<String>>
    fun apiChangePhone(request: JSONObject): Single<SimpleApiResponse>
    fun apiSafetyOptionList(): Single<ApiResponse<List<SafetyOption>>>
    fun apiLogout(): Single<SimpleApiResponse>
    fun apiSafetyFaq(): Single<ApiResponse<List<TitleDescription>>>
    fun apiReportSafetyIssue(request: JSONObject): Single<SimpleApiResponse>
    fun apiSaveRecording(request: JSONObject): Single<SimpleApiResponse>
    fun apiReasonTypeList(type: String): Single<ApiResponse<List<TitleDescription>>>
    fun apiCancelDelivery(request: JSONObject): Single<SimpleApiResponse>
    fun apiTicketList(type: String): Single<PagingResponse<List<Ticket>>>
    fun apiTicketDetail(ticketId: String): Single<ApiResponse<Ticket>>
    fun apiCampaignsList(text: String):  Single<ApiResponse<List<Campaigns>>>
    fun apiCampaignsDetail(id: String):  Single<ApiResponse<Campaigns>>
}