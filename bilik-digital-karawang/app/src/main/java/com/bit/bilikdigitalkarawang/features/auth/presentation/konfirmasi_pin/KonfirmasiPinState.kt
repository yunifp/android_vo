package com.bit.bilikdigitalkarawang.features.auth.presentation.konfirmasi_pin

data class KonfirmasiPinState(
    val pin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isVerified: Boolean = false
)