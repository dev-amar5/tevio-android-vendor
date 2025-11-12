package com.tevioapp.vendor.presentation.views.main.dashboard


import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.Empty
import com.tevioapp.vendor.databinding.FragmentDashboardBinding
import com.tevioapp.vendor.databinding.ItemBannerBinding
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.adapter.QuickAdapter
import com.tevioapp.vendor.presentation.common.base.sheet.BottomSheetController
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.animateVisibility
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.extensions.getLatLngObject
import com.tevioapp.vendor.utility.extensions.setSystemBarInsetListener
import com.tevioapp.vendor.utility.extensions.setTopMarginFromInsets
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.location.GoogleMapUtil.enableCurrentLocation
import com.tevioapp.vendor.utility.location.GoogleMapUtil.setDefaultUISettings
import com.tevioapp.vendor.utility.location.GoogleMapUtil.setMapStyle
import com.tevioapp.vendor.utility.location.HeatMapManager
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.order.StatusHelper
import com.tevioapp.vendor.utility.transform.SpaceDecorationHorizontal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class DashBoardFragment : BaseFragment<FragmentDashboardBinding>() {
    private lateinit var heatMapManager: HeatMapManager
    private lateinit var sheetControllerStatus: BottomSheetController
    private val viewModel: DashBoardFragmentVM by viewModels()
    private lateinit var googleMap: GoogleMap
    private var currentLocation: Location? = null
    override fun onCreateView(view: View, saveInstanceState: Bundle?) {
        binding.map.onCreate(saveInstanceState)
        startShimmer()
        addInitView()
        addGoogleMapView()
        doRegisterObservers()
        getProfileData()
        updateDataInView()
    }


    /**
     * initialize views and data
     */
    private fun addInitView() {
        binding.btnGoOnline.setLoading(loading = true, animate = false)
        sheetControllerStatus = BottomSheetController(
            context = requireContext(),
            bottomSheet = binding.sheetOne,
            onSlide = { _, offset ->
                Logger.d("Slide offset: $offset")
                binding.flAnchor.alpha = 1 - offset
            },
            onStateChanged = { _, state -> Logger.d("State changed: $state") })
        binding.labelCampaign.label = getString(R.string.campaigns)
        setSystemBarInsetListener {
            binding.flTop.setTopMarginFromInsets(it)
        }
        binding.rvBanner.apply {
            addItemDecoration(
                SpaceDecorationHorizontal(
                    context = baseContext, middleSpace = R.dimen.rv_middle_space
                )
            )
            LinearSnapHelper().attachToRecyclerView(this)
        }
    }

    private fun getProfileData() {
        viewModel.apiGetPersonalInfo().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    startShimmer()
                }

                Status.SUCCESS -> {
                    resource.data?.registrationStatus?.let { status ->
                        if (status == Enums.REGISTRATION_STATUS_APPROVED) {
                            binding.vStatus.isVisible = false
                        } else {
                            binding.vStatus.isVisible = true
                            val statusUI = StatusHelper.getRegistrationStatusUI(status)
                            binding.tvStatusTitle.text = statusUI?.title
                            binding.tvStatusDescription.text = statusUI?.description
                            binding.ivStatusIcon.setImageResource(statusUI?.iconRes ?: 0)

                        }
                    }
                    withDelay(500) {
                        stopShimmer()
                    }
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }
        }
    }

    /**
     * initialize google map utils and register listener
     */
    private fun addGoogleMapView() {
        binding.map.getMapAsync { googleMap ->
            this.googleMap = googleMap
            heatMapManager = HeatMapManager(googleMap)
            heatMapManager.enableDynamicUpdate()
            setMapPadding()
            binding.ivMapLayer.setGoogleMap(googleMap)
            googleMap.setOnMapLoadedCallback {
                binding.mapOverlay.animateVisibility(false)
            }
            setDefaultUISettings(googleMap)
            setMapStyle(baseContext, googleMap)
            enableCurrentLocation(baseContext, googleMap)
            viewModel.compositeDisposable.add(dataProvider.subscribeToUserLocation { location ->
                currentLocation = location
                setCurrentLocation()
                updateHeatMapView()
            })

        }
    }

    private fun setMapPadding() {
        if (::googleMap.isInitialized) {
            googleMap.setPadding(20, 50, 20, resources.getDimensionPixelSize(R.dimen._25sdp))
        }
    }

    private fun updateHeatMapView() {
        lifecycleScope.launch(Dispatchers.IO) {
            val latLngList = viewModel.obrHeatMapData.value?.data.orEmpty().map {
                LatLng(it.latitude, it.longitude)
            }
            withContext(Dispatchers.Main) {
                if (::heatMapManager.isInitialized) heatMapManager.setPoints(latLngList)
            }
        }
    }


    /**
     * default map view location
     */
    private fun setCurrentLocation() {
        val latLng = currentLocation?.getLatLng() ?: return
        try {
            val s = CameraUpdateFactory.newLatLngZoom(
                latLng, AppConstants.MAP_ZOOM_SMALL
            )
            googleMap.animateCamera(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateDataInView() {
        updateLocationService()
        setOrderStatusView()
        setCampaignView()
        setRiderOnlineView()
    }

    private fun setOrderStatusView() = with(binding.vGreenButton) {
        val courierStatus = dataProvider.courierStatus
        if (courierStatus?.status.orEmpty() != Enums.RIDER_STATUS_ONLINE) {
            animateVisibility(false)
            return@with
        }
        val activeOrders = courierStatus?.activeOrders.orEmpty()
        val availableOrderCount = courierStatus?.availableOrders.orEmpty().count()
        if (activeOrders.isNotEmpty()) {
            setText("${activeOrders.count()} Active Order")
            setOnClickListener {
                activeOrders.getOrNull(0)?.let {
                    navigateToActiveOrderScreen(it.id.orEmpty())
                }
            }
            animateVisibility(true)
        } else if (availableOrderCount > 0) {
            setText("$availableOrderCount Incoming Orders")
            setOnClickListener {
                navigateToNewOrderScreen()
            }
            animateVisibility(true)
        } else {
            animateVisibility(false)
        }
    }


    /**
     * register livedata observer and click listener
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(viewLifecycleOwner) { view ->
            when (view.id) {
                R.id.iv_map_current -> {
                    setCurrentLocation()
                }

                R.id.iv_safety -> {
                    //startActivity(SafetyActivity.newInstance(baseContext))
                }
            }
        }
        viewModel.obrCourierStatus.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (dataProvider.courierStatus == null) binding.btnGoOnline.setLoading(true)
                }

                Status.SUCCESS -> {
                    binding.btnGoOnline.setLoading(false)
                    dataProvider.courierStatus = resource.data
                    updateDataInView()
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }
        }

        viewModel.obrHeatMapData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    //No Loader
                }

                Status.SUCCESS -> {
                    updateHeatMapView()
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }
        }

    }

    private fun updateLocationService() {
        dataProvider.emitActiveOrders(
            dataProvider.courierStatus?.activeOrders.orEmpty().map { it.id.orEmpty() })
    }


    private fun setRiderOnlineView() = with(binding.btnGoOnline) {
        resetSlider(false)
        val toggleStatus = when (dataProvider.courierStatus?.status.orEmpty()) {
            Enums.RIDER_STATUS_OFFLINE -> {
                setSlideEnabled(true)
                setSlideBackgroundColor(ContextCompat.getColor(baseContext, R.color.gray))
                setSlideTextColor(Color.WHITE)
                setText("Go Online")
                Enums.RIDER_STATUS_ONLINE
            }

            Enums.RIDER_STATUS_ONLINE -> {
                setSlideEnabled(isValidGOOffline())
                setText("Go Offline")
                Enums.RIDER_STATUS_OFFLINE
            }

            else -> Enums.RIDER_STATUS_OFFLINE
        }
        setOnSlideComplete {
            val obj = BaseApp.instance.lastLocation?.getLatLngObject()
            if (obj == null) {
                resetSlider(false)
            } else {
                viewModel.changeRiderStatus(toggleStatus)
            }
        }
        setOnSlideStatusChange {
            sheetControllerStatus.setDraggable(it.not())
        }
    }


    private fun setCampaignView() {
        val adapterOffers = QuickAdapter<Empty>(baseContext, getBinding = { root, _ ->
            ItemBannerBinding.inflate(layoutInflater, root, false)
        })
        adapterOffers.addToRecyclerView(
            binding.rvBanner, LinearLayoutManager(baseContext, RecyclerView.HORIZONTAL, false)
        )
        binding.rvBanner.startAutoScroll()
        adapterOffers.setDummyData(Empty(), 5)
    }

    private fun isValidGOOffline(): Boolean {
        val courierStatus = dataProvider.courierStatus ?: return false
        return courierStatus.activeOrders.isNullOrEmpty()
    }

    private fun navigateToNewOrderScreen() {
      //  startActivity(NewOrderActivity.newInstance(baseContext))
    }

    private fun navigateToActiveOrderScreen(orderId: String) {
       // startActivity(ActiveOrderActivity.newInstance(baseContext, orderId))
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
        binding.ivMapLayer.refreshView()
        viewModel.changeRiderStatus(sharePref.getRiderStatus())
        getHeatMapData()
    }

    private fun getHeatMapData() {
        viewModel.apiHeatMap()
    }


    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_dashboard
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}