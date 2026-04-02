package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CekNikValidUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(nik: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            if (repository.hasAlreadyVoted(nik)) {
                emit(Resource.Error("Anda sudah melakukan pemilihan"))
                return@flow
            }

            val pemilih = repository.getPemilihByNik(nik)

            if(pemilih == null) {
                emit(Resource.Error("NIK tidak terdaftar di data pemilih"))
                return@flow
            }

            emit(Resource.Success(pemilih != null))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}