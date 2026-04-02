package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetKandidatUseCase @Inject constructor(
private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<List<Kandidat>>> = flow {
        try {
            emit(Resource.Loading())

            repository.getKandidat().collect { kandidatList ->
                emit(Resource.Success(kandidatList))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data kandidat"))
        }
    }
}