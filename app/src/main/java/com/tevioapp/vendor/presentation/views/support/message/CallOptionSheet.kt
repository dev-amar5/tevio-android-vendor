package com.tevioapp.vendor.presentation.views.support.message

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.ThreadDetail
import com.tevioapp.vendor.data.enums.Effect
import com.tevioapp.vendor.databinding.SheetCallOptionBinding
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.sheet.BaseBottomSheet
import com.tevioapp.vendor.presentation.views.support.audio.ActiveAudioCallActivity
import com.tevioapp.vendor.utility.CommonMethods
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.util.DataProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallOptionSheet(private val threadId: String) : BaseBottomSheet<SheetCallOptionBinding>() {
    @Inject
    lateinit var dataProvider: DataProvider
    private val viewModel: ChatActivityVM by viewModels()
    private var threadDetail: ThreadDetail? = null
    override fun getLayoutResource(): Int {
        return R.layout.sheet_call_option
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onSheetCreated(binding: SheetCallOptionBinding) {
        viewModel.onClick.observe(viewLifecycleOwner) { view ->
            when (view.id) {
                R.id.iv_cross -> {
                    dismissAllowingStateLoss()
                }

                R.id.btn_call_carrier -> {
                    threadDetail?.contactInfo?.getPhoneDetail()?.toString()?.let {
                        CommonMethods.openDialerApp(requireContext(), it)
                    }
                    dismissAllowingStateLoss()
                }

                R.id.btn_call_in_app -> {
                    viewModel.apiInitiateCall(threadId).observe(this) { resource ->
                        when (resource.status) {
                            Status.LOADING -> {
                                binding.btnCallInApp.setLoading(true)
                            }

                            Status.SUCCESS -> {
                                binding.btnCallInApp.setLoading(false)
                                dismissAllowingStateLoss()
                                resource.data?.let { input ->
                                    input.role = Enums.AUDIO_ROLE_CALLER
                                    startActivity(
                                        ActiveAudioCallActivity.newInstance(
                                            requireContext(), input
                                        )
                                    )
                                }
                            }

                            else -> {
                                binding.btnCallInApp.setLoading(false)
                                baseActivity?.showShortMessage(resource.message)
                            }
                        }
                    }
                }

            }
        }

        viewModel.apiThreadDetail(threadId).observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    loadingUtils.startShimmer()
                }

                Status.SUCCESS -> {
                    loadingUtils.stopShimmer()
                    threadDetail = resource.data
                    setDataInView()
                }

                else -> {
                    loadingUtils.stopShimmer()
                    baseActivity?.showShortMessage(resource.message)
                    dismissAllowingStateLoss()
                }
            }

        }


    }

    private fun setDataInView() = with(getViewBinding()) {
        image = threadDetail?.image
        tvName.text = threadDetail?.name
        tvDesc.text = dataProvider.getRoles().find { it.first == threadDetail?.role }?.second

        val phoneDetails = threadDetail?.contactInfo?.getPhoneDetail()
        if (phoneDetails != null) {
            btnCallCarrier.isVisible = true
            btnCallCarrier.text = phoneDetails.toString()
        } else {
            btnCallCarrier.isVisible = false
        }
    }

    override fun getEffect(): Effect {
        return Effect.DIM
    }
}
