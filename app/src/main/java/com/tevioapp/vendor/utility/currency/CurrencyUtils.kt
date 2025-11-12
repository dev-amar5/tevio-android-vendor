package com.tevioapp.vendor.utility.currency

import java.text.DecimalFormat

object CurrencyUtils {

    fun formatAsCurrency(amount: Double?, default: String = ""): String {
        if (amount == null) {
            return default
        }
        val decimalFormat = DecimalFormat("#,###.##")
        val formattedAmount = decimalFormat.format(amount)
        return "₵ $formattedAmount"
    }
}
