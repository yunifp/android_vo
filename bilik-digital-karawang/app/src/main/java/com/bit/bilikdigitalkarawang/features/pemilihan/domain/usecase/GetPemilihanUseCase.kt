package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<List<Pemilihan>>> = flow {
        try {
            emit(Resource.Loading())

            repository.getPemilihan().collect {
                emit(Resource.Success(it))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data kandidat"))
        }
    }
}