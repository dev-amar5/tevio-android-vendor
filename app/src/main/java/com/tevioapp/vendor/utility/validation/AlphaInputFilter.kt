package com.tevioapp.vendor.utility.validation

import android.text.InputFilter
import android.text.Spanned

class AlphaInputFilter: InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        for (i in start until end) {
            if (!Character.isLetter(source?.get(i) ?: return "")) {
                return "" // Reject the character
            }
        }
        return null // Accept the character
    }
}