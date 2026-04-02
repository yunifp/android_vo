package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto

import okhttp3.RequestBody

data class SyncRekapRequest (
    val jumlahPartisipasi: RequestBody,
    val suaraSah: RequestBody,
    val suaraTidakSah: RequestBody,
    val suaraAbstain: RequestBody,
    val suaraPerKandidat: List<RequestBody>
)