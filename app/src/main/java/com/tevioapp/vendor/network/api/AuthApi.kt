package com.tevioapp.vendor.network.api

import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.AuthResponse
import com.tevioapp.vendor.data.DeliveryTimingOption
import com.tevioapp.vendor.data.DocumentResponse
import com.tevioapp.vendor.data.PayoutResponse
import com.tevioapp.vendor.data.SafetyOption
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.data.common.TitleDescription
import com.tevioapp.vendor.network.helper.ApiResponse
import com.tevioapp.vendor.network.helper.PagingResponse
import com.tevioapp.vendor.network.helper.SimpleApiResponse
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.presentation.views.country.CountryCode
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/otp")
    fun apiSendOtp(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/logout")
    fun apiLogout(): Single<SimpleApiResponse>


    @DELETE("auth/courier/delete-account")
    fun apiDeleteAccount(): Single<SimpleApiResponse>

    @POST("auth/verify-otp")
    fun apiVerifyOtp(
        @Body map: RequestBody
    ): Single<ApiResponse<AuthResponse>>


    @GET("auth/courier/registration-status")
    fun apiRegistrationStatus(): Single<ApiResponse<RegistrationStepsActivity.Steps>>

    @POST("auth/register")
    fun apiSetPersonalInfo(@Body map: RequestBody): Single<ApiResponse<String>>

    @POST("auth/courier/update-location")
    fun apiUpdateLocation(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/delete-multiple-media")
    fun apiDeleteMedia(@Body map: RequestBody): Single<SimpleApiResponse>

    @GET("auth/courier/personal-info")
    fun apiGetPersonalInfo(): Single<ApiResponse<Profile>>


    @GET("auth/courier/preferences/timing-options")
    fun apiDeliveryTimingOptionList(): Single<ApiResponse<List<DeliveryTimingOption>>>

    @POST("auth/courier/payout-info")
    fun apiSetPayoutInfo(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/preferences")
    fun apiSetWorkSettings(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/onboarding/videos/update-stats")
    fun apiUpdateTrainingVideoStats(@Body map: RequestBody): Single<SimpleApiResponse>

    @GET("auth/courier/payout-info")
    fun apiGetPayoutInfo(): Single<ApiResponse<PayoutResponse>>

    @POST("auth/courier/documents")
    fun apiSetDocumentsInfo(@Body map: RequestBody): Single<SimpleApiResponse>

    @GET("auth/courier/documents")
    fun apiGetDocumentsInfo(): Single<ApiResponse<DocumentResponse>>

    @PATCH("auth/courier/update-profile-pic")
    fun apiUpdateProfilePic(@Body map: RequestBody): Single<SimpleApiResponse>

    @GET("auth/country-data")
    fun apiCountryCode(): Single<ApiResponse<List<CountryCode>>>

    @GET("auth/courier/safety/options")
    fun apiSafetyOptionList(): Single<ApiResponse<List<SafetyOption>>>


    @Multipart
    @POST("auth/upload-media")
    fun apiUploadMedia(
        @Part parts: MultipartBody.Part
    ): Single<ApiResponse<String>>

    @GET("auth/users/preferences")
    fun apiGetPreferences(
    ): Single<ApiResponse<AppPreferences>>

    @GET("auth/courier/safety/usage-guide")
    fun apiSafetyFaq(): Single<ApiResponse<List<TitleDescription>>>

    @GET("auth/courier/safety/issue-types")
    fun apiReasonTypeList(@Query("type") type: String): Single<ApiResponse<List<TitleDescription>>>

    @POST("auth/courier/request-phone-change")
    fun apiValidatePhone(@Body map: RequestBody): Single<ApiResponse<String>>

    @POST("auth/courier/verify-phone-change")
    fun apiChangePhone(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/safety/report-issue")
    fun apiReportSafetyIssue(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/courier/report-issue")
    fun apiCancelDelivery(@Body map: RequestBody): Single<SimpleApiResponse>

    @POST("auth/courier/safety/save-recording")
    fun apiSaveRecording(@Body map: RequestBody): Single<SimpleApiResponse>

}