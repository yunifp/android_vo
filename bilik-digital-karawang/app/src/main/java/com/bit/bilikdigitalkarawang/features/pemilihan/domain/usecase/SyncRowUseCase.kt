package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.mapPemilihanToSyncFormat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRow
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SyncRowUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv,
    private val getPemilihanUseCase: GetPemilihanUseCase
) {
    operator fun invoke(): Flow<Resource<CommonStatus>> = flow {
        emit(Resource.Loading())

        val token = dataStoreDiv.getData("sesi_token").first() ?: ""
        if (token.isEmpty()) {
            emit(Resource.Error("Token tidak ditemukan"))
            return@flow
        }

        getPemilihanUseCase().collect { pemilihanResource ->
            if (pemilihanResource is Resource.Success && !pemilihanResource.data.isNullOrEmpty()) {
                val jsonVotes = mapPemilihanToSyncFormat(pemilihanResource.data)
                val syncRow = SyncRow(votes = jsonVotes)
                val result = repository.syncRow(token, syncRow)

                if (result == CommonStatus.Success) {
                    val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date())
                    dataStoreDiv.saveData("last_sync_pemilihan", currentDateTime)
                    emit(Resource.Success(result))
                } else {
                    emit(Resource.Error("Gagal melakukan sinkronisasi rekap"))
                }
                return@collect
            }
        }
        emit(Resource.Error("Data pemilihan tidak ditemukan"))

    }
}
