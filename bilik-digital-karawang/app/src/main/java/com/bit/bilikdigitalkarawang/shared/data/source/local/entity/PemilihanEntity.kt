package com.bit.bilikdigitalkarawang.shared.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pemilihan")
data class PemilihanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idDpt: String,
    val nik: String,
    val noUrut: String?,
    val namaKandidat: String?,
    val idStatus: Int,
    val jenisKelamin: String,
    val hasPrintUlang: Int = 0
)
