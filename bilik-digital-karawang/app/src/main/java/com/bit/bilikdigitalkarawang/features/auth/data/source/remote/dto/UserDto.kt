package com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto


import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("kode_kab")
    val kodeKab: String = "",
    @SerializedName("kode_kec")
    val kodeKec: String = "",
    @SerializedName("kode_kel")
    val kodeKel: String = "",
    @SerializedName("kode_pro")
    val kodePro: String = "",
    @SerializedName("nama_lengkap")
    val nama: String = "",
    @SerializedName("nama_pemilihan")
    val namaPemilihan: String = "",
    @SerializedName("nama_kab")
    val namaKab: String = "",
    @SerializedName("nama_kec")
    val namaKec: String = "",
    @SerializedName("nama_kel")
    val namaKel: String = "",
    @SerializedName("nama_pro")
    val namaPro: String = "",
    @SerializedName("user_id")
    val userId: String = "",
    @SerializedName("id_tps")
    val idTps: String = "",
    @SerializedName("tps_no")
    val tpsNo: String = "",
    @SerializedName("bilik_no")
    val bilikNo: String = "",
)