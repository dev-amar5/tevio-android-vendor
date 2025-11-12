package com.tevioapp.vendor.presentation.views.profile.edit

import DateTimePickerUtil
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.Profile
import com.tevioapp.vendor.data.common.UserLocation
import com.tevioapp.vendor.databinding.ActivityEditPersonalinfoBinding
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.auth.phone.PhoneNumberFragment
import com.tevioapp.vendor.presentation.views.location.LocationActivity
import com.tevioapp.vendor.presentation.views.profile.ProfileViewModel
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.event.helper.InputUtils
import com.tevioapp.vendor.utility.event.helper.Status
import com.tevioapp.vendor.utility.extensions.capitalizeFirstLetter
import com.tevioapp.vendor.utility.extensions.focusError
import com.tevioapp.vendor.utility.extensions.toJsonObject
import com.tevioapp.vendor.utility.util.DateTimeUtils
import com.tevioapp.vendor.utility.util.DateTimeUtils.utcToLocalString
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class EditPersonalInfoActivity : BaseActivity<ActivityEditPersonalinfoBinding>() {
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var phoneNumberFragment1: PhoneNumberFragment
    private lateinit var phoneNumberFragment2: PhoneNumberFragment
    private var profile: Profile? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_edit_personalinfo
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        initView()
        addPhoneNumberView()
        setObserver()
    }

    private fun initView() {
        binding.header.apply {
            ivLogo.isVisible = false
            tvHeading.isVisible = true
            tvHeading.text = getString(R.string.personal_info)
        }
        binding.tvBirth.text = buildString {
            append(getString(R.string.date_of_birth), " (", getString(R.string.optional), ")")
        }
    }

    private fun setObserver() {
        viewModel.onClick.observe(this) { view ->
            when (view.id) {
                R.id.iv_back -> finish()
                R.id.btn_submit -> setUpdateProfile()
                R.id.et_address -> {
                    launcherLocation.launch(
                        LocationActivity.InputData(
                            profile?.addressInfo?.getLatLng(), "Choose Location"
                        )
                    )
                }

                R.id.et_date_of_birth -> {
                    DateTimePickerUtil.showDatePicker(
                        context = this,
                        selectedDate = DateTimeUtils.utcStringToUTCCalendar(profile?.dateOfBirth)?.timeInMillis,
                        maxDate = System.currentTimeMillis(),
                        title = getString(R.string.select_date),
                        outputFormat = AppConstants.FORMAT_DATE_SMALL_WITH_YEAR,
                        onDateSelected = { output, selectedDate ->
                            profile?.apply {
                                dateOfBirth =
                                    DateTimeUtils.localCalendarToUtcTimestamp(selectedDate)
                            }
                            binding.etDateOfBirth.setText(output)
                        })
                }
            }
        }
        viewModel.apiGetPersonalInfo().observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> startShimmer()
                Status.SUCCESS -> {
                    profile = resource.data
                    setDataInView()
                    stopShimmer()
                }

                else -> {
                    stopShimmer()
                    showShortMessage(resource.message)
                }
            }
        }
    }

    private fun addPhoneNumberView() {
        phoneNumberFragment1 = PhoneNumberFragment.addPhoneNumberView(
            fragmentManager = supportFragmentManager,
            conditionerId = R.id.emergency_contacts_1,
            inputData = PhoneNumberFragment.InputData(
                header = getString(R.string.emergency_contacts_1),
                icon = R.drawable.ic_profile_add,
                hint = getString(R.string.phone_number)
            )
        )
        phoneNumberFragment2 = PhoneNumberFragment.addPhoneNumberView(
            fragmentManager = supportFragmentManager,
            conditionerId = R.id.emergency_contacts_2,
            inputData = PhoneNumberFragment.InputData(
                header = buildString {
                    append(
                        getString(R.string.emergency_contacts_2),
                        " (",
                        getString(R.string.optional),
                        ")"
                    )
                }, icon = R.drawable.ic_profile_add, hint = getString(R.string.phone_number)
            )
        )
    }

    private fun setDataInView() {
        val data = profile ?: return
        binding.etFirstname.setText(data.firstName)
        binding.etLastname.setText(data.lastName)
        binding.etEmail.setText(data.email)
        binding.etDateOfBirth.setText(data.dateOfBirth?.utcToLocalString(AppConstants.FORMAT_DATE_SMALL_WITH_YEAR))
        binding.etAddress.setText(data.addressInfo?.address)
        when (data.gender) {
            Enums.GENDER_MALE -> binding.rbMale.isChecked = true
            Enums.GENDER_FEMALE -> binding.rbFemale.isChecked = true
        }

        data.emergencyContacts?.getOrNull(0)?.getPhoneDetail()?.let {
            phoneNumberFragment1.setPhoneDetails(it)
        }
        data.emergencyContacts?.getOrNull(1)?.getPhoneDetail()?.let {
            phoneNumberFragment2.setPhoneDetails(it)
        }

    }

    private fun setUpdateProfile() {
        val firstName = binding.etFirstname.text.toString().trim()
        val lastName = binding.etLastname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rb_male -> Enums.GENDER_MALE
            R.id.rb_female -> Enums.GENDER_FEMALE
            else -> null
        }
        if (firstName.isEmpty()) {
            binding.etFirstname.focusError(getString(R.string.empty_field))
            return
        }
        if (lastName.isEmpty()) {
            binding.etLastname.focusError(getString(R.string.empty_field))
            return
        }
        if (InputUtils.emailValid(email).not()) {
            binding.etEmail.focusError(getString(R.string.invalid_email))
            return
        }
        if (gender == null) {
            showShortMessage(getString(R.string.select_gender))
            return
        }
        if (InputUtils.emailValid(email).not()) {
            binding.etEmail.focusError(getString(R.string.invalid_email))
            return
        }
        if (address.isEmpty()) {
            showShortMessage(getString(R.string.select_address))
            binding.etAddress.performClick()
            return
        }
        val contact1 = phoneNumberFragment1.getValidatedPhoneDetails() ?: return
        val contact2 = if (phoneNumberFragment2.isPhoneNumberEntered()) {
            phoneNumberFragment2.getValidatedPhoneDetails() ?: return
        } else null

        val request = JSONObject()
        request.put("first_name", firstName.capitalizeFirstLetter())
        request.put("last_name", lastName.capitalizeFirstLetter())
        request.put("email", email)
        request.put("gender", gender)
        request.put("d_o_b", profile?.dateOfBirth)
        request.put("emergency_contacts", JSONArray().apply {
            put(contact1.toJsonObject())
            contact2?.let {
                put(it.toJsonObject())
            }
        })

        request.put("address_info", JSONObject().apply {
            put("address", address)
            put("latitude", profile?.addressInfo?.latitude)
            put("longitude", profile?.addressInfo?.longitude)
        })
        viewModel.apiSetPersonalInfo(request).observe(this) {
            when (it.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    showShortMessage(it.message)
                    finish()
                }

                else -> {
                    hideLoading()
                    showShortMessage(it.message)
                }
            }
        }
    }

    private val launcherLocation =
        registerForActivityResult(LocationActivity.LocationContract()) { result ->
            result?.let { newLatLng ->
                val address = profile?.addressInfo ?: Profile.Address()
                address.apply {
                    latitude = newLatLng.latitude
                    longitude = newLatLng.longitude
                    UserLocation.fromLatLng(
                        this@EditPersonalInfoActivity, newLatLng
                    )?.getFullAddress()?.let {
                        binding.etAddress.setText(it)
                        this.address = it
                    }
                }
                profile?.addressInfo = address
            }
        }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, EditPersonalInfoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }

}



