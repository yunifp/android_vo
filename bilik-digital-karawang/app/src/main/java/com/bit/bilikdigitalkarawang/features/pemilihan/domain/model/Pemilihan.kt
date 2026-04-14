package com.bit.bilikdigitalkarawang.features.pemilihan.domain.model

data class Pemilihan(
    val id: Int = 0,
    val nik: String,
    val idDpt: String,
    val heVotesMap: String, // Sesuaikan dengan Entity
    val idStatus: Int,
    val jenisKelamin: String,
    val hasPrintUlang: Int
)