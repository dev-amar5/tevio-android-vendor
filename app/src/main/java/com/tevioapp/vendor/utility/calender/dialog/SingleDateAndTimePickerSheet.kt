package com.tevioapp.vendor.utility.calender.dialog

import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.SheetSingleDateTimeBinding
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.sheet.BaseBottomSheet
import com.tevioapp.vendor.utility.AppConstants
import com.tevioapp.vendor.utility.calender.DateHelper
import com.tevioapp.vendor.utility.calender.SingleDateAndTimePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SingleDateAndTimePickerSheet(
    private val onDateSelect: (Date) -> Unit, private val onNegativeClick: (() -> Unit)?=null
) : BaseBottomSheet<SheetSingleDateTimeBinding>() {
    private val viewModel: SingleDateTimeSheetVM by viewModels()
    private val dateHelper = DateHelper()
    private lateinit var picker: SingleDateAndTimePicker
    private var title: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var minDate: Date? = null
    private var maxDate: Date? = null
    private var defaultDate: Date? = null
    private var dateOnly: Boolean=false
    private var dateFormat: SimpleDateFormat=SimpleDateFormat(AppConstants.FORMAT_DATE_SMALL, Locale.US)
    override fun onSheetCreated(binding: SheetSingleDateTimeBinding) {
        picker = binding.vDateTime
        picker.setDateHelper(dateHelper)
        binding.vLabel.label = title ?: getString(R.string.select_time)
        binding.btnSave.text = positiveText ?: getString(R.string.save)
        binding.tvCancel.text = negativeText
        binding.tvCancel.isGone = negativeText.isNullOrEmpty()

        viewModel.onClick.observe(viewLifecycleOwner) { view ->
            when (view.id) {
                R.id.iv_cross -> dismissAllowingStateLoss()
                R.id.tv_cancel -> {
                    dismissAllowingStateLoss()
                    baseActivity?.baseHandler?.postDelayed({
                        onNegativeClick?.invoke()
                    }, 300)
                }

                R.id.btn_save -> {
                    val selectedDate = picker.date
                    dismissAllowingStateLoss()
                    baseActivity?.baseHandler?.postDelayed({
                        onDateSelect.invoke(selectedDate)
                    }, 300)
                }
            }
        }

        picker.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.montserrat_medium))
        picker.setDayFormatter(dateFormat)
        picker.setDisplayHours(dateOnly.not())
        picker.setDisplayMinutes(dateOnly.not())

        if (minDate != null) {
            picker.minDate = minDate
        }
        if (maxDate != null) {
            picker.maxDate = maxDate
        }

        if (defaultDate != null) {
            picker.setDefaultDate(defaultDate)
        }


    }


    fun setTitle(title: String?): SingleDateAndTimePickerSheet {
        this.title = title
        return this
    }

    fun setNegativeText(negativeText: String?): SingleDateAndTimePickerSheet {
        this.negativeText = negativeText
        return this
    }

    fun setPositiveText(positiveText: String?): SingleDateAndTimePickerSheet {
        this.positiveText = positiveText
        return this
    }
    fun setDateOnly(dateOnly:Boolean):SingleDateAndTimePickerSheet{
        this.dateOnly = dateOnly
        return this
    }

    fun setDateFormat(dateFormat: SimpleDateFormat):SingleDateAndTimePickerSheet{
        this.dateFormat = dateFormat
        return this
    }

    fun setMinDateRange(minDate: Date): SingleDateAndTimePickerSheet {
        this.minDate = minDate
        return this
    }

    fun setMaxDateRange(maxDate: Date): SingleDateAndTimePickerSheet {
        this.maxDate = maxDate
        return this
    }

    fun setDefaultDate(defaultDate: Date): SingleDateAndTimePickerSheet {
        this.defaultDate = defaultDate
        return this
    }


    override fun getLayoutResource(): Int {
        return R.layout.sheet_single_date_time
    }

    override fun getViewModel(): BaseViewModel{
        return viewModel
    }
}


