package com.bit.bilikdigitalkarawang.features.auth.data.repository

import com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto.LoginResponse
import com.bit.bilikdigitalkarawang.features.auth.domain.model.form_data.LoginFormData
import com.bit.bilikdigitalkarawang.features.auth.domain.repository.AuthRepository
import com.bit.bilikdigitalkarawang.shared.data.source.remote.ApiService
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService
): AuthRepository {

    override suspend fun login(formData: LoginFormData): LoginResponse {
        return api.login(
            username = formData.username.toRequestBody(),
            password = formData.password.toRequestBody(),
            deviceId = formData.deviceId.toRequestBody()
        )
    }

}