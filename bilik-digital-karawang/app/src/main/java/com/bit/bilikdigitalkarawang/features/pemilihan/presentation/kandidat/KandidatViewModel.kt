package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.kandidat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.DownloadKandidatUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetKandidatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class KandidatViewModel @Inject constructor(
    private val downloadKandidatUseCase: DownloadKandidatUseCase,
    private val getKandidatUseCase: GetKandidatUseCase
): ViewModel() {

    private val _state = MutableStateFlow(KandidatState())
    val state: StateFlow<KandidatState> = _state

    init {
        getKandidatList()
    }

    fun download() {
        downloadKandidatUseCase().onEach { result ->
            when (result) {
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        showAlert = true,
                        downloadStatus = CommonStatus.Error,
                        downloadStatusMsg = result.message
                            ?: "Terjadi kesalahan saat mengunduh data kandidat. Silakan coba lagi nanti."
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
                        downloadStatusMsg = "Data kandidat berhasil diunduh dan diperbarui."
                    )

                    getKandidatList()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getKandidatList() {
        getKandidatUseCase().onEach { result ->
            when(result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        kandidatList = result.data ?: emptyList(),
                        isInitialLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun hideAlert() {
        _state.update { it.copy(showAlert = false) }
    }

}