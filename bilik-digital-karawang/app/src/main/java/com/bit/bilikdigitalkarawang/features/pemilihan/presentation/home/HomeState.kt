package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home

import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.UserInfo

data class HomeState (
    val userInfo: UserInfo? = null,
    val sudahGantiKertas: String? = null,
    val showAlertGantiKertas: Boolean = false,
    val showConfirmGantiKertas: Boolean = false,
    val hasShownShowcase: Boolean = true,
    val jumlahPemilihan: Int = 0,
    val confirmBukaRekap: Boolean = false,
    val showAlertTidakBisaBukaRekap: Boolean = false
)