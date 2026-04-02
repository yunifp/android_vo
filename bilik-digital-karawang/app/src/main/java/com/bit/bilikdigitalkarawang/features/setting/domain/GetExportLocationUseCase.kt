package com.bit.bilikdigitalkarawang.features.setting.domain

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetExportLocationUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv,
) {

    operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val exportPath = dataStoreDiv.getData("export_path").first() ?: ""

            emit(Resource.Success(exportPath))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data path"))
        }
    }
}