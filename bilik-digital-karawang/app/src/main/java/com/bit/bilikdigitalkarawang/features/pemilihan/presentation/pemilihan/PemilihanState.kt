package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.UserInfo

data class PemilihanState (
    val nik: String = "",
    val informasiPemilih: Pemilih? = null,
    val kandidatList: List<Kandidat> = emptyList(),
    val kandidatTerpilih: List<Kandidat> = emptyList(),
    val namaKandidatTerpilih: List<String> = emptyList(),
    val showConfirmation: Boolean = false,
    val voteStatus: CommonStatus = CommonStatus.Idle,
    val voteStatusMsg: String = "",
    val isCheckingNik: Boolean? = null,
    val checkingNikStatus: Boolean? = null,
    val checkingNikStatusMsg: String = "",
    val printStatus: CommonStatus = CommonStatus.Idle,
    val printStatusMsg: String = "",
    val userInfo: UserInfo? = null,
    val votingMethod: String = "QR Code" // <--- PROPERTY BARU
)