package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.SuaraSahEntity

fun SuaraSahEntity.toSuaraSah(): SuaraSah {
    return SuaraSah(
        nama = this.nama,
        noUrut = this.noUrut,
        localFoto = this.localFoto,
        jumlah = this.jumlah,
        jumlahLaki = this.jumlahLaki,
        jumlahPerempuan = this.jumlahPerempuan
    )
}