package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ResetPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv
) {
    operator fun invoke(
        pinPanitia: String,
        pinPengembang: String
    ): Flow<Resource<CommonStatus>> = flow {
        try {
            emit(Resource.Loading())

            // ===== Hardcode PIN di sini =====
            val correctPinPanitia = dataStoreDiv.getData("user_pin").first()
            val correctPinPengembang = "7777"

            if (pinPanitia != correctPinPanitia) {
                emit(Resource.Error("Kredensial salah"))
                return@flow
            }

            if (pinPengembang != correctPinPengembang) {
                emit(Resource.Error("Kredensial salah"))
                return@flow
            }

            // Jika sudah benar → jalankan reset
            val result = repository.resetPemilihan()
            emit(Resource.Success(result))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}
