package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetDetailPemilihUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(nik: String): Flow<Resource<Pemilih?>> = flow {
        try {
            emit(Resource.Loading())
            val pemilih = repository.getPemilihByNik(nik)
            emit(Resource.Success(pemilih))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}