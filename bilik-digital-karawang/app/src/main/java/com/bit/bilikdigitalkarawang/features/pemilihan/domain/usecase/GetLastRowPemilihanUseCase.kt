package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetLastRowPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<String?>> {
        return repository.getLastRowPemilihan()
            .map<String?, Resource<String?>> { nik ->
                Resource.Success(nik)
            }
            .onStart {
                emit(Resource.Loading())
            }
            .catch { e ->
                emit(
                    Resource.Error(
                        e.localizedMessage ?: "Gagal mengambil data kandidat"
                    )
                )
            }
    }
}

