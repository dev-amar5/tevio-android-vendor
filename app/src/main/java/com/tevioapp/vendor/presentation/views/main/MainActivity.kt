package com.tevioapp.vendor.presentation.views.main

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ActivityMainBinding
import com.tevioapp.vendor.databinding.ItemDrawerMenuBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.main.setting.SettingsActivity
import com.tevioapp.vendor.presentation.views.profile.ProfileActivity
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.drawBelowStatusBar
import com.tevioapp.vendor.utility.extensions.setTopMarginFromInsets
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.permissions.PermissionsUtil
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import com.tevioapp.vendor.utility.rx.EventBus
import com.tevioapp.vendor.utility.service.LocationTrackingService
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var navController: NavController
    private val viewModel: MainActivityVM by viewModels()
    private val listDrawerMenu = mutableListOf<DrawerMenu>()
    private var exit: Boolean = false
    private var navTo: Int = 0
    private var drawerCloseDuration: Long = 250
    override fun getLayoutResource(): Int {
        return R.layout.activity_main
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        doInitialization()
        initializeNavigate()
        addLocationDetector()
        doRegisterObservers()
        setDrawerMenu()
        startLocationTrackingService()
    }

    override fun onStart() {
        super.onStart()
        setProfileData()
    }

    private fun setProfileData() {
        viewModel.apiGetPersonalInfo().observe(this) { resource ->
            if (resource.status == Status.SUCCESS) {
                binding.profile = resource.data
                binding.profileImage = sharePref.getProfileImage()
            }
        }
    }

    private fun setDrawerMenu() {
        val colorOrange = ContextCompat.getColor(this@MainActivity, R.color.orange)
        val colorOrange12 = ContextCompat.getColor(this@MainActivity, R.color.orange_alpha_12)
        val cardColor = ContextCompat.getColor(this@MainActivity, R.color.card_bg_dual)

        binding.container.apply {
            removeAllViews()
            listDrawerMenu.forEachIndexed { index, item ->
                ItemDrawerMenuBinding.inflate(layoutInflater, this, true).apply {
                    this.ivIcon.setImageResource(item.icon)
                    this.title.text = item.name
                    this.vDot.isVisible = item.selected

                    if (item.selected) {
                        this.ivIcon.setColorFilter(colorOrange)
                        this.title.setTextColor(colorOrange)
                        this.ivIcon.backgroundTintList = ColorStateList.valueOf(colorOrange12)
                    } else {
                        this.ivIcon.setColorFilter(getColor(R.color.black))
                        this.ivIcon.backgroundTintList = ColorStateList.valueOf(cardColor)
                    }

                    // Set top margin
                    val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._10sdp)
                    root.layoutParams = layoutParams

                    root.setOnClickListener {
                        onDrawerMenuClick(index)
                    }
                }
            }
        }
    }

    private fun onDrawerMenuClick(index: Int) {
        binding.drawer.closeDrawer(GravityCompat.START)
        // listDrawerMenu.forEachIndexed { i, item -> item.selected = i == index }
        listDrawerMenu.forEachIndexed { i, item -> item.selected = false }
        setDrawerMenu()
        withDelay(drawerCloseDuration) {
            when (index) {
                0 -> {
//                    startActivity(
//                        CampaignActivity.newInstance(this)
//                    )
                }

                1 -> {
//                        startActivity(
//                            TicketActivity.newInstance(
//                                this
//                            )
//                        )
                }

                2 -> {
                    // Navigate to Rewards
                    // navController.navigate(R.id.frg_rewards)
                }

                3 -> {
                    // Navigate to Refer & Earn
                    //  navController.navigate(R.id.frg_refer_and_earn)
                }

                4 -> {
                    startActivity(SettingsActivity.newInstance(this))
                }
            }
        }
    }

    /**
     * register all live data observer from view model here
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_menu -> {
                    binding.drawer.openDrawer(GravityCompat.START)
                }

                R.id.iv_close_menu -> {
                    binding.drawer.closeDrawer(GravityCompat.START)
                }

                R.id.tv_name -> {
                    binding.drawer.closeDrawer(GravityCompat.START)
                    withDelay(drawerCloseDuration) {
                        startActivity(ProfileActivity.newInstance(this))
                    }
                }
            }
        }
        viewModel.compositeDisposable.add(dataProvider.subscribeToActiveOrders {
            startLocationTrackingService()
        })
        viewModel.obrMinutesChange().observe(this) {
            startLocationTrackingService()
        }

    }


    /**
     * initialize all views and variables here
     */
    private fun doInitialization() {
        listDrawerMenu.add(DrawerMenu(R.drawable.ic_prize_cup, getString(R.string.campaigns)))
        listDrawerMenu.add(DrawerMenu(R.drawable.ic_ticket, getString(R.string.tickets)))
        listDrawerMenu.add(DrawerMenu(R.drawable.ic_medal_star, getString(R.string.rewards)))
        listDrawerMenu.add(
            DrawerMenu(
                R.drawable.ic_ticket_star, getString(R.string.refer_and_earn)
            )
        )
        listDrawerMenu.add(DrawerMenu(R.drawable.ic_setting, getString(R.string.settings)))
        binding.profile = sharePref.getProfile()
        binding.profileImage = sharePref.getProfileImage()
    }

    /**
     * initialize all views and variables here
     */
    private fun initializeNavigate() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fc_main) as NavHostFragment
        navController = navHostFragment.navController
        binding.bvMain.setupWithNavController(navController)
        navTo = intent.getIntExtra("nav_to", 0)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            animateGreenLine(destination.id)
        }

        binding.bvMain.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == item.itemId) {
                navController.popBackStack(item.itemId, inclusive = true)
                navController.navigate(item.itemId)
                animateGreenLine(item.itemId)
                return@setOnItemSelectedListener true
            }
            navController.navigate(
                item.itemId,
                null,
                NavOptions.Builder().setPopUpTo(R.id.frg_dashboard, inclusive = false).build()
            )
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.frg_dashboard) {
                    if (exit) {
                        finish()
                    } else {
                        exit = true
                        showShortMessage("Back press again to exit")
                        baseHandler.postDelayed({ exit = false }, 2000)
                    }
                } else {
                    navController.navigateUp()
                }
            }
        })
        binding.bvMain.post {
            animateGreenLine(
                R.id.frg_dashboard
            )
        }
    }

    private fun animateGreenLine(itemId: Int) {
        val index = when (itemId) {
            R.id.frg_dashboard -> 0
            R.id.frg_order_activity -> 1
            R.id.frg_wallet -> 2
            R.id.frg_notification -> 3
            else -> return
        }
        val margin = resources.getDimensionPixelSize(R.dimen.bottom_nav_line_margin)
        val itemWidth = binding.bvMain.width / binding.bvMain.menu.size
        val layoutParams = binding.lineGreen.layoutParams
        layoutParams.width = itemWidth - margin
        binding.lineGreen.layoutParams = layoutParams
        val targetX = itemWidth * index
        binding.lineGreen.animate().x(targetX.toFloat() + margin / 2).setDuration(300).start()
    }

    override fun setupInsets() {
        drawBelowStatusBar {
            binding.flTop.setTopMarginFromInsets(it)
            binding.ivCloseMenu.setTopMarginFromInsets(it)
            binding.fcMain.updatePadding(
                left = it.left, top = 0, right = it.right, bottom = 0
            )
        }
    }

    override fun onDestroy() {
        stopLocationService()
        super.onDestroy()
    }

    private fun addLocationDetector() {
        viewModel.compositeDisposable.add(
            EventBus.subscribe<LocationTrackingService.Callback>().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { callback ->
                    when (callback.type) {
                        LocationTrackingService.Callback.Type.LOCATION_FOUND -> {
                            // Handle location found event
                        }

                        LocationTrackingService.Callback.Type.PERMISSION_REQUIRED -> {
                            runWithPermissions(
                                *PermissionsUtil.getForLocation(),
                                options = QuickPermissionsOptions(
                                    handleRationale = true,
                                    rationaleMessage = getString(R.string.request_location_permission_msg)
                                )
                            ) {
                                startLocationTrackingService()
                            }
                        }

                        LocationTrackingService.Callback.Type.GPS_REQUIRED -> {
                            // Handle GPS required event
                            callback.request?.let {
                                launcherGps.launch(it)
                            }
                        }

                        LocationTrackingService.Callback.Type.GPS_DENY -> {
                            // Handle GPS deny event
                        }

                        LocationTrackingService.Callback.Type.ERROR -> {
                            // Handle error event
                        }
                    }

                })
    }

    private val launcherGps =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            startLocationTrackingService()
        }

    fun startLocationTrackingService() {
        if (sharePref.isRiderOnline() && dataProvider.hasActiveOrder()) {
            PermissionsUtil.requestPostNotificationPermission(this) {
                ContextCompat.startForegroundService(
                    this, Intent(this, LocationTrackingService::class.java)
                )
            }
        } else {
            stopLocationService()
        }
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        stopService(intent)
    }

    /**
     * open main activity from any context with clear top
     */
    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }

    data class DrawerMenu(val icon: Int, var name: String, var selected: Boolean = false)

}
