package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class KandidatDto(
    @SerializedName("foto")
    val foto: String = "",
    @SerializedName("id_kandidat")
    val idKandidat: String = "",
    @SerializedName("kode_kab")
    val kodeKab: String = "",
    @SerializedName("kode_kec")
    val kodeKec: String = "",
    @SerializedName("kode_kel")
    val kodeKel: String = "",
    @SerializedName("kode_pro")
    val kodePro: String = "",
    @SerializedName("nama_calon")
    val namaCalon: String = "",
    @SerializedName("no_urut")
    val noUrut: String = ""
)