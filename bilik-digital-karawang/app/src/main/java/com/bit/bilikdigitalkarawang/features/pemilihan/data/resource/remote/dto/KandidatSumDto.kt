package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class KandidatSumDto(
    @SerializedName("kandidats")
    val kandidatDtos: List<KandidatDto> = listOf()
)