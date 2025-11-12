package com.tevioapp.vendor.presentation.views.support.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.databinding.ActivityActiveAudioCallBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.findParcelData
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.permissions.PermissionsUtil
import com.tevioapp.vendor.utility.permissions.QuickPermissionsOptions
import com.tevioapp.vendor.utility.permissions.runWithPermissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActiveAudioCallActivity : BaseActivity<ActivityActiveAudioCallBinding>() {
    private val viewModel: ActiveAudioCallActivityVM by viewModels()
    private val viewModel2: IncomingAudioCallActivityVM by viewModels()
    private lateinit var inputData: AudioCallData
    private var countDownTimer: CountDownTimer? = null
    override fun getLayoutResource(): Int = R.layout.activity_active_audio_call

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(savedInstanceState: Bundle?) {
        inputData = intent.findParcelData("input", AudioCallData::class.java)!!
        registerObservers()
        requestPermissionsAndInit()
        apiThreadDetail()
    }

    private fun registerObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_end_call -> {
                    disconnectCall(getString(R.string.call_ended))
                }
            }
        }
        binding.stvMute.setOnToggleListener { isMute ->
            viewModel.muteLocalAudio(isMute)
        }
        binding.stvSpeaker.setOnToggleListener {
            viewModel.toggleSpeaker()
        }

        viewModel.obrJoinChannel.observe(this) {
            binding.stvMute.isChecked = false
            binding.stvSpeaker.isChecked = false
            if (binding.tvStatus.text.isEmpty()) {
                if (inputData.role == Enums.AUDIO_ROLE_CALLER) {
                    binding.tvStatus.text = buildString {
                        append(getString(R.string.calling))
                        append(getString(R.string.trailing_dots))
                    }
                } else {
                    binding.tvStatus.text = buildString {
                        append(getString(R.string.connecting))
                        append(getString(R.string.trailing_dots))
                    }
                }
            }
        }

        viewModel.obrUserLeft.observe(this) {
            disconnectCall(getString(R.string.call_ended))
        }

        viewModel.obrUserJoin.observe(this) {
            stopTimer()
            showTimer()
        }
        viewModel.obrUserMute.observe(this) {
            binding.ivReceiverMute.isVisible = it
        }

        viewModel2.obrCallStatus.observe(this) { resource ->
            if (resource.status == Status.SUCCESS) when (resource.data) {
                Enums.CALL_ENDED -> disconnectCall(getString(R.string.call_ended))
                Enums.CALL_DECLINED -> disconnectCall(getString(R.string.call_declined))
                Enums.CALL_NOT_ANSWERED -> disconnectCall(getString(R.string.not_answered))
                Enums.CALL_VISIBLE -> binding.tvStatus.text = buildString {
                    append(getString(R.string.ringing))
                    append(getString(R.string.trailing_dots))
                }
            }
        }
    }

    private fun disconnectCall(message: String) {
        viewModel2.sendCallStatus(inputData.threadId, Enums.CALL_ENDED).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    stopTimer()
                    binding.tvStatus.text = message
                    viewModel.leaveChannel()
                    withDelay(500) {
                        finish()
                    }
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }
        }
    }

    private fun requestPermissionsAndInit() {
        runWithPermissions(
            *PermissionsUtil.getForAudioCalling(), options = QuickPermissionsOptions(
                handleRationale = false,
                rationaleMessage = getString(R.string.request_mic_permission_msg)
            )
        ) {
            viewModel.setAgoraAppId(inputData.appId)
            viewModel.joinChannel(inputData)
        }
    }

    private fun apiThreadDetail() {
        viewModel2.apiThreadDetail(inputData.threadId).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    binding.threadDetail = resource.data?.also { detail ->
                        binding.tvRole.text =
                            dataProvider.getRoles().find { it.first == detail.role }?.second
                    }
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }

        }
    }

    private fun showTimer() {
        val maxMillis = 1000L * 3600 // 1 hour in milliseconds
        countDownTimer = object : CountDownTimer(maxMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedSeconds = (maxMillis - millisUntilFinished) / 1000
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.tvStatus.text = buildString {
                    append(minutes)
                    append(":")
                    append(seconds.toString().padStart(2, '0'))
                }
            }

            override fun onFinish() {
                // Timer finished after 1 hour
                binding.tvStatus.text = ""
            }
        }
        countDownTimer?.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    override fun onDestroy() {
        stopTimer()
        viewModel.leaveChannel()
        super.onDestroy()
    }

    companion object {
        fun newInstance(
            context: Context, audioCallData: AudioCallData
        ): Intent {
            return Intent(context, ActiveAudioCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("input", audioCallData)
            }
        }
    }
}

