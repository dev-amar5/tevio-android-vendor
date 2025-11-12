package com.tevioapp.vendor.presentation.views.map


import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.RouteLegInfo
import com.tevioapp.vendor.data.local.SharedPrefImpl
import com.tevioapp.vendor.databinding.FragmentGoogleMapBinding
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.animateVisibility
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.location.GoogleMapOverlays
import com.tevioapp.vendor.utility.location.GoogleMapUtil.setMapStyle
import com.tevioapp.vendor.utility.location.LocationDetector
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.permissions.PermissionsUtil
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import com.tevioapp.vendor.utility.util.AppSettings
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GoogleMapFragment : BaseFragment<FragmentGoogleMapBinding>() {
    private val viewModel: GoogleMapFragmentVM by viewModels()
    private lateinit var googleMap: GoogleMap
    private var mCurrentLocation: Location? = null
    private lateinit var googleMapOverlays: GoogleMapOverlays
    private var mDestination: LatLng? = null
    private var mCallback: Callback? = null
    private var mMockDirection = AppSettings.isMockDirection()
    private lateinit var locationDetector: LocationDetector

    override fun onCreateView(view: View, saveInstanceState: Bundle?) {
        binding.map.onCreate(saveInstanceState)
        addLocationDetector()
        addGoogleMapView()
        doRegisterObservers()
    }


    /**
     * initialize google map utils and register listener
     */
    private fun addGoogleMapView() {
        binding.map.getMapAsync { googleMap ->
            googleMapOverlays =
                GoogleMapOverlays(baseContext, sharePref.getVehicleInfo(), googleMap)
            sharePref.getSharePreference().registerOnSharedPreferenceChangeListener(onMapTypeChange)
            this.googleMap = googleMap
            googleMap.setPadding(10, 10, 10, 10)
            setUISettings()
            setMapStyle(baseContext, googleMap)
            googleMap.setOnMapLoadedCallback {
                binding.mapOverlay.animateVisibility(false)
            }

            viewModel.compositeDisposable.add(dataProvider.subscribeToUserLocation { location ->
                mCurrentLocation = location
                apiGetRouteInfo()
            })
            mCallback?.onGoogleMapReady(googleMap)
        }
    }

    private fun addLocationDetector() {
        locationDetector = LocationDetector(requireActivity())
        locationDetector.withListener(object : LocationDetector.Listener {
            override fun onError(message: String) {
                showShortMessage(message)
            }

            override fun onDetectorStatusChanged(loading: Boolean) {
                // No loader
            }

            override fun onGpsPermissionDeny() {
                askPermission()
            }

            override fun onLocationFound(location: Location) {
                locationDetector.stopLocationDetector()
            }

            override fun onPermissionPending() {
                askPermission()
            }

            override fun onGpsPermissionPending(request: IntentSenderRequest) {
                launcherGps.launch(request)
            }

        })
    }

    private fun askPermission() {
        runWithPermissions(
            *PermissionsUtil.getForLocation(), options = QuickPermissionsOptions(
                handleRationale = true,
                rationaleMessage = getString(R.string.request_location_permission_msg)
            )
        ) {
            getLatestLocation()
        }
    }

    private fun setUISettings() {
        googleMap.uiSettings.apply {
            isScrollGesturesEnabledDuringRotateOrZoom = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isMyLocationButtonEnabled = false
        }
        googleMap.isBuildingsEnabled = true
        googleMap.setMinZoomPreference(AppConstants.MAP_MAX_ZOOM_OUT)
    }

    fun setDefaultCurrentLocation(enable: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                baseContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                baseContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = enable
    }

    fun setDestination(destination: LatLng?) {
        mDestination = destination
        apiGetRouteInfo()
    }

    fun setMockDirection(enable: Boolean) {
        mMockDirection = enable
    }

    fun getDestination(): LatLng? = mDestination

    fun setCameraOnCurrentPosition() {
        googleMapOverlays.cameraOnPosition(mCurrentLocation?.getLatLng())
    }

    fun isReachedToDestination(): Boolean {
        return googleMapOverlays.isReachedToDestination()
    }

    private fun apiGetRouteInfo() {
        val origin = mCurrentLocation?.getLatLng() ?: return
        val destination = mDestination
        if (destination != null) {
            viewModel.getRouteInfo(
                origin = origin, destination = destination, mMockDirection
            )
        } else {
            googleMapOverlays.setCurrentLocation(mCurrentLocation)
            googleMapOverlays.setDestination(null)
            googleMapOverlays.setRouteInfo(null)
            googleMapOverlays.updateMapView()
        }
    }


    /**
     * register livedata observer and click listener
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(viewLifecycleOwner) { view ->
            when (view.id) {
                R.id.iv_map_current -> {
                    googleMapOverlays.cameraOnPosition(mCurrentLocation?.getLatLng())
                }
            }
        }
        viewModel.obrRouteLegInfo.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    googleMapOverlays.setCurrentLocation(mCurrentLocation)
                    googleMapOverlays.setDestination(mDestination)
                    googleMapOverlays.updateMapView()
                }

                Status.SUCCESS -> {
                    googleMapOverlays.setCurrentLocation(mCurrentLocation)
                    googleMapOverlays.setDestination(mDestination)
                    googleMapOverlays.setRouteInfo(resource.data)
                    googleMapOverlays.updateMapView()
                    mCallback?.onRouteLegInfoChange(resource.data)
                }

                else -> {
                    hideLoading()
                    googleMapOverlays.setRouteInfo(null)
                    showLongMessage(resource.message)
                }
            }
        }
    }

    private val launcherGps =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            locationDetector.onActivityResult(activityResult.resultCode)
        }


    private fun getLatestLocation() {
        Logger.d("getLatestLocation")
        locationDetector.startLocationDetector(
            detectorType = LocationDetector.DETECTOR_ONE_SHOT,
            locationAccuracy = AppConstants.MIN_LOCATION_ACCURACY
        )
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()

    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()

    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
        apiGetRouteInfo()
        getLatestLocation()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
        locationDetector.stopLocationDetector()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    private val onMapTypeChange = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == SharedPrefImpl.KEY_GOOGLE_MAP_TYPE) {
            baseHandler.postDelayed({
                googleMapOverlays.updateMapView()
            }, 100)
        }
    }

    override fun onDestroyView() {
        sharePref.getSharePreference().unregisterOnSharedPreferenceChangeListener(onMapTypeChange)
        super.onDestroyView()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_google_map
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    companion object {
        fun getInstance(
            activity: FragmentActivity, container: ViewGroup, callback: Callback
        ): GoogleMapFragment {
            val fragment = GoogleMapFragment()
            fragment.mCallback = callback
            activity.supportFragmentManager.beginTransaction().add(container.id, fragment).commit()
            return fragment
        }
    }

    interface Callback {
        fun onGoogleMapReady(googleMap: GoogleMap)
        fun onRouteLegInfoChange(routeLegInfo: RouteLegInfo?)
    }
}
