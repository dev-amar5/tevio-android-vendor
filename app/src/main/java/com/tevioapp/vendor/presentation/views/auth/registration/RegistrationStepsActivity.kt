package com.tevioapp.vendor.presentation.views.auth.registration

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ActivityRegistrationStepsBinding
import com.tevioapp.vendor.databinding.ItemRegistrationStepsBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.auth.AuthViewModel
//import com.tevioapp.vendor.presentation.views.auth.registration.kit.JoiningBenefitsActivity
//import com.tevioapp.vendor.presentation.views.auth.registration.work.DeliveryTypeOptionActivity
import com.tevioapp.vendor.presentation.views.main.MainActivity
import com.tevioapp.vendor.utility.event.helper.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationStepsActivity : BaseActivity<ActivityRegistrationStepsBinding>() {
    private val viewModel: AuthViewModel by viewModels()
    private var stepsData: Steps? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_registration_steps
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        initViews()
        doRegisterObservers()
    }

    private fun initViews() {
        binding.header.apply {
            tvHeading.isVisible = false
            ivLogo.isVisible = true
        }
        binding.viewTitle.label = getString(R.string.become_a_delivery_partner)
    }

    private fun doRegisterObservers() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> {
                    sharePref.clearUser()
                    finish()
                }
            }
        }
        viewModel.apiRegistrationStatus().observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (stepsData == null) startShimmer()
                }

                Status.SUCCESS -> {
                    stopShimmer()
                    stepsData = resource.data
                    setDataInView()
                }

                else -> {
                    stopShimmer()
                    showShortMessage(resource.message)
                }
            }
        }
    }

    private fun setDataInView() {
        binding.llItems.removeAllViews()
        val data = stepsData ?: return
        if (data.isAllStepsCompleted()) {
            startActivity(MainActivity.newInstance(this))
            finishAffinity()
            return
        }
        setSectionRow(
            typeIcon = R.drawable.step_profile,
            stepLabel = getString(R.string.step1),
            title = getString(R.string.profile),
            description = getString(R.string.upload_id_and_bank_details),
            isCompleted = data.step1,
            enabled = true
        ) {
           // startActivity(RegistrationActivity.newInstance(this))
        }
        setSectionRow(
            typeIcon = R.drawable.step_work_setting,
            stepLabel = getString(R.string.step2),
            title = getString(R.string.work_settings),
            description = getString(R.string.tell_us_more_about),
            isCompleted = data.step2,
            enabled = data.step1,
        ) {
           // startActivity(DeliveryTypeOptionActivity.newInstance(this))
        }
        setSectionRow(
            typeIcon = R.drawable.step_delivery_kit,
            stepLabel = getString(R.string.step3),
            title = getString(R.string.order_delivery_kit),
            description = getString(R.string.enjoy_your_benefits),
            isCompleted = data.step3,
            enabled = data.step2,
        ) {
           // startActivity(JoiningBenefitsActivity.newInstance(this))
        }
    }

    private fun setSectionRow(
        @DrawableRes typeIcon: Int,
        stepLabel: String,
        title: String,
        description: String,
        isCompleted: Boolean,
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        DataBindingUtil.inflate<ItemRegistrationStepsBinding>(
            layoutInflater, R.layout.item_registration_steps, binding.llItems, true
        ).apply {
            ivType.setImageResource(typeIcon)
            viewSteps.label = stepLabel
            viewSteps.tvLabel.setTypeface(null, Typeface.BOLD)
            tvType.text = title
            tvDesc.text = description
            overlayView.isGone = enabled
            tvStartNow.isVisible = isCompleted.not()
            tvStatusResult.isVisible = isCompleted
            val labelColor = if (isCompleted) R.color.parrot_green else R.color.orange
            viewSteps.tvLabel.setTextColor(ContextCompat.getColor(baseContext, labelColor))
            if (enabled && isCompleted.not()) {
                root.setOnClickListener { onClick() }
            }
        }
    }

    data class Steps(
        @SerializedName("step1_completed") val step1: Boolean,
        @SerializedName("step2_completed") val step2: Boolean,
        @SerializedName("step3_completed") val step3: Boolean
    ) {
        fun isAllStepsCompleted() = step1 && step2 && step3
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, RegistrationStepsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }

}