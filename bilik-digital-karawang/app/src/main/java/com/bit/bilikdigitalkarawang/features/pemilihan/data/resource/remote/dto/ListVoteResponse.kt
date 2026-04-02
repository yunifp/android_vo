package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class ListVoteResponse(
    @SerializedName("data")
    val listVoteDto: ListVoteDto = ListVoteDto(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)