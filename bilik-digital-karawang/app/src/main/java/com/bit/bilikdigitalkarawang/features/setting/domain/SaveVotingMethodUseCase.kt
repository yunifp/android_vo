package com.bit.bilikdigitalkarawang.features.setting.domain

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveVotingMethodUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) {
    operator fun invoke(method: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            dataStoreDiv.saveData("voting_method", method)
            emit(Resource.Success("Berhasil menyimpan metode pemilihan"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menyimpan metode pemilihan"))
        }
    }
}