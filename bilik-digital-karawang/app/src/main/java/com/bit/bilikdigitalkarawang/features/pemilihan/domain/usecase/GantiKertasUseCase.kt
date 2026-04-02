package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import javax.inject.Inject

class GantiKertasUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) {
    suspend operator fun invoke() {
        dataStoreDiv.saveData("sudah_ganti_kertas", "Y")
    }
}

