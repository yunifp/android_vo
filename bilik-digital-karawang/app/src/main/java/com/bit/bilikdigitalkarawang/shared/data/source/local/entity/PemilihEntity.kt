package com.bit.bilikdigitalkarawang.shared.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pemilih")
data class PemilihEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_dpt")
    val idDpt: String,

    @ColumnInfo(name = "kode_pro")
    val kodePro: String,

    @ColumnInfo(name = "kode_kab")
    val kodeKab: String,

    @ColumnInfo(name = "kode_kec")
    val kodeKec: String,

    @ColumnInfo(name = "kode_kel")
    val kodeKel: String,

    @ColumnInfo(name = "nama_pro")
    val namaPro: String,

    @ColumnInfo(name = "nama_kab")
    val namaKab: String,

    @ColumnInfo(name = "nama_kec")
    val namaKec: String,

    @ColumnInfo(name = "nama_desa")
    val namaDesa: String,

    @ColumnInfo(name = "tps_no")
    val tpsNo: String,

    @ColumnInfo(name = "nkk")
    val nkk: String,

    @ColumnInfo(name = "nik")
    val nik: String,

    @ColumnInfo(name = "tempat_lahir")
    val tempatLahir: String,

    @ColumnInfo(name = "tanggal_lahir")
    val tanggalLahir: String,

    @ColumnInfo(name = "nama_penduduk")
    val namaPenduduk: String,

    @ColumnInfo(name = "alamat")
    val alamat: String?,

    @ColumnInfo(name = "ket_tms")
    val ketTms: String?,

    @ColumnInfo(name = "status_disabilitas")
    val statusDisabilitas: String?,

    @ColumnInfo(name = "id_jenis_disabilitas")
    val idJenisDisabilitas: String?,

    @ColumnInfo(name = "jenis_kelamin")
    val jenisKelamin: String,
)