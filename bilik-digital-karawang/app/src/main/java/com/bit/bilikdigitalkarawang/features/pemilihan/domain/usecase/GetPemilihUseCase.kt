package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPemilihUseCase @Inject constructor(
private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<List<Pemilih>>> = flow {
        try {
            emit(Resource.Loading())

            repository.getPemilih().collect { pemilihList ->
                emit(Resource.Success(pemilihList))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data kandidat"))
        }
    }
}