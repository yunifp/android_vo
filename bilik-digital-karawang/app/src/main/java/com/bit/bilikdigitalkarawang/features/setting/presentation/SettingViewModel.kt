package com.bit.bilikdigitalkarawang.features.setting.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRekap
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.DownloadListVoteUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetJumlahPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetLastSyncPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraAbstainUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraPerKandidatUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraSahUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraTidakSahUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetTotalPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.ImportPemilihanFromJsonUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.ResetPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.SyncRekapUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.SyncRowUseCase
import com.bit.bilikdigitalkarawang.features.setting.domain.GetExportLocationUseCase
import com.bit.bilikdigitalkarawang.features.setting.domain.SaveExportLocationUseCase
import com.bit.bilikdigitalkarawang.features.setting.domain.GetVotingMethodUseCase
import com.bit.bilikdigitalkarawang.features.setting.domain.SaveVotingMethodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val getSuaraPerKandidatUseCase: GetSuaraPerKandidatUseCase,
    private val getJumlahPemilihUseCase: GetJumlahPemilihUseCase,
    private val getJumlahPemilihanUseCase: GetTotalPemilihanUseCase,
    private val getSuaraSahUseCase: GetSuaraSahUseCase,
    private val getSuaraTidakSahUseCase: GetSuaraTidakSahUseCase,
    private val getSuaraAbstainUseCase: GetSuaraAbstainUseCase,
    private val syncRekapUseCase: SyncRekapUseCase,
    private val syncRowUseCase: SyncRowUseCase,
    private val getLastSyncPemilihanUseCase: GetLastSyncPemilihanUseCase,
    private val downloadListVoteUseCase: DownloadListVoteUseCase,
    private val resetPemilihanUseCase: ResetPemilihanUseCase,

    private val getExportLocationUseCase: GetExportLocationUseCase,
    private val saveExportLocationUseCase: SaveExportLocationUseCase,
    private val importPemilihanFromJsonUseCase: ImportPemilihanFromJsonUseCase,
    private val getVotingMethodUseCase: GetVotingMethodUseCase,
    private val saveVotingMethodUseCase: SaveVotingMethodUseCase
): ViewModel() {

    private val _state = MutableStateFlow(SettingState())
    val state: StateFlow<SettingState> = _state.asStateFlow()

    init {
        getJumlahPemilih()
        getJumlahPemilihan()
        getSuaraPerKandidat()
        getJumlahSah()
        getJumlahTidakSah()
        getJumlahAbstain()
        getLastSyncPemilihan()
        getExportLocation()
        getVotingMethod()
    }

    fun getJumlahPemilih() {
        getJumlahPemilihUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahPemilih = result.data ?: 0
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Gagal mengambil jumlah suara tidak sah"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getJumlahPemilihan() {
        getJumlahPemilihanUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahPemilihan = result.data ?: 0
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Gagal mengambil jumlah suara tidak sah"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getSuaraPerKandidat() {
        getSuaraPerKandidatUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = "")
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        listSuaraSah = result.data ?: emptyList(),
                        isLoading = false,
                        error = ""
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Terjadi kesalahan tak dikenal"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getJumlahSah() {
        getSuaraSahUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahSah = result.data ?: 0
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Gagal mengambil jumlah suara tidak sah"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getJumlahTidakSah() {
        getSuaraTidakSahUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahTidakSah = result.data ?: 0
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Gagal mengambil jumlah suara tidak sah"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getJumlahAbstain() {
        getSuaraAbstainUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahAbstain = result.data ?: 0
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Gagal mengambil jumlah suara abstain"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun syncRekap() {
        _state.update { it.copy(showConfirmSyncRekap = false) }
        val currentState = _state.value

        val suaraPerKandidat = currentState.listSuaraSah
            .sortedBy { it.noUrut }
            .map { it.jumlah }

        val syncRekap = SyncRekap(
            jumlahPartisipasi = currentState.jumlahPemilihan,
            suaraSah = currentState.jumlahSah,
            suaraTidakSah = currentState.jumlahTidakSah,
            suaraAbstain = currentState.jumlahAbstain,
            suaraPerKandidat = suaraPerKandidat
        )
        syncRekapUseCase(syncRekap).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isSyncingRekap = true, statusSyncMessageRekap = "Data pemilihan berhasil disinkronkan ke server")
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(isSyncingRekap = false, statusSyncRekap = CommonStatus.Success)
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(isSyncingRekap = false, statusSyncRekap = CommonStatus.Error, statusSyncMessageRekap = result.message ?: "Gagal menyinkronkan data kandidat. Periksa koneksi internet atau coba lagi nanti")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun syncRow() {
        _state.update { it.copy(showConfirmSyncRow = false) }

        syncRowUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isSyncingRow = true, statusSyncMessageRow = "Data pemilihan berhasil disinkronkan ke server")
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(isSyncingRow = false, statusSyncRow = CommonStatus.Success)
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(isSyncingRow = false, statusSyncRow = CommonStatus.Error, statusSyncMessageRow = result.message ?: "Gagal menyinkronkan data kandidat. Periksa koneksi internet atau coba lagi nanti")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun downloadListVote() {
        _state.update { it.copy(showConfirmDownloadRekap = false) }
        downloadListVoteUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isDownloadingListVote = true, statusMessageDownloadListVote = "Data pemilihan sebelumnya berhasil disimpan")
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(isDownloadingListVote = false, statusDownloadListVote = CommonStatus.Success)
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(isDownloadingListVote = false, statusDownloadListVote = CommonStatus.Error, statusMessageDownloadListVote = result.message ?: "Gagal menyinkronkan data kandidat. Periksa koneksi internet atau coba lagi nanti")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetPemilihan(pinPanitia: String, pinPengembang: String) {
        _state.update { it.copy(showConfirmResetPemilihan = false) }

        resetPemilihanUseCase(
            pinPanitia = pinPanitia,
            pinPengembang = pinPengembang
        ).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(
                        isResetingPemilihan = true,
                        statusMessageResetPemilihan = "Menghapus data..."
                    )
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isResetingPemilihan = false,
                        statusResetPemilihan = CommonStatus.Success,
                        statusMessageResetPemilihan = "Berhasil menghapus data"
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isResetingPemilihan = false,
                        statusResetPemilihan = CommonStatus.Error,
                        statusMessageResetPemilihan = result.message
                            ?: "Gagal menghapus data. Periksa koneksi internet atau coba lagi nanti."
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getLastSyncPemilihan() {
        viewModelScope.launch {
            getLastSyncPemilihanUseCase().collect { lastTime ->
                _state.update { it.copy(lastSyncPemilihan = lastTime) }
            }
        }
    }

    fun toggleConfirmSyncRekap(value: Boolean) {
        _state.update { it.copy(showConfirmSyncRekap = value) }
    }

    fun toggleConfirmSyncRow(value: Boolean) {
        _state.update { it.copy(showConfirmSyncRow = value) }
    }

    fun toggleConfirmDownloadListVote(value: Boolean) {
        _state.update { it.copy(showConfirmDownloadRekap = value) }
    }

    fun toggleConfirmResetPemilihan(value: Boolean) {
        _state.update { it.copy(showConfirmResetPemilihan = value) }
    }

    fun hideAlert() {
        _state.update { it.copy(statusSyncRekap = null, statusSyncRow = null, statusDownloadListVote = null, statusResetPemilihan = null, statusSavingExportLocation = null,  statusRestoring = null, statusSavingVotingMethod = null ) }
    }

    fun getExportLocation() {
        getExportLocationUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(
                        exportLocation = "Memuat..."
                    )
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        exportLocation = result.data ?: ""
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        exportLocation = result.message ?: "Gagal mendapatkan path"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onSelectLocation(uri: Uri) {
        saveExportLocationUseCase(uri).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(
                        isSavingExportLocation = true
                    )
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isSavingExportLocation = false,
                        statusSavingExportLocation = CommonStatus.Success,
                        saveExportLocationMsg = result.message ?: "Berhasil menyimpan path",
                        exportLocation = uri.toString()
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isSavingExportLocation = false,
                        statusSavingExportLocation = CommonStatus.Error,
                        saveExportLocationMsg = result.message ?: "Gagal menyimpan path"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            importPemilihanFromJsonUseCase(uri).collect { result ->
                when(result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isRestoring = true
                        )
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            statusRestoring = CommonStatus.Success,
                            restoringMsg = result.message ?: "Berhasil restoring"
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            statusRestoring = CommonStatus.Error,
                            restoringMsg = result.message ?: "Gagal restoring"
                        )
                    }
                }
            }
        }
    }

    fun getVotingMethod() {
        getVotingMethodUseCase().onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(
                    votingMethod = result.data ?: "QR Code"
                )
            }
        }.launchIn(viewModelScope)
    }

    fun saveVotingMethod(method: String) {
        saveVotingMethodUseCase(method).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        votingMethod = method,
                        statusSavingVotingMethod = CommonStatus.Success,
                        saveVotingMethodMsg = "Metode pemilihan berhasil diubah"
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        statusSavingVotingMethod = CommonStatus.Error,
                        saveVotingMethodMsg = result.message ?: "Gagal mengubah metode"
                    )
                }
                else -> {}
            }
        }.launchIn(viewModelScope)


    }

}