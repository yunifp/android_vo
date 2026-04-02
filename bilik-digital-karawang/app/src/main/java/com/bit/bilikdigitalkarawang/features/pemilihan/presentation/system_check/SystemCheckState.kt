package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.system_check

import com.bit.bilikdigitalkarawang.common.CommonStatus

data class SystemCheckState(
    val kandidatCheck: CommonStatus = CommonStatus.Idle,
    val kandidatCheckMsg: String = "",

    val pemilihCheck: CommonStatus = CommonStatus.Idle,
    val pemilihCheckMsg: String = "",

    val sdCardCheck: CommonStatus = CommonStatus.Idle,
    val sdCardCheckMsg: String = "",

    val backupPathCheck: CommonStatus = CommonStatus.Idle,
    val backupPathCheckMsg: String = "",

    val printerCheck: CommonStatus = CommonStatus.Idle,
    val printerCheckMsg: String = "",

    val votingMethod: String = "QR Code"
)