package com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("data")
    val dataDto: DataDto = DataDto(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)