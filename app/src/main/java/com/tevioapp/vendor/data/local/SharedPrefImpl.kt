package com.tevioapp.vendor.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.WorkManager
import com.google.android.gms.maps.GoogleMap
import com.google.gson.Gson
import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.VehicleInfo
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.util.DataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val dataProvider: DataProvider
) : SharedPref {

    private val gson = Gson()

    override fun saveRegistrationStatus(status: String?) {
        sharedPreferences.edit {
            if (status.isNullOrEmpty()) remove(KEY_ACCOUNT_STATUS)
            else putString(KEY_ACCOUNT_STATUS, status)
        }
    }

    override fun getRegistrationStatus(): String? {
        return sharedPreferences.getString(KEY_ACCOUNT_STATUS, null)
    }

    override fun saveToken(token: String?) {
        sharedPreferences.edit {
            if (token.isNullOrEmpty()) remove(KEY_TOKEN)
            else putString(KEY_TOKEN, token)
        }
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    override fun getTokenType(): String? {
        return if (getToken() == null) null
        else if (getRegistrationStatus() == Enums.REGISTRATION_STATUS_PENDING) Enums.TOKEN_TYPE_TEMP
        else Enums.TOKEN_TYPE_BEARER
    }

    override fun isLoggedIn(): Boolean {
        return getTokenType() != null
    }

    override fun clearPref() {
        WorkManager.getInstance(context).cancelAllWork()
        sharedPreferences.edit { clear() }
    }

    override fun clearUser() {
        dataProvider.emitActiveOrders(emptyList())
        saveRiderStatus(Enums.RIDER_STATUS_OFFLINE)
        saveToken(null)
        saveRegistrationStatus(null)
        saveProfile(null)
    }

    override fun saveProfile(profile: Profile?) {
        sharedPreferences.edit {
            if (profile != null) putString(KEY_PROFILE, gson.toJson(profile))
            else remove(KEY_PROFILE)
        }
    }

    override fun getProfile(): Profile? {
        return try {
            sharedPreferences.getString(KEY_PROFILE, null)?.let {
                gson.fromJson(it, Profile::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun saveVehicleInfo(vehicleInfo: VehicleInfo?) {
        sharedPreferences.edit {
            if (vehicleInfo != null) putString(KEY_VEHICLE, gson.toJson(vehicleInfo))
            else remove(KEY_VEHICLE)
        }
    }

    override fun getVehicleInfo(): VehicleInfo? {
        return try {
            sharedPreferences.getString(KEY_VEHICLE, null)?.let {
                gson.fromJson(it, VehicleInfo::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun saveAppPreference(appPreferences: AppPreferences) {
        sharedPreferences.edit { putString(KEY_APP_PREF, gson.toJson(appPreferences)) }
    }

    override fun getAppPreference(): AppPreferences {
        val data = sharedPreferences.getString(KEY_APP_PREF, null)
        return try {
            gson.fromJson(data, AppPreferences::class.java)
        } catch (e: Exception) {
            AppPreferences()
        }
    }

    override fun getMyUserId(): String? {
        return getProfile()?.id
    }

    override fun getCustomLocationTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_LOCATION_DETECT_TIME, 0L)
    }

    override fun resetCustomLocationTime() {
        sharedPreferences.edit {
            putLong(KEY_LAST_LOCATION_DETECT_TIME, System.currentTimeMillis())
        }
    }

    override fun setGoogleMapType(mapType: Int) {
        sharedPreferences.edit { putInt(KEY_GOOGLE_MAP_TYPE, mapType) }
    }

    override fun getGoogleMapType(): Int {
        return sharedPreferences.getInt(KEY_GOOGLE_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
    }

    override fun getBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    override fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
    }

    override fun saveRiderStatus(status: String) {
        sharedPreferences.edit { putString(KEY_RIDER_STATUS, status) }
    }

    override fun getRiderStatus(): String {
        return sharedPreferences.getString(KEY_RIDER_STATUS, null) ?: Enums.RIDER_STATUS_OFFLINE
    }

    override fun isRiderOnline(): Boolean {
        return getRiderStatus() == Enums.RIDER_STATUS_ONLINE
    }

    override fun setLocalProfileImage(uri: String?) {
        sharedPreferences.edit {
            if (uri.isNullOrEmpty()) remove(KEY_LOCAL_PROFILE_URI)
            else putString(KEY_LOCAL_PROFILE_URI, uri)
        }
    }

    override fun getProfileImage(): String {
        val localUri = sharedPreferences.getString(KEY_LOCAL_PROFILE_URI, null).orEmpty()
        return localUri.ifEmpty { getProfile()?.profilePic.orEmpty() }
    }

    override fun getSharePreference(): SharedPreferences {
        return sharedPreferences
    }

    companion object {
        const val KEY_ACCOUNT_STATUS = "account_status"
        const val KEY_TOKEN = "token"
        const val KEY_PROFILE = "profile"
        const val KEY_VEHICLE = "vehicle"
        const val KEY_APP_PREF = "app_pref"
        const val KEY_LAST_LOCATION_DETECT_TIME = "last_location_detect_time"
        const val KEY_GOOGLE_MAP_TYPE = "google_map_type"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_RIDER_STATUS = "rider_status"
        const val KEY_LOCAL_PROFILE_URI = "local_uri"
    }
}
