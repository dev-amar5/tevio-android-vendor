package com.tevioapp.vendor.presentation

import android.app.Application
import android.location.Location
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.location.GoogleMapUtil
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.util.DataProvider
import com.tevioapp.vendor.utility.util.ThemeUtil
import com.tevioapp.vendor.utility.workers.UpdateLocationWorker
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dataProvider: DataProvider

    @Inject
    lateinit var sharedPref: SharedPref

    private var _countryCode: String? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        RxJavaPlugins.setErrorHandler {
            Logger.e("UncaughtError= ${it.printStackTrace()}")
        }
        WorkManager.initialize(this, workManagerConfiguration)
        if (Places.isInitialized().not()) {
            Places.initialize(applicationContext, getString(R.string.google_map_key))
        }
        ThemeUtil.applySavedTheme(this)
    }


    companion object {
        @get:Synchronized
        lateinit var instance: BaseApp
        private var _lastLocation: Location? = null
    }

    var lastLocation: Location?
        get() = _lastLocation
        set(value) {
            if (value != null) {
                val isLocationDifferent = GoogleMapUtil.isLocationUpdated(_lastLocation, value)
                _lastLocation = value
                if (isLocationDifferent) {
                    dataProvider.emitLocation(value)
                    val pair = refreshCountryCode()
                    if (pair.second != null && pair.first != pair.second) {
                        _countryCode = pair.second
                    }
                    if (sharedPref.isRiderOnline()) {
                        UpdateLocationWorker.createJob(this, value)
                    }
                }
            }
        }

    fun getCountryCode(): String {
        return _countryCode.orEmpty().ifEmpty { "GH" }
    }

    fun refreshCountryCode(): Pair<String, String?> {
        return Pair(
            getCountryCode(),
            GoogleMapUtil.getCountryCodeFromLatLng(this, _lastLocation?.getLatLng())
        )
    }


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

}