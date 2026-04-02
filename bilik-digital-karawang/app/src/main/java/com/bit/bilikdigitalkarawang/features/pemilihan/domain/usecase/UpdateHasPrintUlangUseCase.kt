package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateHasPrintUlangUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(nik: String): Flow<Resource<CommonStatus>> = flow {
        try {
            emit(Resource.Loading())

            val result = repository.updateHasPrintUlang(nik)

            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}