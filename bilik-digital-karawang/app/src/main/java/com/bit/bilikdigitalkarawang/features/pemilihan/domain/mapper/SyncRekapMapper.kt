package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.SyncRekapRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRekap
import okhttp3.RequestBody.Companion.toRequestBody

fun SyncRekap.toRequest(): SyncRekapRequest {
    return SyncRekapRequest(
        jumlahPartisipasi = jumlahPartisipasi.toString().toRequestBody(),
        suaraSah = suaraSah.toString().toRequestBody(),
        suaraTidakSah = suaraTidakSah.toString().toRequestBody(),
        suaraAbstain = suaraAbstain.toString().toRequestBody(),
        suaraPerKandidat = suaraPerKandidat.map { it.toString().toRequestBody() }
    )
}