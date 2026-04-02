package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import javax.inject.Inject

class GetDetailPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    suspend operator fun invoke(nik: String): Pemilihan? {
        return repository.detailPemilihan(nik)
    }
}
