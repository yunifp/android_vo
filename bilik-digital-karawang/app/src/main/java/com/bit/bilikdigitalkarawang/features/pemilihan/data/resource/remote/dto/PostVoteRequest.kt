package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto

data class PostVoteRequest (
    val idDpt: String,
    val nik: String,
    val noUrut: String?,
    val idStatus: Int,
)