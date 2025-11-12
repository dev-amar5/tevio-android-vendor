package com.tevioapp.vendor.utility.util

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import com.tevioapp.vendor.R
import com.tevioapp.vendor.presentation.common.base.BaseActivity
import com.tevioapp.vendor.utility.extensions.showKeyboard

class PassCodeUtils(val activity: FragmentActivity) {
    private lateinit var et1: EditText
    private lateinit var et2: EditText
    private lateinit var et3: EditText
    private lateinit var et4: EditText

    fun setPassCodeView(et1: EditText, et2: EditText, et3: EditText, et4: EditText) {
        this.et1 = et1
        this.et2 = et2
        this.et3 = et3
        this.et4 = et4
        et1.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                et2.requestFocus()
            }
        }
        et2.doOnTextChanged { text, _, before, _ ->
            if (before == 1) {
                et1.requestFocus()
            } else if (!text.isNullOrEmpty()) et3.requestFocus()
        }
        et3.doOnTextChanged { text, _, before, _ ->
            if (before == 1) {
                et2.requestFocus()
            } else if (!text.isNullOrEmpty()) et4.requestFocus()
        }
        et4.doOnTextChanged { text, _, before, _ ->
            if (before == 1) {
                et3.requestFocus()
            } else if (!text.isNullOrEmpty()) et4.clearFocus()
        }
        et1.requestFocus()
        activity.showKeyboard(et1)
    }

    fun setOtpInView(otp: String?) {
        if (otp.isNullOrEmpty()) {
            et1.setText("")
            et2.setText("")
            et3.setText("")
            et4.setText("")
        } else {
            otp.toCharArray().forEachIndexed { index, c ->
                when (index) {
                    0 -> {
                        et1.setText(c.toString())
                    }

                    1 -> {
                        et2.setText(c.toString())
                    }

                    2 -> {
                        et3.setText(c.toString())
                    }

                    3 -> {
                        et4.setText(c.toString())
                    }
                }
            }
        }
    }

    fun getValidOTP(): String? {
        val s1 = isOTPFieldValid(et1) ?: return null
        val s2 = isOTPFieldValid(et2) ?: return null
        val s3 = isOTPFieldValid(et3) ?: return null
        val s4 = isOTPFieldValid(et4) ?: return null
        return s1 + s2 + s3 + s4
    }

    private fun isOTPFieldValid(editText: EditText): String? {
        val s1 = editText.text.toString().trim().toIntOrNull()
        if (s1 == null) {
            editText.requestFocus()
            if (activity is BaseActivity<*>)
                activity.showShortMessage(activity.getString(R.string.enter_the_4_digit_code_sent_to_you))
            return null
        }
        return s1.toString()
    }
}