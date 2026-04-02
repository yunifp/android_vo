package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap

import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.UserInfo

data class RekapState(
    val listSuaraSah: List<SuaraSah> = emptyList(),
    val jumlahSahLaki: Int = 0,
    val jumlahSahPerempuan: Int = 0,
    val jumlahTidakSahLaki: Int = 0,
    val jumlahTidakSahPerempuan: Int = 0,
    val jumlahAbstainLaki: Int = 0,
    val jumlahAbstainPerempuan: Int = 0,
    val jumlahPemilihan: Int = 0,
    val jumlahPemilih: Int = 0,
    val isLoading: Boolean = false,
    val error: String = "",
    val userInfo: UserInfo? = null,
)