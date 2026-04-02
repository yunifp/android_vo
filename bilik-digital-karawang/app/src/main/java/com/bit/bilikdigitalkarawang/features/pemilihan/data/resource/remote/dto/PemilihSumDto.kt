package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class PemilihSumDto(
    @SerializedName("jumlah")
    val jumlah: Int = 0,
    @SerializedName("pemilih")
    val pemilihDto: List<PemilihDto> = listOf()
)