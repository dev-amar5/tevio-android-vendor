package com.tevioapp.vendor.utility.transform

import android.text.InputFilter
import android.text.Spanned

class SearchFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Prevent space at the first position
        if (dstart == 0 && source == " ") {
            return "" // Reject space as the first character
        }
        // Prevent multiple consecutive spaces
        if (dstart > 0 && source == " " && dest?.get(dstart - 1) == ' ') {
            return "" // Reject if trying to add a space after another space
        }

        return null // Accept the input
    }
}
