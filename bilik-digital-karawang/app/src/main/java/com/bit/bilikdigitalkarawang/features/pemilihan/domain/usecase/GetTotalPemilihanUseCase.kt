package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTotalPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<Int>> = flow {
        try {
            emit(Resource.Loading())
            val total = repository.getTotalPemilihan()
            emit(Resource.Success(total))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error"))
        }
    }
}
