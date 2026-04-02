package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRowDto(
    @SerialName("id_dpt")
    val idDpt: String = "",
    @SerialName("id_tps")
    val idTps: String = "",
    @SerialName("kode_kab")
    val kodeKab: String = "",
    @SerialName("kode_kec")
    val kodeKec: String = "",
    @SerialName("kode_kel")
    val kodeKel: String = "",
    @SerialName("kode_pro")
    val kodePro: String = "",
    @SerialName("nik")
    val nik: String = "",
    @SerialName("no_urut")
    val noUrut: String = "",
    @SerialName("status")
    val status: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)