package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PemilihDto
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihEntity

fun PemilihEntity.toPemilih(): Pemilih {
    return Pemilih(
        id = this.id,
        idDpt = this.idDpt,
        kodePro = this.kodePro,
        kodeKab = this.kodeKab,
        kodeKec = this.kodeKec,
        kodeKel = this.kodeKel,
        namaPro = this.namaPro,
        namaKab = this.namaKab,
        namaKec = this.namaKec,
        namaDesa = this.namaDesa,
        tpsNo = this.tpsNo,
        nkk = this.nkk,
        nik = this.nik,
        tempatLahir = this.tempatLahir,
        tanggalLahir = this.tanggalLahir,
        namaPenduduk = this.namaPenduduk,
        alamat = this.alamat,
        ketTms = this.ketTms,
        statusDisabilitas = this.statusDisabilitas,
        idJenisDisabilitas = this.idJenisDisabilitas,
        jenisKelamin = this.jenisKelamin
    )
}

fun PemilihDto.toEntity(): PemilihEntity {
    return PemilihEntity(
        idDpt = this.id,
        kodePro = this.kodePro,
        kodeKab = this.kodeKab,
        kodeKec = this.kodeKec,
        kodeKel = this.kodeKel,
        namaPro = this.namaPro,
        namaKab = this.namaKab,
        namaKec = this.namaKec,
        namaDesa = this.namaDesa,
        tpsNo = this.tpsNo,
        nkk = this.nkk,
        nik = this.nik,
        tempatLahir = this.tempatLahir,
        tanggalLahir = this.tanggalLahir,
        namaPenduduk = this.namaPenduduk,
        alamat = this.alamat,
        ketTms = this.ketTms,
        statusDisabilitas = this.statusDisabilitas,
        idJenisDisabilitas = this.idJenisDisabilitas,
        jenisKelamin = this.jenisKelamin
    )
}