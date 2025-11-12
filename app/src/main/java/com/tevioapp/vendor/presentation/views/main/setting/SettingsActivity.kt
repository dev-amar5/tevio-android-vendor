package com.tevioapp.vendor.presentation.views.main.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.AppPreferences
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.databinding.ActivitySettingsBinding
import com.tevioapp.vendor.databinding.DialogChangeThemeBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.dialog.BaseAlertDialog
import com.tevioapp.vendor.presentation.common.base.dialog.BaseCustomDialog
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.clearUserAndRestart
import com.tevioapp.vendor.utility.security.BiometricAuthUtil
import com.tevioapp.vendor.utility.util.ThemeUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    private val viewModel: SettingsActivityVM by viewModels()
    private lateinit var appPreferences: AppPreferences
    private var dialogTheme: BaseCustomDialog<DialogChangeThemeBinding>? = null
    private var dialogLogout: BaseAlertDialog? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_settings
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        doInitialization()
        doRegisterObservers()
        setThemeView()
    }

    private fun setThemeView() {
        val savedTheme = ThemeUtil.getSavedTheme(this)
        when (savedTheme) {
            ThemeUtil.ThemeMode.LIGHT -> binding.tvMode.text = getString(R.string.label_off)
            ThemeUtil.ThemeMode.DARK -> binding.tvMode.text = getString(R.string.label_on)
            ThemeUtil.ThemeMode.SYSTEM_DEFAULT -> binding.tvMode.text =
                getString(R.string.system_default)
        }
    }

    private fun applyThemeAndRestart(mode: ThemeUtil.ThemeMode) {
        dialogTheme?.dismiss()
        ThemeUtil.applyTheme(this, mode)
        recreate()
    }

    /**
     * init all views and variables here
     */
    private fun doInitialization() {
        binding.header.apply {
            tvHeading.isVisible = true
            tvHeading.text = getString(R.string.settings)
            ivLogo.isVisible = false
            ivBack.isVisible = true
        }
        appPreferences = sharePref.getAppPreference()
        binding.vSafetyAlert.apply {
            label = getString(R.string.safety_alert_for_delivery)
            customSwitch.isChecked = appPreferences.dataSharing
            customSwitch.setOnCheckedChangeListener { _, isChecked ->
                appPreferences.dataSharing = isChecked
            }
        }
        binding.vBiometric.apply {
            if (BiometricAuthUtil.hasBiometricHardware(this@SettingsActivity)) {
                root.isVisible = true
                label = getString(R.string.biometrics_authentication)
                customSwitch.isChecked = sharePref.getBiometricEnabled()
                customSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val biometricAuthUtil =
                            BiometricAuthUtil(this@SettingsActivity, onSuccess = {
                                sharePref.setBiometricEnabled(true)
                            }, onFailure = {
                                customSwitch.isChecked = false
                                showLongMessage(it)
                            })
                        if (biometricAuthUtil.canAuthenticate()) {
                            biometricAuthUtil.authenticate(
                                "Authentication", "Use biometric or device PIN to login"
                            )
                        } else {
                            customSwitch.isChecked = false
                        }
                    } else {
                        sharePref.setBiometricEnabled(false)
                    }
                }
            } else {
                root.isVisible = false
            }
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

                R.id.tv_mode -> {
                    showThemeDialog()
                }

                R.id.tv_delete_account -> {
                    startActivity(DeleteAccountActivity.newInstance(this))
                }

                R.id.tv_logout -> {
                    apiLogout()
                }
            }
        }
    }

    private fun apiLogout() {
        dialogLogout = BaseAlertDialog(
            this,
            title = getString(R.string.log_out),
            message = getString(R.string.are_you_sure_want_to_logout),
            positive = getString(R.string.yes),
            negative = getString(R.string.cancel),
            cancelable = false,
            onNegativeButtonClick = { dialog ->
                dialog.dismiss()
            },
            onPositiveButtonClick = { dialog ->
                viewModel.apiLogout().observe(this) { resource ->
                    when (resource.status) {
                        Status.LOADING -> showLoading()
                        Status.SUCCESS -> {
                            hideLoading()
                            showShortMessage(resource.message)
                            dialog.dismiss()
                            viewModel.disconnectSocket()
                            clearUserAndRestart()
                        }

                        else -> {
                            hideLoading()
                            showShortMessage(resource.message)
                        }
                    }
                }
            }).show()
    }

    private fun showThemeDialog() {
        dialogTheme = BaseCustomDialog(
            mContext = this,
            layoutId = R.layout.dialog_change_theme,
            effect = Effect.BLUR,
            listener = object : BaseCustomDialog.Listener<DialogChangeThemeBinding>() {
                override fun onViewCreated(binding: DialogChangeThemeBinding) {
                    val savedTheme = ThemeUtil.getSavedTheme(this@SettingsActivity)
                    when (savedTheme) {
                        ThemeUtil.ThemeMode.LIGHT -> binding.rbOff.isChecked = true
                        ThemeUtil.ThemeMode.DARK -> binding.rbOn.isChecked = true
                        ThemeUtil.ThemeMode.SYSTEM_DEFAULT -> binding.rbSystemDefault.isChecked =
                            true
                    }
                    binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
                        when (checkedId) {
                            binding.rbSystemDefault.id -> applyThemeAndRestart(ThemeUtil.ThemeMode.SYSTEM_DEFAULT)
                            binding.rbOn.id -> applyThemeAndRestart(ThemeUtil.ThemeMode.DARK)
                            binding.rbOff.id -> applyThemeAndRestart(ThemeUtil.ThemeMode.LIGHT)
                        }
                    }
                }
            })
        dialogTheme?.show()
    }

    override fun onDestroy() {
        dialogTheme?.dismiss()
        dialogLogout?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
