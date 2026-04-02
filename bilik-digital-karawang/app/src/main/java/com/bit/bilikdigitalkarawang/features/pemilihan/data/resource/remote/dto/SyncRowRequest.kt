package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto

import okhttp3.RequestBody

data class SyncRowRequest (
    val votes: RequestBody,
)