package com.tevioapp.vendor.utility.listener

class SnackAction(
    val action: String? = null,
    val onActionClick: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null
)

