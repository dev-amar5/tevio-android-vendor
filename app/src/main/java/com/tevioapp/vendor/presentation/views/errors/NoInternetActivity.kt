package com.tevioapp.vendor.presentation.views.errors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ActivityNoInternetBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoInternetActivity : BaseActivity<ActivityNoInternetBinding>() {
    private val viewModel: ErrorsViewModel by viewModels()


    /** Inflate the layout resource for the login activity**/
    override fun getLayoutResource(): Int {
        return R.layout.activity_no_internet
    }

    /** Bind the ViewModel to this activity**/
    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /**Initialize views and set observers on viewModel**/
    override fun onCreateView(savedInstanceState: Bundle?) {
        connection.observe(this) { status ->
            if (status.connected) {
                baseHandler.postDelayed({
                    finish()
                }, 2000)
            } else {
                baseHandler.removeCallbacksAndMessages(null)
            }
        }
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.btn_retry -> {
                    if (connection.isConnected()) {
                        finish()
                    } else {
                        showShortMessage(getString(R.string.please_check_your_internet_connection))
                    }
                }
            }
        }
    }

    override fun observeInternetChanges(): Boolean {
        return false
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, NoInternetActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }


}



