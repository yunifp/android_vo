package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih

data class PemilihState(
    val pemilihList: List<Pemilih> = emptyList(),
    val downloadStatus: CommonStatus = CommonStatus.Idle,
    val downloadStatusMsg: String = "",
    val showAlert: Boolean = false,
    val searchQuery: String = "",
    val lastTimeGetData: String = "",
    val isInitialLoading: Boolean = true
)