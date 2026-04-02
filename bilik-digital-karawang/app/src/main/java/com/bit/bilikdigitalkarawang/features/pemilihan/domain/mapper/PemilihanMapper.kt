package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.VoteDto
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.google.gson.Gson

fun PemilihanEntity.toPemilihan(): Pemilihan {
    return Pemilihan(
        id = id,
        nik = nik,
        idDpt = idDpt,
        noUrut = noUrut,
        namaKandidat = namaKandidat,
        idStatus = idStatus,
        jenisKelamin = jenisKelamin,
        hasPrintUlang = hasPrintUlang
    )
}

fun mapPemilihanToSyncFormat(pemilihanList: List<Pemilihan>): String {
    val mappedList = pemilihanList.map {
        mapOf(
            "id_dpt" to (it.idDpt ?: ""),
            "nik" to (it.nik ?: ""),
            "no_urut" to (it.noUrut ?: ""),
            "status" to (it.idStatus?.toString() ?: ""),
            "jenis_kelamin" to (it.jenisKelamin?.toString() ?: "")
        )
    }
    return Gson().toJson(mappedList)
}

fun VoteDto.toPemilihanEntity(): PemilihanEntity {
    return PemilihanEntity(
        nik = this.nik,
        idDpt = this.idDpt,
        noUrut = this.noUrut,
        idStatus = this.status.toInt(),
        namaKandidat = null,
        jenisKelamin = this.jenisKelamin,
        hasPrintUlang = 0
    )
}