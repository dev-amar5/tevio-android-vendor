import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.util.DateTimeUtils
import com.tevioapp.vendor.utility.util.DateTimeUtils.calendarToFormattedString
import java.util.Calendar
import java.util.TimeZone

object DateTimePickerUtil {

    fun showTimePicker(
        context: Context,
        selectedTime: Calendar? = null,
        is24Hour: Boolean = false,
        outputFormat: String = "hh:mm a",
        title: String = "Select Time",
        onTimeSelected: (formattedTime: String, selectedCalendar: Calendar) -> Unit
    ) {
        val initialHour = selectedTime?.get(Calendar.HOUR_OF_DAY) ?: 12
        val initialMinute = selectedTime?.get(Calendar.MINUTE) ?: 0

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(initialHour).setMinute(initialMinute).setTitleText(title)
            .setTheme(R.style.MaterialTimePicker).build()

        val activity = unwrapActivity(context)
        activity?.let {
            timePicker.show(it.supportFragmentManager, "MaterialTimePicker")
        }

        timePicker.addOnPositiveButtonClickListener {
            val localCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val formattedTime = calendarToFormattedString(
                localCalendar, outputFormat, TimeZone.getDefault()
            ) ?: ""

            onTimeSelected(formattedTime, localCalendar)
        }
    }


    fun showDatePicker(
        context: Context,
        selectedDate: Long? = null,
        minDate: Long? = null,
        maxDate: Long? = null,
        outputFormat: String = "dd MMM yyyy",
        title: String = "Select Date",
        onDateSelected: (formattedDate: String, selectedCalendar: Calendar) -> Unit
    ) {
        val validators = mutableListOf<CalendarConstraints.DateValidator>()
        minDate?.let { validators.add(DateValidatorPointForward.from(it)) }
        maxDate?.let { validators.add(DateValidatorPointBackward.before(it)) }

        val constraintsBuilder = CalendarConstraints.Builder()
        if (validators.isNotEmpty()) {
            constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators))
        }

        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(title)
            .setSelection(selectedDate ?: MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build()).setTheme(R.style.MaterialDatePicker)
            .build()

        val activity = unwrapActivity(context)
        activity?.let {
            datePicker.show(it.supportFragmentManager, "MaterialDatePicker")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            // Convert selection from UTC to local for display
            val utcCalendar = DateTimeUtils.getUtcCalenderInstance().apply {
                timeInMillis = selection
            }

            val localCalendar = DateTimeUtils.convertUtcToLocal(utcCalendar)!!

            val formattedDate = calendarToFormattedString(
                localCalendar, outputFormat, TimeZone.getDefault()
            ) ?: ""
            onDateSelected(formattedDate, localCalendar)
        }
    }

    private fun unwrapActivity(context: Context): AppCompatActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is AppCompatActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
