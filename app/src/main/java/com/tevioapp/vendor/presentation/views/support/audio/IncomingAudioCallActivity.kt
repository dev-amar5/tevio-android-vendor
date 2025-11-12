package com.tevioapp.vendor.presentation.views.support.audio

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.AudioCallData
import com.tevioapp.vendor.databinding.ActivityIncomingAudioCallBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.notification.NotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IncomingAudioCallActivity : BaseActivity<ActivityIncomingAudioCallBinding>() {
    @Inject
    lateinit var notificationProvider: NotificationProvider
    private val viewModel: IncomingAudioCallActivityVM by viewModels()
    private var ringtone: Ringtone? = null
    private lateinit var inputData: AudioCallData

    override fun getLayoutResource(): Int = R.layout.activity_incoming_audio_call

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(savedInstanceState: Bundle?) {
        initialization()
        doRegisterObservers()
        apiThreadDetail()
        playRingTone()
        withDelay(RING_TIME_OUT) {
            sendCallStatus(Enums.CALL_NOT_ANSWERED) {
                finish()
            }
        }
        sendCallStatus(Enums.CALL_VISIBLE)
    }

    private fun initialization() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        inputData = AudioCallData(
            role = Enums.AUDIO_ROLE_RECEIVER,
            appId = intent.getStringExtra("app_id").orEmpty(),
            threadId = intent.getStringExtra("thread_id").orEmpty(),
            token = intent.getStringExtra("token").orEmpty(),
            uId = intent.getStringExtra("uid")?.toIntOrNull() ?: 0
        )
        binding.tvStatus.text = buildString {
            append("Incoming Call")
            append(getString(R.string.trailing_dots))
        }

        intent.getIntExtra("notification_id", -1).takeIf { it != -1 }?.let { id ->
            notificationProvider.removeNotification(intent.getIntExtra("notification_id", id))
        }
    }

    private fun playRingTone() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            isLooping = true
            play()
        }
    }

    private fun doRegisterObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_accept -> {
                    stopRinger()
                    startActivity(
                        ActiveAudioCallActivity.newInstance(this, inputData)
                    )
                    finish()
                }

                R.id.iv_decline -> {
                    stopRinger()
                    sendCallStatus(Enums.CALL_DECLINED) {
                        finish()
                    }
                }
            }
        }
        viewModel.obrCallStatus.observe(this) { resource ->
            if (resource.status == Status.SUCCESS && resource.data == Enums.CALL_ENDED) {
                finish()
            }
        }
    }

    private fun apiThreadDetail() {
        viewModel.apiThreadDetail(inputData.threadId).observe(this) { resource ->
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

    private fun sendCallStatus(status: String, onSuccess: (() -> Unit)? = null) {
        viewModel.sendCallStatus(inputData.threadId, status).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    onSuccess?.invoke()
                }

                else -> {
                    showShortMessage(resource.message)
                }
            }
        }
    }

    override fun onDestroy() {
        stopRinger()
        super.onDestroy()
    }

    private fun stopRinger() {
        ringtone?.stop()
        ringtone = null
    }

    companion object {
        const val RING_TIME_OUT = 15000L
    }
}
