package com.tevioapp.vendor.presentation.views.auth.login.pager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.FragmentFirstViewBinding
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.auth.AuthViewModel
import com.tevioapp.vendor.presentation.views.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FirstViewFragment : BaseFragment<FragmentFirstViewBinding>() {
    private lateinit var activityM: LoginActivity
    private val viewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(view: View, saveInstanceState: Bundle?) {
        addInitView()
    }

    private fun addInitView() {
        activityM = activity as LoginActivity
    }

    override fun getLayoutResource(): Int = R.layout.fragment_first_view

    override fun getViewModel(): BaseViewModel = viewModel

    companion object {
        fun newInstance(): FirstViewFragment {
            val fragment = FirstViewFragment()
            return fragment
        }
    }
}