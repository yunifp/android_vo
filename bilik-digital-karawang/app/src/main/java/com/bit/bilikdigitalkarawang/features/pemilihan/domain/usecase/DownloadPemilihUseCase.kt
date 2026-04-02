package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DownloadPemilihUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv
) {
    operator fun invoke(): Flow<Resource<CommonStatus>> = flow {
        try {
            emit(Resource.Loading())

            // Get token from DataStore
            val token = dataStoreDiv.getData("sesi_token").first() ?: ""

            if (token.isEmpty()) {
                emit(Resource.Error("Token tidak ditemukan"))
                return@flow
            }

            // Download pemilih data
            val result = repository.downloadPemilih(token)

            if (result == CommonStatus.Success) {
                // Save current datetime as last_get_data
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
                dataStoreDiv.saveData("last_get_data", currentDateTime)

                emit(Resource.Success(result))
            } else {
                emit(Resource.Error("Gagal mengunduh data pemilih"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengunduh data pemilih"))
        }
    }
}