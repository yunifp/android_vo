package com.bit.bilikdigitalkarawang.features.setting.domain

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetVotingMethodUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv,
) {
    operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            // Jika belum ada data, default ke "QR Code"
            val method = dataStoreDiv.getData("voting_method").first() ?: "QR Code"
            emit(Resource.Success(method))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data metode pemilihan"))
        }
    }
}