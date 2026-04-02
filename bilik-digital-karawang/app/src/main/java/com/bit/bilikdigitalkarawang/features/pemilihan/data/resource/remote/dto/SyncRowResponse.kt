package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRowResponse(
    @SerialName("data")
    val syncRowDto: SyncRowDto = SyncRowDto(),
    @SerialName("message")
    val message: String = "",
    @SerialName("success")
    val success: Boolean = false
)