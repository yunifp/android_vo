package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class SyncRekapResponse(
    @SerializedName("data")
    val syncRekapDto: SyncRekapDto = SyncRekapDto(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)