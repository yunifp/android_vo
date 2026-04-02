package com.bit.bilikdigitalkarawang.features.auth.domain.repository

import com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto.LoginResponse
import com.bit.bilikdigitalkarawang.features.auth.domain.model.form_data.LoginFormData

interface AuthRepository {

    suspend fun login(formData: LoginFormData): LoginResponse

}