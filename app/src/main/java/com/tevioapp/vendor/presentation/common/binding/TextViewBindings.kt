package com.tevioapp.vendor.presentation.common.binding

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.Campaigns
import com.tevioapp.vendor.data.Order
import com.tevioapp.vendor.data.Ticket
import com.tevioapp.vendor.presentation.common.compoundviews.Milestone
import com.tevioapp.vendor.presentation.common.compoundviews.MilestoneProgressView
import com.tevioapp.vendor.utility.AppConstants.FORMAT_DATE_MEDIUM
import com.tevioapp.vendor.utility.AppConstants.FORMAT_TIME
import com.tevioapp.vendor.utility.CommonMethods
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.currency.CurrencyUtils
import com.tevioapp.vendor.utility.extensions.SpanConfig
import com.tevioapp.vendor.utility.extensions.formatMeterToKm
import com.tevioapp.vendor.utility.extensions.setMultiSpan
import com.tevioapp.vendor.utility.order.StatusHelper
import com.tevioapp.vendor.utility.util.DateTimeUtils
import com.tevioapp.vendor.utility.util.DateTimeUtils.calendarToFormattedString
import com.tevioapp.vendor.utility.util.DateTimeUtils.utcToLocalCalendar
import java.util.Calendar
import java.util.Locale


object TextViewBindings {

