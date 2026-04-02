package com.bit.bilikdigitalkarawang.features.setting.presentation

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah

data class SettingState (
    val listSuaraSah: List<SuaraSah> = emptyList(),
    val jumlahSah: Int = 0,
    val jumlahTidakSah: Int = 0,
    val jumlahAbstain: Int = 0,
    val jumlahPemilihan: Int = 0,
    val jumlahPemilih: Int = 0,
    val isLoading: Boolean = false,
    val error: String = "",

    val showConfirmSyncRekap: Boolean = false,
    val isSyncingRekap: Boolean = false,
    val statusSyncRekap: CommonStatus? = null,
    val statusSyncMessageRekap: String = "",
    val showConfirmDownloadRekap: Boolean = false,
    val isDownloadingListVote: Boolean = false,
    val statusDownloadListVote: CommonStatus? = null,
    val statusMessageDownloadListVote: String = "",
    val showConfirmSyncRow: Boolean = false,
    val isSyncingRow: Boolean = false,
    val statusSyncRow: CommonStatus? = null,
    val statusSyncMessageRow: String = "",
    val lastSyncPemilihan: String = "",
    val isResetingPemilihan: Boolean = false,
    val statusResetPemilihan: CommonStatus? = null,
    val statusMessageResetPemilihan: String = "",
    val showConfirmResetPemilihan: Boolean = false,

    val exportLocation: String = "",
    val isSavingExportLocation: Boolean = false,
    val statusSavingExportLocation: CommonStatus? = null,
    val saveExportLocationMsg: String = "",
    val isRestoring: Boolean = false,
    val statusRestoring: CommonStatus? = null,
    val restoringMsg: String = ""
)