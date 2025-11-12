package com.tevioapp.vendor.presentation.views.splash

import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.firebase.FirebaseApp
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ActivitySplashBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.auth.login.LoginActivity
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.presentation.views.main.MainActivity
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.extensions.drawBelowStatusBar
import com.tevioapp.vendor.utility.location.LocationDetector
import com.tevioapp.vendor.utility.permissions.PermissionsUtil
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import com.tevioapp.vendor.utility.popups.PopupsUtils.showLocationPermissionPopUp
import com.tevioapp.vendor.utility.security.BiometricAuthUtil
import com.tevioapp.vendor.utility.socket.SocketClient
import com.tevioapp.vendor.utility.workers.CountryListWorker
import com.tevioapp.vendor.utility.workers.GetDocumentsWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val viewModel: SplashActivityVM by viewModels()

    @Inject
    lateinit var socketClient: SocketClient
    private var mediaPlayer: MediaPlayer? = null
    private var isVideoPlayed: Boolean = false
    private val locationDetector by lazy { LocationDetector(this) }
    override fun getLayoutResource(): Int {
        return R.layout.activity_splash
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        CountryListWorker.createJob(this)
        doInitialization()
        addLocationDetector()
        doRegisterObservers()
    }


    override fun setupInsets() {
        drawBelowStatusBar {
            binding.root.apply {
                val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin = it.bottom
                this.layoutParams = layoutParams
            }
        }
    }

    /**
     * register all live data observer from view model here//
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(this) {}
    }


    private fun doInitialization() {
        binding.videoView.setZOrderMediaOverlay(true)
        val videoPath = "android.resource://${packageName}/${R.raw.splash_video_client}"
        val uri = videoPath.toUri()
        val surfaceHolder = binding.videoView.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@SplashActivity, uri)
                    setDisplay(holder)
                    val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build()
                    setAudioAttributes(attrs)
                    setVolume(0f, 0f)
                    setOnPreparedListener {
                        try {
                            val videoWidth = it.videoWidth
                            val videoHeight = it.videoHeight
                            val surfaceView = binding.videoView
                            // Get parent or screen dimensions
                            val parentWidth = surfaceView.width
                            val parentHeight = surfaceView.height
                            val videoProportion = videoWidth.toFloat() / videoHeight
                            val screenProportion = parentWidth.toFloat() / parentHeight
                            val layoutParams = surfaceView.layoutParams
                            if (videoProportion > screenProportion) {
                                // Video is wider than screen
                                layoutParams.width = parentWidth
                                layoutParams.height = (parentWidth / videoProportion).toInt()
                            } else {
                                // Video is taller than screen
                                layoutParams.width = (parentHeight * videoProportion).toInt()
                                layoutParams.height = parentHeight
                            }
                            surfaceView.layoutParams = layoutParams
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        it.start()
                    }
                    setOnCompletionListener {
                        releaseMedia()
                    }
                    prepareAsync()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
                // No usage
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                releaseMedia()
            }
        })
        baseHandler.postDelayed({
            isVideoPlayed = true
            getLatestLocation()
        }, 5000)

    }


    private fun releaseMedia() {
        mediaPlayer?.release()
        mediaPlayer = null
    }


    private fun addLocationDetector() {
        locationDetector.withListener(object : LocationDetector.Listener {
            override fun onError(message: String) {
                showLongMessage(message)
            }

            override fun onDetectorStatusChanged(loading: Boolean) {
                binding.pbOne.isVisible = loading
            }

            override fun onGpsPermissionDeny() {
                showPermissionView()
            }

            override fun onLocationFound(location: Location) {
                locationDetector.stopLocationDetector()
                getScreenIntent {
                    startActivity(it)
                    finish()
                }
            }

            override fun onPermissionPending() {
                showPermissionView()
            }

            override fun onGpsPermissionPending(request: IntentSenderRequest) {
                launcherGps.launch(request)
            }

        })
    }

    private fun showPermissionView() {
        showLocationPermissionPopUp(this@SplashActivity) {
            runWithPermissions(
                *PermissionsUtil.getForLocation(), options = QuickPermissionsOptions(
                    handleRationale = true,
                    rationaleMessage = getString(R.string.request_location_permission_msg)
                )
            ) {
                getLatestLocation()
            }
        }
    }


    private fun getScreenIntent(onIntent: (Intent) -> Unit) {
        if (sharePref.isLoggedIn()) {
            when (sharePref.getRegistrationStatus()) {
                Enums.REGISTRATION_STATUS_PENDING -> {
                    sharePref.clearUser()
                    onIntent(LoginActivity.newInstance(this))
                }

                Enums.REGISTRATION_STATUS_IN_PROCESS -> {
                    onIntent(RegistrationStepsActivity.newInstance(this))
                }

                else -> {
                    GetDocumentsWorker.createJob(this)
                    if (sharePref.getBiometricEnabled()) {
                        val biometricAuthUtil = BiometricAuthUtil(activity = this, onSuccess = {
                            onIntent(MainActivity.newInstance(this))
                        }, onFailure = { errorMsg ->
                            showLongMessage(errorMsg)
                        })
                        biometricAuthUtil.authenticate()
                    } else {
                        onIntent(MainActivity.newInstance(this))
                    }
                }
            }
        } else {
            onIntent(LoginActivity.newInstance(this))
        }
    }

    private val launcherGps =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            locationDetector.onActivityResult(activityResult.resultCode)
        }

    override fun onDestroy() {
        locationDetector.stopLocationDetector()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        getLatestLocation()
    }

    private fun getLatestLocation() {
        if (isVideoPlayed) {
            locationDetector.startLocationDetector(
                detectorType = LocationDetector.DETECTOR_ONE_SHOT,
                locationAccuracy = AppConstants.MIN_LOCATION_ACCURACY
            )
        }
    }


}
