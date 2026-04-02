package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.KandidatDto
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.KandidatEntity

fun KandidatEntity.toKandidat(): Kandidat {
    return Kandidat(
        idKandidat = this.idKandidat,
        kodePro = this.kodePro,
        kodeKab = this.kodeKab,
        kodeKec = this.kodeKec,
        kodeKel = this.kodeKel,
        noUrut = this.noUrut,
        namaCalon = this.namaCalon,
        foto = this.foto,
        localFoto = this.localFoto
    )
}

fun KandidatDto.toEntity(localFoto: String): KandidatEntity {
    return KandidatEntity(
        idKandidat = this.idKandidat,
        kodePro = this.kodePro,
        kodeKab = this.kodeKab,
        kodeKec = this.kodeKec,
        kodeKel = this.kodeKel,
        noUrut = this.noUrut,
        namaCalon = this.namaCalon,
        foto = this.foto,
        localFoto = localFoto
    )
}