    @JvmStatic
    @BindingAdapter(
        value = ["currency", "currency_default", "strike_through", "currency_suffix"],
        requireAll = false
    )
    fun setCurrencyText(
        textView: TextView,
        amount: Double?,
        currencyDefault: String?,
        strikeThrough: Boolean = false,
        currencySuffix: String?
    ) {
        val text = buildString {
            val str1 = CurrencyUtils.formatAsCurrency(amount, currencyDefault.orEmpty())
            if (str1.isNotEmpty()) {
                if (currencySuffix.orEmpty().isNotEmpty()) append(currencySuffix)
                append(str1)
            }
        }
        textView.text = if (strikeThrough) {
            text.setMultiSpan(strikeThrough = true)
        } else {
            text
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["underline", "underline_web_link"], requireAll = false)
    fun underLine(textview: TextView, underLine: Boolean?, webLink: String?) {
        if (underLine == true) {
            val text = textview.text.toString()
            textview.text = text.setMultiSpan(underLine = true)
        }
        val url = webLink.orEmpty()
        if (url.isNotEmpty()) {
            textview.setOnClickListener {
                CommonMethods.openUrlInBrowser(textview.context, url)
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["full_text", "searched_text"], requireAll = false)
    fun searchText(textView: TextView, text: String?, text2: String?) {
        try {
            if (text.isNullOrEmpty()) {
                textView.text = text
                return
            }
            if (text2.isNullOrEmpty()) {
                textView.text = text
                return
            }
            val startIndex =
                text.lowercase(Locale.getDefault()).indexOf(text2.lowercase(Locale.getDefault()))
            if (startIndex > -1) {
                val endIndex = startIndex + text2.length
                val sb = SpannableStringBuilder(text)
                val fcs = ForegroundColorSpan(
                    ContextCompat.getColor(
                        textView.context, R.color.orange
                    )
                )
                val bss = StyleSpan(Typeface.BOLD)
                sb.setSpan(fcs, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                sb.setSpan(bss, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textView.text = sb
            } else {
                textView.text = text
            }
        } catch (e: java.lang.Exception) {
            textView.text = text
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["distance", "distance_suffix"], requireAll = false)
    fun locationDistance(textview: TextView, distance: Double?, suffix: String?) {
        textview.text = buildString {
            append(distance.formatMeterToKm())
            if (suffix.isNullOrEmpty().not()) {
                append(" ", suffix)
            }
        }
    }


    @JvmStatic
    @BindingAdapter(
        value = ["date_timestamp", "date_format", "date_prefix", "date_suffix", "date_today_only_time"],
        requireAll = false
    )
    fun setDateFormat1(
        textview: TextView,
        timeStamp: String?,
        format: String?,
        prefix: String?,
        suffix: String?,
        todayOnlyTime: Boolean?
    ) {

        val calender = utcToLocalCalendar(timeStamp)
        textview.text = if (calender == null) {
            timeStamp.orEmpty()
        } else {
            buildString {
                val d1 = format ?: FORMAT_DATE_MEDIUM
                if (prefix.orEmpty().isNotEmpty()) {
                    append(prefix)
                }
                append(
                    if (todayOnlyTime == true && DateTimeUtils.compareDate(
                            calender, Calendar.getInstance()
                        ) == 0
                    ) {
                        calendarToFormattedString(calender, FORMAT_TIME)
                    } else {
                        calendarToFormattedString(calender, d1)
                    }
                )
                if (suffix.orEmpty().isNotEmpty()) {
                    append(suffix)
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["primary_text", "secondary_text", "distance"], requireAll = false)
    fun predicateAutoComplete(
        textview: TextView,
        primaryText: AutocompletePrediction?,
        secondaryText: AutocompletePrediction?,
        distance: AutocompletePrediction?
    ) {

        if (primaryText != null) textview.text = primaryText.getPrimaryText(null)
        else if (secondaryText != null) textview.text = secondaryText.getSecondaryText(null)
        else if (distance != null) textview.text =
            distance.distanceMeters?.toDouble().formatMeterToKm()
        else textview.text = null

    }

    @JvmStatic
    @BindingAdapter(value = ["text_optional"], requireAll = false)
    fun textOptional(
        textview: TextView, enabled: Boolean?
    ) {
        if (enabled == true) {
            textview.text = buildString {
                append(
                    textview.text.toString(),
                    " (",
                    textview.context.getString(R.string.optional),
                    ")"
                )
            }
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["cart_type"], requireAll = false
    )
    fun setCartType(
        textView: TextView,
        type: String?,
    ) {
        textView.text = when (type) {
            Enums.CART_TYPE_FOOD_DELIVERY, Enums.CART_TYPE_GROUP_ORDER -> textView.context.getString(
                R.string.food_delivery
            )

            Enums.CART_TYPE_DROP_IT -> textView.context.getString(R.string.drop_it)
            Enums.CART_TYPE_GROCERY -> textView.context.getString(R.string.store_delivery)
            else -> textView.context.getString(R.string.hint_dash)
        }
    }


    @JvmStatic
    @BindingAdapter(
        value = ["order_status"], requireAll = false
    )
    fun setOrderStatus(
        textView: TextView,
        order: Order?,
    ) {
        StatusHelper.setOrderStatusView(textView, order)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["ticket_status"], requireAll = false
    )
    fun setTicketStatus(
        textView: TextView,
        ticket: Ticket?,
    ) {
        StatusHelper.setTicketStatusView(textView, ticket)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["custom_text_color"], requireAll = false
    )
    fun setTextColor(
        textView: TextView,
        colorCode: String?,
    ) {
        val defaultColor = textView.currentTextColor
        val color = try {
            if (colorCode.isNullOrEmpty()) {
                defaultColor
            } else {
                colorCode.toColorInt()
            }
        } catch (e: Exception) {
            defaultColor
        }
        textView.setTextColor(color)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["campaign_time_info"], requireAll = false
    )
    fun setCampaignTimeInfo(
        textView: TextView,
        timeInfo: Campaigns.TimeInfo?,
    ) {
        val daysLeft = timeInfo?.daysLeft
        if (daysLeft != null) {
            textView.text = buildString {
                append(daysLeft.toString())
                append(" ", "Days Left")
            }
        } else {
            textView.text = ""
        }
    }


    @JvmStatic
    @BindingAdapter(
        value = ["milestone_info"], requireAll = false
    )
    fun setMilestoneInfo(
        view: MilestoneProgressView,
        bean: Campaigns?,
    ) {
        val list = bean?.progress?.milestones.orEmpty()
        if (list.isNotEmpty()) {
            view.isVisible = true
            val percent = bean?.progress?.getProgressPercent() ?: 0F
            val configTop = SpanConfig(
                bold = true,
                typeface = ResourcesCompat.getFont(view.context, R.font.montserrat_medium)
            )
            val configBottom = SpanConfig(
                typeface = ResourcesCompat.getFont(
                    view.context, R.font.montserrat_regular
                )
            )
            view.setData(
                list.map {
                    Milestone(
                        it.getDisplayAmount().setMultiSpan(configTop),
                        it.getDisplayLabel().setMultiSpan(configBottom)
                    )
                }, percent
            )
        } else {
            view.isVisible = false
        }
    }
}

