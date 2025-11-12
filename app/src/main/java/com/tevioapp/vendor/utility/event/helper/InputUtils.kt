package com.tevioapp.vendor.utility.event.helper

import android.util.Patterns
import androidx.databinding.ObservableField
import java.util.regex.Pattern

object InputUtils {

    /**
     * password pattern to check password
     * minimum 6 characters
     * at least 1 special character
     * at least 1 uppercase letter
     * at least 1 digit (number)
     * no white spaces
     */
    private val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^(?=.*[@#$%^&+=])" +           // at least 1 special character
                "(?=.*[A-Z])" +                // at least 1 uppercase letter
                "(?=.*\\d)" +                    // at least 1 digit (number)
                "(?=\\S+$)" +                   // no white spaces
                ".{6,}$"                         // at least 6 characters
    )

    /**
     * check if password is valid
     * @param password password to check
     */
    fun passwordInvalid(password: String?): Boolean {
        if (password == null) return true
        return !PASSWORD_PATTERN.matcher(password.trim { it <= ' ' }).matches()
    }

    /**
     * check if email is valid
     * @param email email to check
     */
    fun emailValid(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            return false
        }
        val emailPattern = Patterns.EMAIL_ADDRESS
        if (emailPattern != null) {
            return emailPattern.matcher(email.trim { it <= ' ' }).matches()
        }
        return false
    }



    /**
     * check if phone number is valid
     * @param phone phone number to check
     */
    fun ObservableField<String>.isEmpty(): Boolean {
        this.get()?.let {
            return it.trim().isEmpty()
        }
        return true
    }

}