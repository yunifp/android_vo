package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class VoteDto(
    @SerializedName("id_dpt")
    val idDpt: String = "",
    @SerializedName("nik")
    val nik: String = "",
    @SerializedName("no_urut")
    val noUrut: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("jenis_kelamin")
    val jenisKelamin: String = ""
)