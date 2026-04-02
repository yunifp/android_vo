package com.bit.bilikdigitalkarawang.features.auth.presentation.pin

data class PinState(
    val pin: String = "",
    val confirmPin: String = "",
    val isPinCreated: Boolean = false,
    val isPinMatched: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)
