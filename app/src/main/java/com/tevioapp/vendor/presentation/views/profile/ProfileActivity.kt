package com.tevioapp.vendor.presentation.views.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.DocumentResponse
import com.tevioapp.vendor.data.PayoutResponse
import com.tevioapp.vendor.data.common.FileInfo
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.databinding.ActivityProfileBinding
import com.tevioapp.vendor.databinding.ViewInfoIconTextBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
//import com.tevioapp.vendor.presentation.views.camera.CameraActivity
import com.tevioapp.vendor.presentation.views.profile.edit.EditPersonalInfoActivity
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.maskLastChars
import com.tevioapp.vendor.utility.extensions.setMultiSpan
import com.tevioapp.vendor.utility.util.DateTimeUtils.utcToLocalString
import com.tevioapp.vendor.utility.workers.UpdateProfileImageWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {
    private val viewModel: ProfileViewModel by viewModels()
    private var profile: Profile? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        setObserver()
    }

    private fun apiCalls() {
        viewModel.apiPersonalInfo().observe(this@ProfileActivity) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (profile == null) startShimmer()
                }

                Status.SUCCESS -> {
                    profile = resource.data
                    setProfileView()
                    setAccountInfoView()
                    setPersonalInfoView()
                    stopShimmer()
                }

                else -> {
                    showShortMessage(resource.message)
                    stopShimmer()
                }
            }
        }
        viewModel.apiPayoutDetail().observe(this@ProfileActivity) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    setPayoutInfoView(resource.data)
                }

                else -> {
                    setPayoutInfoView(null)
                    showShortMessage(resource.message)
                }
            }
        }

        viewModel.apiGetDocumentsInfo().observe(this@ProfileActivity) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // No loader
                }

                Status.SUCCESS -> {
                    setVehicleInfoView(resource.data)
                }

                else -> {
                    setVehicleInfoView(null)
                    showShortMessage(resource.message)
                }
            }
        }
    }

    private fun setProfileView() {
        binding.profileImage = sharePref.getProfileImage()
        binding.tvName.text = profile?.getFullName()
    }

    private fun setAccountInfoView() = with(binding.vAccountInfo) {
        profile?.let {
            cvEdit.setOnClickListener {
                //startActivity(EditAccountInfoActivity.newInstance(this@ProfileActivity))
            }
            vTop.label = getString(R.string.account_info)
            container.removeAllViews()
            container.setIconTextRowView(
                R.drawable.ic_phone_call_stroke, "Phone Number", it.getPhoneDetail()?.toString()
            )
            container.setIconTextRowView(
                R.drawable.ic_calender,
                "Joining Date",
                it.createdAt.utcToLocalString(AppConstants.FORMAT_DATE_SMALL_WITH_YEAR)
            )
            root.isVisible = true
        } ?: run {
            root.isVisible = false
        }
    }

    private fun setPersonalInfoView() = with(binding.vPersonalInfo) {
        profile?.let {
            cvEdit.setOnClickListener {
                startActivity(EditPersonalInfoActivity.newInstance(this@ProfileActivity))
            }
            vTop.label = getString(R.string.personal_info)
            container.removeAllViews()
            container.setIconTextRowView(
                R.drawable.ic_cake,
                getString(R.string.date_of_birth),
                it.dateOfBirth.utcToLocalString(AppConstants.FORMAT_DATE_SMALL_WITH_YEAR)
            )
            container.setIconTextRowView(
                R.drawable.ic_email, getString(R.string.email), it.email
            )
            container.setIconTextRowView(
                R.drawable.ic_location, getString(R.string.address), it.addressInfo?.address
            )
            container.setIconTextRowView(
                R.drawable.ic_profile_add,
                getString(R.string.emergency_contacts_1),
                it.emergencyContacts?.getOrNull(0)?.getPhoneDetail()?.toString()
            )
            container.setIconTextRowView(
                R.drawable.ic_profile_add,
                getString(R.string.emergency_contacts_2),
                it.emergencyContacts?.getOrNull(1)?.getPhoneDetail()?.toString()
            )
            root.isVisible = true
        } ?: run {
            root.isVisible = false
        }
    }

    private fun setVehicleInfoView(documentResponse: DocumentResponse?) =
        with(binding.vVehicleInfo) {
            cvEdit.setOnClickListener {
               // startActivity(EditVehicleDetailActivity.newInstance(this@ProfileActivity))
            }
            if (documentResponse != null) {
                vTop.label = getString(R.string.vehicle_details)
                container.removeAllViews()
                container.setIconTextRowView(
                    R.drawable.ic_vehicle,
                    getString(R.string.vehicle_type),
                    dataProvider.getVehicleTypes()
                        .find { it.first == documentResponse.vehicleInfo?.vehicleType }?.second
                )

                container.setIconTextRowView(
                    R.drawable.ic_id_card,
                    getString(R.string.vehicle_name),
                    documentResponse.vehicleInfo?.vehicleName,
                )

                container.setIconTextRowView(
                    R.drawable.ic_id_card,
                    getString(R.string.license_plate),
                    documentResponse.vehicleInfo?.licensePlateNumber,
                )

                container.setIconTextRowView(
                    R.drawable.ic_id_card,
                    getString(R.string.motor_insurance),
                    documentResponse.vehicleInfo?.motorInsuranceNumber,
                )
                documentResponse.vehicleInfo?.motorInsuranceExpiryDate.utcToLocalString(
                    AppConstants.FORMAT_DATE_SMALL_WITH_YEAR
                )?.let {
                    container.setIconTextRowView(
                        R.drawable.ic_id_card,
                        getString(R.string.motor_insurance_expiry),
                        it,
                    )
                }

                documentResponse.identityProofList?.forEach { ids ->
                    when (ids.documentType) {
                        Enums.DOCUMENT_TYPE_NATIONAL_ID -> {
                            ids.documentType
                            container.setIconTextRowView(
                                R.drawable.ic_id_card,
                                getString(R.string.national_id),
                                getString(R.string.view)
                            ) {
                                startActivity(
                                    ViewDocumentActivity.newInstance(
                                        this@ProfileActivity,
                                        getString(R.string.national_id),
                                        arrayListOf(
                                            FileInfo(
                                                title = "Front",
                                                fileName = "national_id_front.png",
                                                url = ids.frontImage.orEmpty(),
                                            ), FileInfo(
                                                title = "Back",
                                                fileName = "national_id_back.png",
                                                url = ids.backImage.orEmpty()
                                            )
                                        )
                                    )
                                )
                            }
                        }

                        Enums.DOCUMENT_TYPE_DL -> {
                            container.setIconTextRowView(
                                R.drawable.ic_id_card,
                                getString(R.string.drivers_license),
                                getString(R.string.view)
                            ) {
                                startActivity(
                                    ViewDocumentActivity.newInstance(
                                        this@ProfileActivity,
                                        getString(R.string.drivers_license),
                                        arrayListOf(
                                            FileInfo(
                                                title = "Front",
                                                fileName = "dl_front.png",
                                                url = ids.frontImage.orEmpty()
                                            ), FileInfo(
                                                title = "Back",
                                                fileName = "dl_back.png",
                                                url = ids.backImage.orEmpty()
                                            )
                                        )
                                    )
                                )
                            }
                        }

                        else -> {
                            container.setIconTextRowView(
                                R.drawable.ic_id_card,
                                getString(R.string.other),
                                getString(R.string.view)
                            ) {
                                startActivity(
                                    ViewDocumentActivity.newInstance(
                                        this@ProfileActivity,
                                        getString(R.string.other),
                                        arrayListOf(
                                            FileInfo(
                                                title = "Front",
                                                fileName = "other_id_front.png",
                                                url = ids.frontImage.orEmpty()
                                            ), FileInfo(
                                                title = "Back",
                                                fileName = "other_id_back.png",
                                                url = ids.backImage.orEmpty()
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                val insuranceImage = documentResponse.vehicleInfo?.motorInsuranceCard.orEmpty()
                if (insuranceImage.isNotEmpty()) {
                    container.setIconTextRowView(
                        R.drawable.ic_id_card,
                        getString(R.string.motor_insurance_card),
                        getString(R.string.view)
                    ) {
                        startActivity(
                            ViewDocumentActivity.newInstance(
                                this@ProfileActivity,
                                getString(R.string.motor_insurance_card),
                                arrayListOf(
                                    FileInfo(
                                        title = "Front",
                                        fileName = "motor_insurance.png",
                                        url = insuranceImage,
                                    )
                                )
                            )
                        )
                    }
                }
                root.isVisible = true
            } else {
                root.isVisible = false
            }
        }

    private fun setPayoutInfoView(payoutResponse: PayoutResponse?) = with(binding.vPayoutInfo) {
        if (payoutResponse != null) {
            cvEdit.setOnClickListener {
               // startActivity(EditBankDetailActivity.newInstance(this@ProfileActivity))
            }
            vTop.label = getString(R.string.payment_details)
            container.removeAllViews()

            val method = dataProvider.getPaymentMethodTypes()
                .find { it.first == payoutResponse.method }?.second
            container.setIconTextRowView(
                R.drawable.ic_wallet, getString(R.string.payout_method), method
            )
            payoutResponse.bankDetail?.let { detail ->
                container.setIconTextRowView(
                    R.drawable.ic_bank, getString(R.string.bank_name), detail.bankName
                )
                container.setIconTextRowView(
                    R.drawable.ic_bank, getString(R.string.account_number), detail.accountNumber.maskLastChars(4)
                )
                container.setIconTextRowView(
                    R.drawable.ic_note, getString(R.string.code), detail.swiftCode
                )
            }

            payoutResponse.mobileMoney?.let { detail ->
                container.setIconTextRowView(
                    R.drawable.ic_money,
                    getString(R.string.mobile_money_network),
                    detail.networkName
                )
                container.setIconTextRowView(
                    R.drawable.ic_bank,
                    getString(R.string.mobile_money_name),
                    detail.mobileMoneyName
                )
                container.setIconTextRowView(
                    R.drawable.ic_note, getString(R.string.mobile_money_number), detail.number
                )
            }
            root.isVisible = true
        } else {
            root.isVisible = false
        }
    }

    /**Set observers for various events like button clicks and social login responses**/
    private fun setObserver() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> finish()
                R.id.iv_camera -> {
                  //  launcherCamera.launch(null)
                }
            }
        }
    }

    private fun LinearLayout.setIconTextRowView(
        @DrawableRes icon: Int, key: String, value: String?, onClick: (() -> Unit)? = null
    ) {
        val count = this.childCount
        ViewInfoIconTextBinding.inflate(layoutInflater, this, true).apply {
            this.ivIcon.setImageResource(icon)
            this.tvKey.text = key
            if (onClick != null) {
                this.tvValue.text = value.orEmpty().setMultiSpan(
                    color = ContextCompat.getColor(
                        this@ProfileActivity, R.color.orange
                    ), underLine = true
                )
                this.tvValue.setOnClickListener {
                    onClick()
                }
            } else {
                this.tvValue.text = value.orEmpty().setMultiSpan(bold = true)
            }
            if (count > 0) {
                val params = root.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = resources.getDimensionPixelSize(R.dimen._10sdp)
                root.layoutParams = params
            }
        }
    }

    override fun onStart() {
        super.onStart()
        apiCalls()
    }

//    private val launcherCamera =
//        registerForActivityResult(CameraActivity.CameraContract()) { path ->
//            if (path.isNotEmpty()) {
//                sharePref.setLocalProfileImage(path)
//                binding.profileImage = path
//                UpdateProfileImageWorker.createJob(this, path)
//            }
//        }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }


}



