package com.tevioapp.vendor.utility.transform

import android.text.InputFilter
import android.text.Spanned

class CustomFilter(val maxLength: Int, private val allowedCharacters: String) : InputFilter {

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

        // Calculate the new length if the input is added
        val newLength = (dest?.length ?: 0) + (end - start) - (dend - dstart)

        // If the new length exceeds $maxLength characters, trim the input
        if (newLength > maxLength) {
            val keep = maxLength - (dest?.length ?: 0) + (dend - dstart)
            return if (keep <= 0) {
                "" // If no characters can be added, reject the input
            } else {
                source?.subSequence(start, start + keep) // Trim the input to fit within the limit
            }
        }
        // Validate each character in the input
        for (i in start until end) {
            val char = source?.getOrNull(i) ?: continue
            if (!allowedCharacters.contains(char)) {
                return "" // Reject the character if it's not allowed
            }
        }

        return null // Accept the input
    }
}
