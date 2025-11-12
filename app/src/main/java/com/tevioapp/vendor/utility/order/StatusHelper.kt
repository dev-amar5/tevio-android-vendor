package com.tevioapp.vendor.utility.order

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tevioapp.vendor.R
import com.tevioapp.vendor.utility.Enums

object StatusHelper {

    private val commonStatuses = mapOf(
        Enums.ORDER_STATUS_CANCELLED to StatusUI(
            "Cancelled", "Order is cancelled", R.drawable.ic_cross, StatusColor.RED
        ), Enums.ORDER_STATUS_REJECTED to StatusUI(
            "Rejected", "Order is rejected", R.drawable.ic_cross, StatusColor.RED
        ), Enums.ORDER_STATUS_REFUND_INITIATED to StatusUI(
            "Refund", "Refund initiated", R.drawable.ic_money, StatusColor.RED
        ), Enums.ORDER_STATUS_REFUND_IN_PROCESS to StatusUI(
            "Refund", "Refund in process", R.drawable.ic_money, StatusColor.RED
        ), Enums.ORDER_STATUS_REFUND_PROCESSED to StatusUI(
            "Refund", "Refund processed", R.drawable.ic_money, StatusColor.GREEN
        )
    )

    private val baseDeliveryStatuses = mapOf(
        Enums.ORDER_STATUS_PENDING to StatusUI(
            "Pending", "Order confirmation pending", R.drawable.ic_clock_outline, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_CONFIRMED to StatusUI(
            "Confirmed", "Order confirmed", R.drawable.ic_tick, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_IN_PROCESS to StatusUI(
            "In Process", "Order in process", R.drawable.ic_cooking_pot, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_PROCESSED to StatusUI(
            "Processed", "Order processed", R.drawable.ic_tick, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_PACKED to StatusUI(
            "Packed", "Order packed", R.drawable.ic_bag, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_PICKED_UP to StatusUI(
            "Picked Up", "Order picked up", R.drawable.ic_bag, StatusColor.ORANGE
        ), Enums.ORDER_STATUS_OUT_FOR_DELIVERY to StatusUI(
            "Out for Delivery",
            "Order is out for delivery",
            R.drawable.ic_scooter,
            StatusColor.ORANGE
        ), Enums.ORDER_STATUS_DELIVERED to StatusUI(
            "Delivered", "Order delivered", R.drawable.ic_location, StatusColor.GREEN
        )
    )

    private val foodStatuses = baseDeliveryStatuses + commonStatuses
    private val groceryStatuses = baseDeliveryStatuses + commonStatuses
    private val dropItStatuses = baseDeliveryStatuses + commonStatuses


    private val registrationStatuses = mapOf(
        Enums.REGISTRATION_STATUS_PENDING to StatusUI(
            "Pending",
            "Your registration is pending",
            R.drawable.ic_clock_outline,
            StatusColor.ORANGE
        ), Enums.REGISTRATION_STATUS_IN_PROCESS to StatusUI(
            "In Process",
            "Your registration is in process",
            R.drawable.ic_clock_outline,
            StatusColor.ORANGE
        ), Enums.REGISTRATION_STATUS_APPROVED to StatusUI(
            "Approved", "Your account is approved", R.drawable.ic_tick, StatusColor.GREEN
        ), Enums.REGISTRATION_STATUS_REJECTED to StatusUI(
            "Rejected", "Your account is rejected", R.drawable.ic_cross, StatusColor.RED
        ), Enums.REGISTRATION_STATUS_SUBMITTED to StatusUI(
            "Submitted",
            "Your registration is submitted and pending for approval try after some time",
            R.drawable.ic_clock_outline,
            StatusColor.ORANGE
        )
    )

    private val ticketStatuses = mapOf(
        Enums.ORDER_STATUS_PENDING to StatusUI(
            "Submitted", "Ticket submitted", R.drawable.ic_clock_outline, StatusColor.ORANGE
        )
    )


    fun getOrderStatusUI(cartType: String?, status: String?): StatusUI? {
        if (status.isNullOrEmpty() || cartType.isNullOrEmpty()) return null
        return when (cartType) {
            Enums.CART_TYPE_FOOD_DELIVERY, Enums.CART_TYPE_GROUP_ORDER -> foodStatuses[status]
            Enums.CART_TYPE_GROCERY -> groceryStatuses[status]
            Enums.CART_TYPE_DROP_IT -> dropItStatuses[status]
            else -> null
        }
    }

    fun getTicketStatusUI(status: String?): StatusUI? {
        return ticketStatuses[status.orEmpty()]
    }

    fun getRegistrationStatusUI(status: String?): StatusUI? {
        return registrationStatuses[status.orEmpty()]
    }



}

/**
 * Represents a displayable status with title, description, icon, and color.
 */
data class StatusUI(
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val color: StatusColor
)

enum class StatusColor(
    @ColorRes val textColor: Int,
    @ColorRes val backgroundTint: Int,
    @DrawableRes val background: Int
) {
    GREEN(
        R.color.dark_green, R.color.green_alpha_20, R.drawable.bg_round_solid_3x
    ),
    RED(R.color.red, R.color.red_alpha_20, R.drawable.bg_round_solid_3x), ORANGE(
        R.color.orange, R.color.orange_alpha_12, R.drawable.bg_round_solid_3x
    ),
    YELLOW(R.color.white, R.color.yellow, R.drawable.bg_round_solid_3x)
}
