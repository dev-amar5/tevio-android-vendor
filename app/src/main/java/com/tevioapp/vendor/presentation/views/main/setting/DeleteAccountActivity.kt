package com.tevioapp.vendor.presentation.views.main.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ActivityDeleteAccountBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.dialog.BaseAlertDialog
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.clearUserAndRestart
import com.tevioapp.vendor.utility.extensions.withDelay
import dagger.hilt.android.AndroidEntryPoint
import java.net.HttpURLConnection

@AndroidEntryPoint
class DeleteAccountActivity : BaseActivity<ActivityDeleteAccountBinding>() {
    private val viewModel: SettingsActivityVM by viewModels()
    private var dialogAlert: BaseAlertDialog? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_delete_account
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        doInitialization()
        doRegisterObservers()
    }

    /**
     * init all views and variables here
     */
    private fun doInitialization() {
        binding.header.apply {
            tvHeading.isVisible = true
            tvHeading.text = getString(R.string.delete_account)
            ivLogo.isVisible = false
            ivBack.isVisible = true
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

                R.id.btn_submit -> {
                    apiDeleteAccount()
                }
            }
        }
    }

    private fun apiDeleteAccount() {
        viewModel.apiDeleteAccount().observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    showShortMessage(resource.message)
                    withDelay(500) {
                        viewModel.disconnectSocket()
                        clearUserAndRestart()
                    }
                }

                else -> {
                    hideLoading()
                    if (resource.errorCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        dialogAlert = BaseAlertDialog(
                            this,
                            title = "Unauthorized Deletion",
                            message = resource.message,
                            icon = R.drawable.ic_cross_hexa,
                            positive = getString(R.string.got_it),
                            cancelable = true,
                            onPositiveButtonClick = { dialog ->
                                dialog.dismiss()

                            }).show()
                    } else {
                        showShortMessage(resource.message)
                    }
                }
            }
        }

    }


    override fun onDestroy() {
        dialogAlert?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, DeleteAccountActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
