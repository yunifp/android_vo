package com.bit.bilikdigitalkarawang.features.pemilihan.domain.model

data class Pemilihan(
    val id: Int = 0,
    val nik: String,
    val idDpt: String,
    val noUrut: String? = null,
    val namaKandidat: String? = null,
    val idStatus: Int,
    val jenisKelamin: String,
    val hasPrintUlang: Int
)
