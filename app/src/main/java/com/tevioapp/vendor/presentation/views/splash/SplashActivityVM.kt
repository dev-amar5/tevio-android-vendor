package com.tevioapp.vendor.presentation.views.splash

import com.tevioapp.vendor.data.local.SharedPref
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SplashActivityVM @Inject constructor(
    val sharedPref: SharedPref
) : BaseViewModel() {

}

