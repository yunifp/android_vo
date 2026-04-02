package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.util.Log
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.ListVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toPemilihanEntity
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.shared.domain.usecase.GetDeviceIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DownloadListVoteUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv,
    private val getDeviceIdUseCase: GetDeviceIdUseCase,
) {
    operator fun invoke(): Flow<Resource<ListVoteResponse>> = flow {
        try {
            emit(Resource.Loading())

            val token = dataStoreDiv.getData("sesi_token").first() ?: ""

            // Get device ID
            val deviceId: String = getDeviceIdUseCase()

            // Download pemilih data
            val result = repository.getListVote(token, deviceId)

            Log.d("LOGASF", token)
            Log.d("LOGASF", deviceId)
            Log.d("LOGASF", result.toString())

            if (result.success) {
                result.listVoteDto.voteDtos.map { vote ->
                    val pemilihan = vote.toPemilihanEntity()
                    repository.insertPemilihan(pemilihan)
                }
                emit(Resource.Success(result))
            } else {
                emit(Resource.Error("Gagal mengunduh data pemilih"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengunduh data pemilih"))
        }
    }
}