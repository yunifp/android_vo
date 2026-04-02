package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class PemilihResponse(
    @SerializedName("data")
    val pemilihSumDto: PemilihSumDto = PemilihSumDto(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)