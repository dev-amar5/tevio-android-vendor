package com.tevioapp.vendor.presentation.views.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.UserLocation
import com.tevioapp.vendor.databinding.ActivityLocationBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.animation.LocationPinAnimation
import com.tevioapp.vendor.utility.extensions.asCommaSeparatedString
import com.tevioapp.vendor.utility.extensions.asLatLng
import com.tevioapp.vendor.utility.extensions.drawBelowStatusBar
import com.tevioapp.vendor.utility.extensions.findParcelData
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.extensions.hideKeyboard
import com.tevioapp.vendor.utility.keyboard.KeyboardDetector
import com.tevioapp.vendor.utility.location.GoogleMapUtil
import com.tevioapp.vendor.utility.location.LocationDetector
import com.tevioapp.vendor.utility.permissions.PermissionsUtil
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.parcelize.Parcelize


@AndroidEntryPoint
class LocationActivity : BaseActivity<ActivityLocationBinding>() {
    private var currentMarker: Marker? = null
    private lateinit var inputData: InputData
    private var moveReason: Int = 0
    private val viewModel: LocationActivityVM by viewModels()
    private var currentLocation: Location? = null
    private var newLocation: LatLng? = null
    private var gpsLocationFound: Boolean = false
    private var mMap: GoogleMap? = null
    private val locationDetector by lazy { LocationDetector(this) }
    private lateinit var locationPinAnimation: LocationPinAnimation

    override fun getLayoutResource(): Int {
        return R.layout.activity_location
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        initView()
        initCenterView()
        setObserver()
        setPlaceSearchView()
        addLocationDetector()
        hideAddressErrorView()
        addKeyBoardListener()
    }

