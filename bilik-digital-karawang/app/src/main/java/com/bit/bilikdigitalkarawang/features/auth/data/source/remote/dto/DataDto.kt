package com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto


import com.google.gson.annotations.SerializedName

data class DataDto(
    @SerializedName("token")
    val token: String = "",
    @SerializedName("user")
    val userDto: UserDto = UserDto()
)