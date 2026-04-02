package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class PostVoteResponse(
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)