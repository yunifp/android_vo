package com.bit.bilikdigitalkarawang.features.pemilihan.domain.model

data class SyncRekap(
    val jumlahPartisipasi: Int,
    val suaraSah: Int,
    val suaraTidakSah: Int,
    val suaraAbstain: Int,
    val suaraPerKandidat: List<Int>
)