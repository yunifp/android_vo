package com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.usecase

import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.SavedPrinterInfo
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetSavedPrinterUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) {
    suspend operator fun invoke(): Result<SavedPrinterInfo> {
        val macAddress = dataStoreDiv.getData("selected_printer_mac").firstOrNull()
        val name = dataStoreDiv.getData("selected_printer_name").firstOrNull()

        if (macAddress.isNullOrEmpty() || name.isNullOrEmpty()) {
            return Result.failure(Exception("Data printer tidak lengkap di DataStore"))
        }

        return Result.success(SavedPrinterInfo(name = name, macAddress = macAddress))
    }
}