package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSuaraPerKandidatUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<List<SuaraSah>>> = flow {
        emit(Resource.Loading())
        repository.getJumlahSuaraSahPerKandidat()
            .map { suaraSahList ->
                Resource.Success(suaraSahList)
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data suara sah"))
            }
            .collect { result ->
                emit(result)
            }
    }
}