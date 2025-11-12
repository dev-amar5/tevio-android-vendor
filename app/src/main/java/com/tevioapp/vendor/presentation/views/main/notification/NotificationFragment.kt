package com.tevioapp.vendor.presentation.views.main.notification


import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.FragmentNotificationBinding
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {
    private val viewModel: NotificationFragmentVM by viewModels()
    override fun onCreateView(view: View, saveInstanceState: Bundle?) {
        doRegisterObservers()
    }

    /**
     * register livedata observer and click listener
     */
    private fun doRegisterObservers() {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}