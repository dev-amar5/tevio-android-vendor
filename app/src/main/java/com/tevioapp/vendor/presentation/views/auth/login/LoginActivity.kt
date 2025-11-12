package com.tevioapp.vendor.presentation.views.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.PhoneDetails
import com.tevioapp.vendor.databinding.ActivityLoginBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.adapter.ViewPager2Adapter
import com.tevioapp.vendor.presentation.common.base.dialog.BaseAlertDialog
import com.tevioapp.vendor.presentation.views.auth.AuthViewModel
import com.tevioapp.vendor.presentation.views.auth.PassCodeContract
import com.tevioapp.vendor.presentation.views.auth.login.pager.FirstViewFragment
import com.tevioapp.vendor.presentation.views.auth.phone.PhoneNumberFragment
import com.tevioapp.vendor.presentation.views.auth.registration.RegistrationStepsActivity
import com.tevioapp.vendor.presentation.views.main.MainActivity
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.extensions.drawBelowStatusBar
import com.tevioapp.vendor.utility.extensions.enableAutoScroll
import com.tevioapp.vendor.utility.order.StatusHelper
import com.tevioapp.vendor.utility.socket.SocketClient
import com.tevioapp.vendor.utility.workers.GetDocumentsWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    @Inject
    lateinit var socketClient: SocketClient
    private lateinit var phoneNumberFragment: PhoneNumberFragment
    private var phoneDetails: PhoneDetails? = null
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var adapter: ViewPager2Adapter
    private var dialogAlert: BaseAlertDialog? = null

    /** Inflate the layout resource for the login activity**/
    override fun getLayoutResource(): Int {
        return R.layout.activity_login
    }

    /** Bind the ViewModel to this activity**/
    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /**Initialize views and set observers on viewModel**/
    override fun onCreateView(savedInstanceState: Bundle?) {
        socketClient.offAll()
        socketClient.disconnect().subscribe()
        addPhoneNumberView()
        setUpAdapter()
        setObserver()
    }

    /**Add the phone number fragment to the layout**/
    private fun addPhoneNumberView() {
        phoneNumberFragment = PhoneNumberFragment.addPhoneNumberView(
            fragmentManager = supportFragmentManager,
            conditionerId = R.id.v_phone,
            inputData = PhoneNumberFragment.InputData(
                header = getString(R.string.phone_number),
                icon = R.drawable.ic_phone_call_stroke,
                hint = getString(R.string.phone_number)
            )
        )
    }

    private fun setUpAdapter() {
        adapter = ViewPager2Adapter(supportFragmentManager, lifecycle)
        adapter.addFragment(FirstViewFragment.newInstance(), "")
        adapter.addFragment(FirstViewFragment.newInstance(), "")
        adapter.addFragment(FirstViewFragment.newInstance(), "")
        adapter.addFragment(FirstViewFragment.newInstance(), "")

        binding.viewPager.adapter = adapter
        binding.tabGroup.setupWithViewPager(binding.viewPager, adapter)
        binding.viewPager.enableAutoScroll(this, enable = true)

    }

    /**Adjust the layout based on the system's insets**/
    override fun setupInsets() {
        drawBelowStatusBar {
            binding.header.root.apply {
                val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = it.top
                this.layoutParams = layoutParams
            }

            binding.clPhoneNo.apply {
                val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin = it.bottom
                this.layoutParams = layoutParams
            }
        }
    }


    /**Set observers for various events like button clicks and social login responses**/
    private fun setObserver() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> finish()

                R.id.btn_get_code -> {
                    phoneDetails = phoneNumberFragment.getValidatedPhoneDetails()
                    phoneDetails?.let {
                        launcherPassCode.launch(it)
                    }
                }
            }

        }

    }

    private fun validateAccountStatus(accountStatus: String?) {
        when (accountStatus) {
            Enums.REGISTRATION_STATUS_SUBMITTED, Enums.REGISTRATION_STATUS_APPROVED -> {
                GetDocumentsWorker.createJob(this)
                startActivity(MainActivity.newInstance(this))
                finishAffinity()
            }

            Enums.REGISTRATION_STATUS_PENDING, Enums.REGISTRATION_STATUS_IN_PROCESS -> {
                startActivity(RegistrationStepsActivity.newInstance(this))
                finishAffinity()
            }

            else -> {
                sharePref.clearPref()
                val statusUI = StatusHelper.getRegistrationStatusUI(accountStatus)
                dialogAlert = BaseAlertDialog(
                    this,
                    title = statusUI?.title.orEmpty(),
                    message = statusUI?.description.orEmpty(),
                    icon = statusUI?.iconRes ?: R.drawable.ic_cross_hexa,
                    positive = getString(R.string.got_it),
                    cancelable = false,
                    onPositiveButtonClick = { dialog ->
                        dialog.dismiss()
                        finishAffinity()
                    }).show()
            }
        }
    }

    /**Register a launcher for the passcode activity result**/
    private val launcherPassCode = registerForActivityResult(PassCodeContract()) { response ->
        response?.let { authResponse ->
            sharePref.saveToken(authResponse.token)
            sharePref.saveRegistrationStatus(authResponse.registrationStatus)
            validateAccountStatus(authResponse.registrationStatus)
        }
    }


    override fun onDestroy() {
        binding.viewPager.enableAutoScroll(this, enable = false)
        super.onDestroy()
    }

    /**
     * Create an Intent to open LoginActivity from any context with a flag for showing back button
     */
    companion object {
        fun newInstance(context: Context, showBack: Boolean = false): Intent {
            return Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("showBack", showBack)
            }
        }
    }


}



