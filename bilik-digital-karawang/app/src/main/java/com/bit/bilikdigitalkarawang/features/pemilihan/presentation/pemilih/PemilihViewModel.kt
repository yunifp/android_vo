package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.DownloadPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetLastTimeDownloadPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetPemilihUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PemilihViewModel @Inject constructor(
    private val downloadPemilihUseCase: DownloadPemilihUseCase,
    private val getPemilihUseCase: GetPemilihUseCase,
    private val getLastTimeDownloadPemilihUseCase: GetLastTimeDownloadPemilihUseCase
): ViewModel() {

    private val _state = MutableStateFlow(PemilihState())
    val state: StateFlow<PemilihState> = _state

    init {
        getPemilihList()
        getLastDownloadTime()
    }

    fun download() {
        downloadPemilihUseCase().onEach { result ->
            when (result) {
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        showAlert = true,
                        downloadStatus = CommonStatus.Error,
                        downloadStatusMsg = result.message
                            ?: "Terjadi kesalahan saat mengunduh data pemilih. Silakan coba lagi nanti."
                    )
                }

                is Resource.Loading -> {
                    _state.value = _state.value.copy(
                        downloadStatus = CommonStatus.Loading,
                    )
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        showAlert = true,
                        downloadStatus = CommonStatus.Success,
                        downloadStatusMsg = "Data pemilih berhasil diunduh dan diperbarui."
                    )

                    getPemilihList()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getPemilihList() {
        getPemilihUseCase().onEach { result ->
            when(result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        pemilihList = result.data ?: emptyList(),
                        isInitialLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getLastDownloadTime() {
        viewModelScope.launch {
            getLastTimeDownloadPemilihUseCase().collect { lastTime ->
                _state.update { it.copy(lastTimeGetData = lastTime) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun hideAlert() {
        _state.update { it.copy(showAlert = false) }
    }

}