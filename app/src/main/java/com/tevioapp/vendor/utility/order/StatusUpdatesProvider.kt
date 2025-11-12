package com.tevioapp.vendor.utility.order

import android.content.Context
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LifecycleCoroutineScope
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.Order
import com.tevioapp.vendor.data.Ticket
import com.tevioapp.vendor.presentation.common.compoundviews.TimeLineView
import com.tevioapp.vendor.utility.Enums
import com.tevioapp.vendor.utility.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatusUpdatesProvider(
    private val context: Context, private val lifecycleCoroutineScope: LifecycleCoroutineScope
) {

    fun getForOrder(
        order: Order?, isShort: Boolean, onResult: (List<TimeLineView.TimelineStep>) -> Unit
    ) {
        lifecycleCoroutineScope.launch(Dispatchers.IO) {
            val list = mutableListOf<TimeLineView.TimelineStep>()
            if (isShort) {
                order?.let {
                    list.add(getTimeLineStep1(it))
                    list.add(getTimeLineStep2(it))
                    list.add(getTimeLineStep3(it))
                }
            } else {
                order?.updates?.forEach { update ->
                    if (update.isValid()) {
                        StatusHelper.getOrderStatusUI(order.cartType, update.status)?.let {
                            val actualTime =
                                DateTimeUtils.getDisplayDateTime(update.actualTime).orEmpty()
                            list.add(TimeLineView.TimelineStep(it.iconRes).apply {
                                title = buildSpannedString { append(it.description) }
                                isCompleted = actualTime.isNotEmpty()
                                subtitle = buildSpannedString {
                                    if (actualTime.isNotEmpty()) {
                                        append(actualTime)
                                    } else {
                                        append("By ${context.getString(R.string.dot_middle)} ")
                                        append(
                                            DateTimeUtils.getDisplayDateTime(update.estimatedTime)
                                                ?: "--"
                                        )
                                    }
                                }
                            })
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onResult(list)
            }
        }
    }


    private fun getTimeLineStep1(order: Order): TimeLineView.TimelineStep {
        val steps = when (order.cartType) {
            Enums.CART_TYPE_FOOD_DELIVERY, Enums.CART_TYPE_GROCERY, Enums.CART_TYPE_GROUP_ORDER -> {
                arrayListOf(
                    Enums.ORDER_STATUS_IN_PROCESS,
                    Enums.ORDER_STATUS_PROCESSED,
                    Enums.ORDER_STATUS_PACKED
                )
            }

            Enums.CART_TYPE_DROP_IT -> arrayListOf(
                Enums.ORDER_STATUS_IN_PROCESS, Enums.ORDER_STATUS_PROCESSED
            )

            else -> arrayListOf()
        }

        val timeLineView = TimeLineView.TimelineStep(R.drawable.ic_shop)
        timeLineView.title = buildSpannedString { append(order.pickupInfo.name) }
        timeLineView.isCompleted = true
        timeLineView.subtitle = buildSpannedString {
            order.updates?.findLast { it.status in steps && it.isCompleted() }
                ?.let { currentStatus ->
                    when (order.cartType) {
                        Enums.CART_TYPE_FOOD_DELIVERY, Enums.CART_TYPE_GROUP_ORDER -> {
                            when (currentStatus.status) {
                                Enums.ORDER_STATUS_IN_PROCESS -> {
                                    append("Order Preparing")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }

                                Enums.ORDER_STATUS_PROCESSED -> {
                                    append("Order Prepared")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }

                                Enums.ORDER_STATUS_PACKED -> {
                                    append("Order Packed")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }
                            }
                        }

                        Enums.CART_TYPE_GROCERY -> {
                            when (currentStatus.status) {
                                Enums.ORDER_STATUS_IN_PROCESS -> {
                                    append("Order In-Process")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }

                                Enums.ORDER_STATUS_PROCESSED -> {
                                    append("Order Processed")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }

                                Enums.ORDER_STATUS_PACKED -> {
                                    append("Order Packed")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }
                            }
                        }

                        Enums.CART_TYPE_DROP_IT -> {
                            when (currentStatus.status) {
                                Enums.ORDER_STATUS_IN_PROCESS -> {
                                    append("DropIt In-Process")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }

                                Enums.ORDER_STATUS_PROCESSED -> {
                                    append("DropIt Processed")
                                    DateTimeUtils.getDisplayDateTime(currentStatus.actualTime)
                                        ?.let {
                                            append(" ${context.getString(R.string.dot_middle)} ")
                                            append(it)
                                        }
                                }
                            }
                        }
                    }

                }
        }
        return timeLineView
    }

    private fun getTimeLineStep2(order: Order): TimeLineView.TimelineStep {
        val timeLineView = TimeLineView.TimelineStep(R.drawable.ic_scooter)
        timeLineView.title = buildSpannedString { append("Delivery") }
        order.updates?.find { it.status == Enums.ORDER_STATUS_OUT_FOR_DELIVERY }?.let { update ->
            timeLineView.isCompleted = update.isCompleted()
            timeLineView.subtitle = buildSpannedString {
                if (update.isCompleted()) {
                    append("Out for Delivery")
                    DateTimeUtils.getDisplayDateTime(update.actualTime)?.let {
                        append(" ${context.getString(R.string.dot_middle)} ")
                        append(it)
                    }
                } else {
                    append("Out for Delivery By")
                    DateTimeUtils.getDisplayDateTime(update.estimatedTime)?.let {
                        append(" ${context.getString(R.string.dot_middle)} ")
                        append(it)
                    }
                }
            }
        }
        return timeLineView
    }

    private fun getTimeLineStep3(order: Order): TimeLineView.TimelineStep {
        val timeLineView = TimeLineView.TimelineStep(R.drawable.ic_location)
        timeLineView.title = buildSpannedString { append(order.dropInfo.address) }
        timeLineView.isCompleted =
            order.courierStatus == Enums.COURIER_STATUS_ARRIVED_AT_DESTINATION
        timeLineView.subtitle = buildSpannedString {
            order.updates?.find { it.status == Enums.ORDER_STATUS_DELIVERED }?.let { update ->
                if (update.isCompleted()) {
                    append("Delivered")
                    DateTimeUtils.getDisplayDateTime(update.actualTime)?.let {
                        append(" ${context.getString(R.string.dot_middle)} ")
                        append(it)
                    }
                } else {
                    append("Delivered By")
                    DateTimeUtils.getDisplayDateTime(update.estimatedTime)?.let {
                        append(" ${context.getString(R.string.dot_middle)} ")
                        append(it)
                    }
                }
            }
        }
        return timeLineView
    }

    fun getOrderStatusDisplayText(order: Order): String {
        return StatusHelper.getOrderStatusUI(
            order.cartType, order.orderStatus
        )?.description.orEmpty()
    }

    fun getForTicket(ticket: Ticket?, onResult: (List<TimeLineView.TimelineStep>) -> Unit) {
        lifecycleCoroutineScope.launch(Dispatchers.IO) {
            val list = mutableListOf<TimeLineView.TimelineStep>()
            ticket?.updates?.forEach { update ->
                StatusHelper.getTicketStatusUI(update.status)?.let {
                    val actualTime = DateTimeUtils.getDisplayDateTime(update.actualTime).orEmpty()
                    list.add(TimeLineView.TimelineStep(it.iconRes).apply {
                        title = buildSpannedString { append(it.description) }
                        isCompleted = actualTime.isNotEmpty()
                        subtitle = buildSpannedString {
                            append(actualTime)
                        }
                    })
                }
            }
            withContext(Dispatchers.Main) {
                onResult(list)
            }
        }
    }
}