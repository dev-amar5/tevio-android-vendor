package com.tevioapp.vendor.presentation.views.auth.phone


import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.PhoneDetails
import com.tevioapp.vendor.databinding.FragmentPhoneNumberBinding
import com.tevioapp.vendor.presentation.common.base.BaseFragment
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.views.country.CountryCode
import com.tevioapp.vendor.presentation.views.country.CountryCodeSheet
import com.tevioapp.vendor.presentation.views.country.CountryCodeSheetVM
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.extensions.findParcelData
import com.tevioapp.vendor.utility.extensions.focusError
import com.tevioapp.vendor.utility.workers.CountryListWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize


@AndroidEntryPoint
class PhoneNumberFragment : BaseFragment<FragmentPhoneNumberBinding>() {
    private var countryCode: CountryCode? = null
    private val viewModel: CountryCodeSheetVM by viewModels()
    private lateinit var inputData: InputData
    private var editable: Boolean = true
    override fun onCreateView(view: View, saveInstanceState: Bundle?) {
        inputData = arguments?.findParcelData("inputData", InputData::class.java)!!
        initView()
        doRegisterObservers()
        setDefaultCountryCode()
    }

    private fun initView() {
        binding.tvLabelPhone.text = inputData.header
        binding.etPhone.hint = inputData.hint
        binding.tvLabelPhone.setCompoundDrawablesWithIntrinsicBounds(
            inputData.icon, 0, 0, 0
        )
    }

    fun setEditable(editable: Boolean) {
        this.editable = editable
        binding.etPhone.isEnabled = editable
    }

    /**
     * register livedata observer and click listener
     */
    private fun doRegisterObservers() {
        viewModel.onClick.observe(viewLifecycleOwner) { view ->
            when (view.id) {
                R.id.et_country_code -> {
                    if (editable) showCountryCodeSheet()
                }
            }
        }
        WorkManager.getInstance(baseContext)
            .getWorkInfosForUniqueWorkLiveData(CountryListWorker.TAG)
            .observe(viewLifecycleOwner) { workInfos ->
                val workInfo = workInfos.firstOrNull() ?: return@observe
                if (workInfo.state == WorkInfo.State.SUCCEEDED && workInfo.outputData.getBoolean(
                        "updated",
                        false
                    )
                ) {
                    setDefaultCountryCode()
                }
            }
    }

    private fun setDefaultCountryCode() {
        viewModel.getCountryCodeByIso().observe(viewLifecycleOwner) { result ->
            result?.let {
                inputData.defaultPhoneDetails?.let {
                    setPhoneDetails(it)
                } ?: run {
                    updateCountryCode(it)
                }
            }
        }
    }


    fun updateCountryCode(countryCode: CountryCode) {
        this.countryCode = countryCode
        binding.etPhone.text = null
        binding.etCountryCode.setText(countryCode.dialCode)
        countryCode.maxLength?.let {
            val filter = InputFilter.LengthFilter(it)
            binding.etPhone.filters = arrayOf(filter)
        }
    }

    fun setPhoneDetails(phoneDetails: PhoneDetails) {
        viewModel.getCountryCodeByIso(phoneDetails.isoCode).observe(viewLifecycleOwner) { result ->
            result?.let { countryCode ->
                updateCountryCode(countryCode)
                binding.etPhone.setText(phoneDetails.phoneNumber)
            }
        }
    }

    private fun showCountryCodeSheet() {
        CountryCodeSheet {
            updateCountryCode(it)
        }.showSheet(this)
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_phone_number
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    fun getValidatedPhoneDetails(): PhoneDetails? {
        val selectedCountry = countryCode
        fun isValidMobile(countryCode: CountryCode, phone: String): Boolean {
            val min = countryCode.minLength ?: AppConstants.DEFAULT_PHONE_MIN_LENGTH
            val max = countryCode.maxLength ?: AppConstants.DEFAULT_PHONE_MAX_LENGTH
            return phone.length in min..max
        }
        if (selectedCountry == null) {
            showShortMessage(getString(R.string.select_country_code))
            return null
        }
        val phoneText = binding.etPhone.text.toString().trim()
        if (phoneText.isEmpty()) {
            binding.etPhone.focusError(getString(R.string.phone_number))
            return null
        }

        if (isValidMobile(selectedCountry, phoneText).not()) {
            binding.etPhone.focusError(getString(R.string.phone_number_not_valid))
            return null
        }
        val dialCodeWithOutPlus = selectedCountry.dialCode.replace("+", "")
        return PhoneDetails(
            phoneNumber = phoneText,
            countryCode = dialCodeWithOutPlus,
            isoCode = selectedCountry.code
        )
    }

    fun isPhoneNumberEntered(): Boolean {
        val phoneText = binding.etPhone.text.toString().trim()
        return phoneText.isNotEmpty()
    }


    @Parcelize
    data class InputData(
        val header: String,
        val icon: Int,
        val hint: String,
        val defaultPhoneDetails: PhoneDetails? = null
    ) : Parcelable

    companion object {
        fun addPhoneNumberView(
            fragmentManager: FragmentManager, @IdRes conditionerId: Int, inputData: InputData
        ): PhoneNumberFragment {
            val fragment = PhoneNumberFragment()
            fragment.arguments = Bundle().apply {
                putParcelable("inputData", inputData)
            }
            fragmentManager.beginTransaction().replace(conditionerId, fragment).commit()
            return fragment
        }
    }

}
