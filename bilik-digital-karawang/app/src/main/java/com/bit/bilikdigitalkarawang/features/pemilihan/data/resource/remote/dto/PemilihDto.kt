package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class PemilihDto(
    @SerializedName("alamat")
    val alamat: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("id_jenis_disabilitas")
    val idJenisDisabilitas: String = "",
    @SerializedName("ket_tms")
    val ketTms: String = "",
    @SerializedName("kode_kab")
    val kodeKab: String = "",
    @SerializedName("kode_kec")
    val kodeKec: String = "",
    @SerializedName("kode_kel")
    val kodeKel: String = "",
    @SerializedName("kode_pro")
    val kodePro: String = "",
    @SerializedName("nama_desa")
    val namaDesa: String = "",
    @SerializedName("nama_kab")
    val namaKab: String = "",
    @SerializedName("nama_kec")
    val namaKec: String = "",
    @SerializedName("nama_penduduk")
    val namaPenduduk: String = "",
    @SerializedName("nama_pro")
    val namaPro: String = "",
    @SerializedName("nik")
    val nik: String = "",
    @SerializedName("nkk")
    val nkk: String = "",
    @SerializedName("status_disabilitas")
    val statusDisabilitas: String = "",
    @SerializedName("tanggal_lahir")
    val tanggalLahir: String = "",
    @SerializedName("tempat_lahir")
    val tempatLahir: String = "",
    @SerializedName("tps_no")
    val tpsNo: String = "",
    @SerializedName("id_tps")
    val idTps: String = "",
    @SerializedName("jenis_kelamin")
    val jenisKelamin: String = "",
)