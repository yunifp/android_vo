package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang2

import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan

data class PrintUlang2State (
    val nik: String = "",
    val informasiPemilih: Pemilih? = null,
    val detailPemilihan: Pemilihan? = null,
    val isCheckingNik: Boolean? = null,
    val checkingNikStatus: Boolean? = null,
    val checkingNikStatusMsg: String = "",
    val isCheckingHasPrintUlang: Boolean? = false,
    val checkingHasPrintUlangStatus: Boolean? = false,
    val checkingHasPrintUlangMsg: String = "",
    val isPrinting: Boolean? = null,
    val printingStatus: Boolean? = null,
    val printingMsg: String = "",
    val showConfirmSelesaiPrintUlang: Boolean? = false,
    val selesaiPrintUlang: Boolean = false,
    val updatingHasPrintUlang: Boolean? = false,
    val updatingHasPrintUlangStatus: Boolean? = false,
    val updatingHasPrintUlangMsg: String = ""
)