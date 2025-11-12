package com.tevioapp.vendor.injection.helper

import android.content.Context
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.extensions.asCommaSeparatedString
import com.tevioapp.vendor.utility.extensions.getLatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    @ApplicationContext val context: Context, val sharedPref: SharedPref
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        when (sharedPref.getTokenType()) {
            Enums.TOKEN_TYPE_BEARER -> request.addHeader(
                "Authorization", "Bearer ${sharedPref.getToken()}"
            )

            Enums.TOKEN_TYPE_TEMP -> request.addHeader(
                "token", sharedPref.getToken().orEmpty()
            )
        }
        getLocationFromPref()?.let {
            request.addHeader("Location", it)
        }
        request.addHeader("Device-Type", AppConstants.DEVICE_TYPE_ANDROID)

        return chain.proceed(request.build())
    }

    private fun getLocationFromPref(): String? {
        return BaseApp.instance.lastLocation?.getLatLng()?.asCommaSeparatedString()
    }

}