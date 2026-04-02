package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLastSyncPemilihanUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) {
    operator fun invoke(): Flow<String> {
        return dataStoreDiv.getData("last_sync_pemilihan")
            .map { it ?: "" }
    }
}