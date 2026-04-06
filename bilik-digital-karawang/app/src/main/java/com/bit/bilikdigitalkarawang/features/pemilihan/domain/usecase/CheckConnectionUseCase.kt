package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.shared.data.source.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CheckConnectionUseCase @Inject constructor(
    private val apiService: ApiService
) {
    operator fun invoke(): Flow<Resource<Boolean>> = flow {
        try {
            val response = apiService.checkConnection()
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Mode Oflline"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Koneksi terputus atau Server down"))
        }
    }
}