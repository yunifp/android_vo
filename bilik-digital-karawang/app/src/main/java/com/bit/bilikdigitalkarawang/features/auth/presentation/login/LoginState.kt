package com.bit.bilikdigitalkarawang.features.auth.presentation.login

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.auth.domain.model.form_data.LoginFormData

data class LoginState (
    val formData: LoginFormData = LoginFormData(username = "", password = "", deviceId = ""),
    val status: CommonStatus = CommonStatus.Idle,
    val statusMsg: String = "",
    val showAlert: Boolean = false
)