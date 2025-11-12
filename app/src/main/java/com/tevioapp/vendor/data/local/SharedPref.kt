package com.tevioapp.vendor.data.local

import android.content.SharedPreferences
import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.VehicleInfo
import com.tevioapp.vendor.data.common.Profile

interface SharedPref {
    fun saveRegistrationStatus(status: String?)
    fun getRegistrationStatus(): String?
    fun saveToken(token: String?)
    fun getTokenType(): String?
    fun isLoggedIn(): Boolean
    fun getToken(): String?
    fun saveProfile(profile: Profile?)
    fun getProfile(): Profile?

    fun saveVehicleInfo(vehicleInfo: VehicleInfo?)
    fun getVehicleInfo(): VehicleInfo?
    fun clearPref()
    fun clearUser()
    fun saveAppPreference(appPreferences: AppPreferences)
    fun getAppPreference(): AppPreferences
    fun getMyUserId(): String?
    fun getCustomLocationTime(): Long
    fun resetCustomLocationTime()
    fun setGoogleMapType(mapType: Int)
    fun getGoogleMapType(): Int
    fun getBiometricEnabled(): Boolean
    fun setBiometricEnabled(enabled: Boolean)

    fun saveRiderStatus(status: String)
    fun getRiderStatus(): String
    fun isRiderOnline(): Boolean

    fun setLocalProfileImage(uri: String?)
    fun getProfileImage(): String
    fun getSharePreference(): SharedPreferences
}