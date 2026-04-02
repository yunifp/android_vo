package com.bit.bilikdigitalkarawang.features.auth.domain.model.form_data

data class LoginFormData (
    val username: String,
    val password: String,
    val deviceId: String
)