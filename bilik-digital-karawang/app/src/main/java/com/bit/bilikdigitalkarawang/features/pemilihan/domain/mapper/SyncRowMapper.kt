package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.SyncRowRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRow
import okhttp3.RequestBody.Companion.toRequestBody

fun SyncRow.toRequest(): SyncRowRequest {
    return SyncRowRequest(
        votes = votes.toRequestBody()
    )
}