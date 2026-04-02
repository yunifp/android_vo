package com.bit.bilikdigitalkarawang.shared.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kandidat")
data class KandidatEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_kandidat")
    val idKandidat: String,

    @ColumnInfo(name = "kode_pro")
    val kodePro: String,

    @ColumnInfo(name = "kode_kab")
    val kodeKab: String,

    @ColumnInfo(name = "kode_kec")
    val kodeKec: String,

    @ColumnInfo(name = "kode_kel")
    val kodeKel: String,

    @ColumnInfo(name = "no_urut")
    val noUrut: String,

    @ColumnInfo(name = "nama_calon")
    val namaCalon: String,

    @ColumnInfo(name = "foto")
    val foto: String,

    @ColumnInfo(name = "local_foto")
    val localFoto: String
)