    private fun addKeyBoardListener() {
        viewModel.compositeDisposable.add(
            KeyboardDetector(this).getObserver().subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    //   addressSheet?.onKeyBoardOpen(it.isOpen)
                })
    }


    private fun setPlaceSearchView() {
        binding.pavSearch.activateAutoCompleteView(viewModel) { result ->
            hideKeyboard(binding.pavSearch.getSearchView())
            animateCameraToPosition(result.latLng)
            false
        }
        binding.pavSearch.onFocusChange = { hasFocus ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.vHeader)
            if (hasFocus) {
                constraintSet.connect(
                    R.id.pav_search,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
            } else {
                constraintSet.connect(
                    R.id.pav_search, ConstraintSet.START, R.id.iv_back, ConstraintSet.END
                )
            }
            constraintSet.connect(
                R.id.pav_search, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            val transition = ChangeBounds()
            transition.duration = 100
            transition.interpolator = AccelerateDecelerateInterpolator()
            TransitionManager.beginDelayedTransition(binding.vHeader, transition)
            constraintSet.applyTo(binding.vHeader)
        }
    }

    override fun setupInsets() {
        drawBelowStatusBar {
            binding.vHeader.apply {
                val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = it.top
                this.layoutParams = layoutParams
            }
            binding.vBottomSheet.apply {
                val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin = it.bottom
                this.layoutParams = layoutParams
            }

        }
    }

    private fun initView() {
        intent.findParcelData("inputData", InputData::class.java)?.let {
            inputData = it
        } ?: run {
            finish()
            return
        }
        locationPinAnimation = LocationPinAnimation(binding.ivLocationPin)
        binding.mapView.getMapAsync {
            mMap = it
            it.setPadding(10, 50, 10, 50)
            GoogleMapUtil.setDefaultUISettings(it)
            GoogleMapUtil.setMapStyle(this, it)
            it.setOnMapClickListener { latLng ->
                animateCameraToPosition(latLng)
            }
            it.setOnMapLoadedCallback {
                binding.mapOverlay.animate().alpha(0f)
            }
            setLastLocation()
        }
    }

    private fun setLastLocation() {
        newLocation = inputData.latLng ?: locationDetector.lastLocation()?.getLatLng()
        moveCameraToPosition(newLocation)
        setLocationInView()
        mMap?.apply {
            setOnCameraIdleListener {
                if (gpsLocationFound && (moveReason == REASON_GESTURE || moveReason == REASON_DEVELOPER_ANIMATION)) {
                    newLocation = cameraPosition.target
                    setLocationInView()
                }
                if (moveReason == REASON_GESTURE) {
                    adjustMapCenterOnZoom()
                }
            }
            setOnCameraMoveStartedListener { reason ->
                baseHandler.removeCallbacks(locationViewRunnable)
                moveReason = reason
                hideLocationPinView()
            }
        }
    }


    private fun setObserver() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.btn_confirm -> {
                    confirmLocation()
                }

                R.id.iv_current -> {
                    animateCameraToCurrentLocation()
                }

                R.id.iv_back -> onBack()
            }
        }
    }

    private fun animateCameraToCurrentLocation() {
        currentLocation = locationDetector.lastLocation()
        currentLocation?.getLatLng()?.let {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it, AppConstants.MAP_ZOOM_LARGE
                )
            )
        } ?: run {
            getLatestLocation(true)
        }
    }

    private fun confirmLocation() {
        val latLng = newLocation ?: return
        setResult(RESULT_OK, Intent().apply {
            putExtra("result", latLng.asCommaSeparatedString())
        })
        finish()

    }


    private fun onBack() {
        if (binding.pavSearch.resetSearch()) {
            hideKeyboard()
        } else {
            finish()
        }
    }

    private fun hideAddressErrorView() {
        binding.ivCurrent.isVisible = true
    }


    /**
     * initialize location detector and register listener
     */
    private fun addLocationDetector() {
        locationDetector.withListener(object : LocationDetector.Listener {
            override fun onError(message: String) {
                showLongMessage(message)
            }

            override fun onDetectorStatusChanged(loading: Boolean) {
                if (loading) {
                    locationPinAnimation.startAnimation()
                    loadingUtils.startShimmer()
                } else {
                    locationPinAnimation.stopAnimation()
                    loadingUtils.stopShimmer()
                }
            }

            override fun onGpsPermissionDeny() {
                showLongMessage(getString(R.string.allow_gps_permission))
            }

            override fun onLocationFound(location: Location) {
                gpsLocationFound = true
                currentLocation = location
                setCurrentLocationPin(location)/*   if (inputData.userAddress == null && inputData.latLng == null) animateCameraToPosition(
                       location.getLatLng()
                   )*/
            }

            override fun onPermissionPending() {
                runWithPermissions(
                    *PermissionsUtil.getForLocation(), options = QuickPermissionsOptions(
                        handleRationale = true,
                        rationaleMessage = getString(R.string.request_location_permission_msg)
                    )
                ) {
                    getLatestLocation(false)
                }
            }

            override fun onGpsPermissionPending(request: IntentSenderRequest) {
                launcherGps.launch(request)
            }

        })
    }

    private fun setCurrentLocationPin(location: Location) {
        mMap?.apply {
            clear()
            currentMarker = addMarker(
                MarkerOptions().position(location.getLatLng()).icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_location_dot)
                ).anchor(0.5f, 0.5f).zIndex(2f)
            )
            addCircle(
                CircleOptions().center(location.getLatLng()).radius(location.accuracy.toDouble())
                    .fillColor(
                        ContextCompat.getColor(
                            this@LocationActivity, R.color.map_circle_inner
                        )
                    ).strokeWidth(0f).zIndex(1f)
            )
        }
    }

    private fun setLocationInView() {
        val userLocation = UserLocation.fromLatLng(this, newLocation)
        if (userLocation == null) {
            binding.vBottomSheet.isVisible = false
        } else {
            binding.tvLocName.text = userLocation.title
            binding.tvLocAddress.text = userLocation.address
            binding.ivLocType.setImageResource(R.drawable.ic_location)
            binding.vBottomSheet.isVisible = true
        }
        baseHandler.postDelayed(locationViewRunnable, 2500)
    }


    private fun animateCameraToPosition(latLng: LatLng?) {
        if (latLng != null) mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng, AppConstants.MAP_ZOOM_LARGE
            )
        )
    }

    private fun moveCameraToPosition(latLng: LatLng?) {
        if (latLng != null) mMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng, AppConstants.MAP_ZOOM_LARGE
            )
        )
    }


    private fun initCenterView() {
        binding.vSelectLocation.root.apply {
            alpha = 0f
        }
    }

    private val locationViewRunnable = {
        if (newLocation != null) {
            showLocationPinView()
        }
    }

    private fun showLocationPinView() {
        binding.vSelectLocation.apply {
            tvHeading.text = binding.tvLocName.text.toString()
            tvSubHeading.text = inputData.description
            if (newLocation != null) root.animate().alpha(1f).setDuration(300).start()
        }
    }

    private fun hideLocationPinView() {
        binding.vSelectLocation.apply {
            root.animate().alpha(0f).setDuration(200).start()
        }
    }

    private val launcherGps =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            locationDetector.onActivityResult(activityResult.resultCode)
        }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        baseHandler.postDelayed(
            { getLatestLocation(true) }, 200
        )
    }

    private fun getLatestLocation(resetGpsRequestCount: Boolean) {
        if (gpsLocationFound) return
        locationDetector.startLocationDetector(
            detectorType = LocationDetector.DETECTOR_ONE_SHOT,
            resetGpsRequestCount = resetGpsRequestCount,
            locationAccuracy = AppConstants.MIN_LOCATION_ACCURACY
        )
    }

    private fun adjustMapCenterOnZoom() {
        mMap?.apply {
            currentMarker?.let { marker ->
                val zoomLevel = cameraPosition.zoom
                val currentCenter = cameraPosition.target
                val results = FloatArray(1)
                Location.distanceBetween(
                    currentCenter.latitude,
                    currentCenter.longitude,
                    marker.position.latitude,
                    marker.position.longitude,
                    results
                )
                if (results[0] <= 2) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, zoomLevel))
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        locationDetector.stopLocationDetector()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    @Parcelize
    data class InputData(
        val latLng: LatLng? = null, val description: String = "Confirm location"
    ) : Parcelable


    class LocationContract : ActivityResultContract<InputData, LatLng?>() {
        override fun createIntent(context: Context, input: InputData): Intent {
            return Intent(context, LocationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("inputData", input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): LatLng? {
            if (resultCode == Activity.RESULT_OK) {
                return intent?.getStringExtra("result")?.asLatLng()
            }
            return null
        }
    }

}

