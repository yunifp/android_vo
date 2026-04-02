package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.kandidat

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat

data class KandidatState(
    val kandidatList: List<Kandidat> = emptyList(),
    val downloadStatus: CommonStatus = CommonStatus.Idle,
    val downloadStatusMsg: String = "",
    val showAlert: Boolean = false,
    val isInitialLoading: Boolean = true
)
