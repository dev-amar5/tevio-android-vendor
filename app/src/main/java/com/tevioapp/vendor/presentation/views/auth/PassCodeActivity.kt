package com.tevioapp.vendor.presentation.views.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.AuthResponse
import com.tevioapp.vendor.data.PhoneDetails
import com.tevioapp.vendor.databinding.ActivityPasscodeBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.utility.CommonMethods.updateResendLabel
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.findParcelData
import com.tevioapp.vendor.utility.extensions.showKeyboard
import com.tevioapp.vendor.utility.otp.SmsBroadcastReceiver
import com.tevioapp.vendor.utility.util.PassCodeUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PassCodeActivity : BaseActivity<ActivityPasscodeBinding>() {
    private val resendTime = 60 * 1000L
    private var countDownTimer: CountDownTimer? = null
    private val viewModel: AuthViewModel by viewModels()
    private var phoneDetails: PhoneDetails? = null
    private lateinit var passCodeUtils: PassCodeUtils
    private var smsReceiver: SmsBroadcastReceiver? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_passcode
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        doInitialization()
        doRegisterObservers()
        startSmsRetriever()
        apiSendOtp()
    }

    private fun apiSendOtp() {
        phoneDetails?.let {
            viewModel.sendOtpApi(it)
        }
    }

    private fun apiVerifyOtp(otp: String) {
        phoneDetails?.let {
            viewModel.verifyOtpApi(it, otp)
        }
    }


    /**
     * register livedata observer here
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> {
                    finish()
                }

                R.id.btn_continue -> {
                    val otp = passCodeUtils.getValidOTP()
                    if (otp != null) {
                        apiVerifyOtp(otp)
                    } else {
                        showShortMessage(getString(R.string.otp_invalid))
                    }
                }

                R.id.tv_resend_label -> {
                    apiSendOtp()
                }
            }
        }
        viewModel.obrSendOtpApi.observe(this) {
            when (it.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    showShortMessage(it.message)
                    startTimer()
                    baseHandler.postDelayed({ setOtpInView("1234") }, 2000)
                }

                else -> {
                    hideLoading()
                    showShortMessage(it.message)
                    updateResendLabel(this@PassCodeActivity, binding.tvResendLabel, 0)
                }
            }
        }
        viewModel.obrVerifyOtpApi.observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    showShortMessage(resource.message)
                    resource.data?.let {
                        setResult(Activity.RESULT_OK, Intent().apply { putExtra("response", it) })
                        finish()
                    }
                }

                else -> {
                    hideLoading()
                    showLongMessage(resource.message)
                }
            }
        }
    }

    private fun setOtpInView(otp: String) {
        passCodeUtils.setOtpInView(otp)
        if (passCodeUtils.getValidOTP() != null) {
            binding.btnContinue.performClick()
        }
    }


    /**
     * init all views and variables here
     */
    private fun doInitialization() {
        passCodeUtils = PassCodeUtils(this)
        passCodeUtils.setPassCodeView(
            et1 = binding.et1, et2 = binding.et2, et3 = binding.et3, et4 = binding.et4
        )
        phoneDetails = intent.findParcelData("details", PhoneDetails::class.java)
        updateResendLabel(this@PassCodeActivity, binding.tvResendLabel, null)
    }


    /**
     * start Resend OTP timer
     */
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(resendTime, 1000) {
            override fun onTick(p0: Long) {
                updateResendLabel(this@PassCodeActivity, binding.tvResendLabel, p0)
            }

            override fun onFinish() {
                updateResendLabel(this@PassCodeActivity, binding.tvResendLabel, 0)
            }

        }
        countDownTimer?.start()
        binding.et1.requestFocus()
        showKeyboard(binding.et1)
    }


    private fun startSmsRetriever() {
        SmsRetriever.getClient(this).startSmsRetriever().addOnSuccessListener {
            smsReceiver = SmsBroadcastReceiver { otp ->
                otp?.let { setOtpInView(it) } ?: showShortMessage("OTP Timeout or failed")
            }
            val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            ContextCompat.registerReceiver(
                this, smsReceiver, filter, ContextCompat.RECEIVER_EXPORTED
            )
        }.addOnFailureListener {
            showShortMessage("Failed to start SMS Retriever")
        }
    }


    override fun onDestroy() {
        runCatching { unregisterReceiver(smsReceiver) }
        countDownTimer?.cancel()
        super.onDestroy()
    }

}

class PassCodeContract : ActivityResultContract<PhoneDetails, AuthResponse?>() {

    override fun createIntent(context: Context, input: PhoneDetails): Intent {
        return Intent(context, PassCodeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("details", input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AuthResponse? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.findParcelData("response", AuthResponse::class.java)
        }
        return null
    }
}
