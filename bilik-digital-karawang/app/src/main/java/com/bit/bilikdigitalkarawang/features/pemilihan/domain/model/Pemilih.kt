package com.bit.bilikdigitalkarawang.features.pemilihan.domain.model

data class Pemilih(
    val id: Int,
    val idDpt: String,
    val kodePro: String,
    val kodeKab: String,
    val kodeKec: String,
    val kodeKel: String,
    val namaPro: String,
    val namaKab: String,
    val namaKec: String,
    val namaDesa: String,
    val tpsNo: String,
    val nkk: String,
    val nik: String,
    val tempatLahir: String,
    val tanggalLahir: String,
    val namaPenduduk: String,
    val alamat: String?,
    val ketTms: String?,
    val statusDisabilitas: String?,
    val idJenisDisabilitas: String?,
    val jenisKelamin: String
)