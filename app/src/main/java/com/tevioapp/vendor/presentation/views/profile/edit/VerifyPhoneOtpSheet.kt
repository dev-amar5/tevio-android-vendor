package com.tevioapp.vendor.presentation.views.profile.edit

import android.os.CountDownTimer
import androidx.fragment.app.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.PhoneDetails
import com.tevioapp.vendor.databinding.SheetVerifyPhoneOtpBinding
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.sheet.BaseBottomSheet
import com.tevioapp.vendor.presentation.views.profile.ProfileViewModel
import com.tevioapp.vendor.utility.CommonMethods.updateResendLabel
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.showKeyboard
import com.tevioapp.vendor.utility.extensions.toJsonObject
import com.tevioapp.vendor.utility.extensions.withDelay
import com.tevioapp.vendor.utility.util.PassCodeUtils
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class VerifyPhoneOtpSheet(private val phoneDetails: PhoneDetails) :
    BaseBottomSheet<SheetVerifyPhoneOtpBinding>() {
    private val viewModel: ProfileViewModel by viewModels()
    private val resendTime = 60 * 1000L
    private var countDownTimer: CountDownTimer? = null
    private lateinit var passCodeUtils: PassCodeUtils
    private var token: String? = null
    override fun getLayoutResource(): Int {
        return R.layout.sheet_verify_phone_otp
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onSheetCreated(binding: SheetVerifyPhoneOtpBinding) {
        passCodeUtils = PassCodeUtils(requireActivity())
        passCodeUtils.setPassCodeView(
            et1 = binding.et1, et2 = binding.et2, et3 = binding.et3, et4 = binding.et4
        )
        updateResendLabel(requireContext(), binding.tvResendLabel, null)
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it.id) {
                R.id.iv_cross -> {
                    dismissAllowingStateLoss()
                }

                R.id.btn_submit -> {
                    if (token.isNullOrEmpty()) {
                        apiSendOtp()
                    } else {
                        apiVerifyOtp()
                    }
                }

                R.id.tv_resend_label -> {
                    apiSendOtp()
                }
            }
        }
        apiSendOtp()
    }

    private fun apiSendOtp() {
        viewModel.apiValidatePhone(phoneDetails.toJsonObject()).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> loadingUtils.showLoading()
                Status.SUCCESS -> {
                    loadingUtils.hideLoading()
                    token = resource.data
                    updateOTPView()
                    baseActivity?.withDelay(2000) {
                        setOtpInView("1234")
                    }
                }

                else -> {
                    loadingUtils.hideLoading()
                    baseActivity?.showShortMessage(resource.message)
                    token = null
                    updateOTPView()
                }
            }
        }
    }

    private fun setOtpInView(otp: String) = with(getViewBinding()) {
        passCodeUtils.setOtpInView(otp)
        if (passCodeUtils.getValidOTP() != null) {
            btnSubmit.performClick()
        }
    }

    private fun updateOTPView() = with(getViewBinding()) {
        if (token.isNullOrEmpty()) {
            stopTimer()
            updateResendLabel(requireContext(), tvResendLabel, null)
            passCodeUtils.setOtpInView(null)
        } else {
            stopTimer()
            countDownTimer = object : CountDownTimer(resendTime, 1000) {
                override fun onTick(p0: Long) {
                    updateResendLabel(requireContext(), tvResendLabel, p0)
                }

                override fun onFinish() {
                    updateResendLabel(requireContext(), tvResendLabel, 0)
                }
            }
            countDownTimer?.start()
            et1.requestFocus()
            baseActivity?.showKeyboard(et1)
        }
    }

    private fun apiVerifyOtp() {
        val otp = passCodeUtils.getValidOTP()
        if (otp == null) {
            baseActivity?.showShortMessage(getString(R.string.otp_invalid))
            return
        }
        val tokenPhone = token.orEmpty()
        if (tokenPhone.isEmpty()) {
            updateOTPView()
            return
        }
        viewModel.apiChangePhone(JSONObject().apply {
            put("token", token)
            put("otp", otp)
        }).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> loadingUtils.showLoading()
                Status.SUCCESS -> {
                    loadingUtils.hideLoading()
                    baseActivity?.showShortMessage(resource.message)
                    activity?.finish()
                }

                else -> {
                    loadingUtils.hideLoading()
                    baseActivity?.showLongMessage(resource.message)
                }
            }
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    override fun onStart() {
        super.onStart()
        extendToHeightPercent()
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }
}
