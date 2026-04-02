package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.PrintMode
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CekHasPrintUlangUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CekHasVotedUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetDetailPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetDetailPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.PrintHasilVotingUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.UpdateHasPrintUlangUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrintUlangViewModel @Inject constructor(
    private val printHasilVotingUseCase: PrintHasilVotingUseCase,
    private val cekHasVotedUseCase: CekHasVotedUseCase,
    private val getDetailPemilihanUseCase: GetDetailPemilihanUseCase,
    private val getDetailPemilihUseCase: GetDetailPemilihUseCase,
    private val cekHasPrintUlangUseCase: CekHasPrintUlangUseCase,
    private val updateHasPrintUlangUseCase: UpdateHasPrintUlangUseCase
): ViewModel() {
    private val _state = MutableStateFlow(PrintUlangState())
    val state: StateFlow<PrintUlangState> = _state

    fun setNik(nik: String) {
        _state.update { it.copy(nik = nik) }
        checkingNik()
    }

    fun checkingHasPrintUlang() {
        cekHasPrintUlangUseCase(_state.value.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isCheckingHasPrintUlang = true) }
                }
                is Resource.Success -> {
                    Log.d("ASDASD", result.data.toString())
                    val hasPrintUlang: Boolean = result.data == true

                    _state.update {
                        it.copy(
                            checkingHasPrintUlangStatus = hasPrintUlang,
                            isCheckingHasPrintUlang = false,
                            checkingHasPrintUlangMsg = result.message ?: "Sudah melakukan cetak ulang"
                        )
                    }

                    if (!hasPrintUlang) {
                        loadDetailPemilihan()
                        getDetailPemilih()
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            checkingHasPrintUlangStatus = false,
                            checkingHasPrintUlangMsg = result.message ?: "Terjadi Kesalahan",
                            isCheckingHasPrintUlang = false,
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun checkingNik() {
        cekHasVotedUseCase(_state.value.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isCheckingNik = true) }
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            checkingNikStatus = result.data,
                            isCheckingNik = false,
                            checkingNikStatusMsg = result.message ?: "NIK tersebut belum melakukan pemilihan"
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            checkingNikStatus = false,
                            checkingNikStatusMsg = result.message ?: "Terjadi Kesalahan",
                            isCheckingNik = false,
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getDetailPemilih() {
        getDetailPemilihUseCase(_state.value.nik).onEach { result ->
            when (result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        informasiPemilih = result.data
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun loadDetailPemilihan() {
        viewModelScope.launch {
            val result = getDetailPemilihanUseCase(_state.value.nik)
            _state.update { it.copy(detailPemilihan = result) }
        }
    }

    fun print() {
        viewModelScope.launch {

            // 🔄 START PRINTING
            _state.update {
                it.copy(
                    isPrinting = true,
                    printingStatus = null,
                    printingMsg = ""
                )
            }

            val detail = _state.value.detailPemilihan
            if (detail == null) {
                _state.update {
                    it.copy(
                        isPrinting = false,
                        printingStatus = false,
                        printingMsg = "Data pemilihan tidak valid"
                    )
                }
                return@launch
            }

            val result = printHasilVotingUseCase(
                printMode = PrintMode.RESI_CETAK_ULANG,
                idStatus = detail.idStatus,
                noUrut = detail.noUrut,
                namaKandidat = detail.namaKandidat
            )

            if(result.isSuccess) {
                _state.update {
                    it.copy(
                        isPrinting = false,
                        printingStatus = true,
                        printingMsg = "Berhasil mencetak ulang"
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isPrinting = false,
                        printingStatus = false,
                        printingMsg = "Gagal mencetak ulang"
                    )
                }
            }
        }
    }

    fun selesaiPrintUlang() {
        updateHasPrintUlangUseCase(_state.value.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(updatingHasPrintUlangStatus = true) }
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            updatingHasPrintUlang = result.data == CommonStatus.Success,
                            updatingHasPrintUlangStatus = true,
                            updatingHasPrintUlangMsg = result.message ?: "Berhasil updating flag hasPrintUlang"
                        )
                    }

                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            updatingHasPrintUlang = false,
                            updatingHasPrintUlangMsg = result.message ?: "Terjadi Kesalahan",
                            updatingHasPrintUlangStatus = false,
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleSelesaiPrintUlang(value: Boolean) {
        _state.update { it.copy(showConfirmSelesaiPrintUlang = value) }
    }

    fun resetPrintStatus(){
        _state.update { it.copy(printingStatus = null) }
    }

}