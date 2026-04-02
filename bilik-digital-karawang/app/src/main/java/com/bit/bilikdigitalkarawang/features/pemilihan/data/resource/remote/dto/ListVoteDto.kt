package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class ListVoteDto(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("votes")
    val voteDtos: List<VoteDto> = listOf()
)