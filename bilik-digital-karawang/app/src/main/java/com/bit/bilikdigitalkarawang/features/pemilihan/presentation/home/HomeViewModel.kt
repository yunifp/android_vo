package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GantiKertasUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetHasShownShowcaseUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSudahGantiKertasUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetTotalPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetUserInfoUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.UpdateHasShownShowUseCase
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.features.setting.domain.GetVotingMethodUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CheckConnectionUseCase
import kotlinx.coroutines.delay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val sudahGantiKertasUseCase: GetSudahGantiKertasUseCase,
    private val gantiKertasUseCase: GantiKertasUseCase,
    private val getHasShownShowcaseUseCase: GetHasShownShowcaseUseCase,
    private val updateHasShownShowUseCase: UpdateHasShownShowUseCase,
    private val getTotalPemilihanUseCase: GetTotalPemilihanUseCase,
    private val dataStoreDiv: DataStoreDiv,
    private val getVotingMethodUseCase: GetVotingMethodUseCase,
    private val checkConnectionUseCase: CheckConnectionUseCase
): ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        checkShowcaseStatus()
        getUserInfo()
        getVotingMethod()
        startConnectionObserver()
    }

    private fun getVotingMethod() {
        getVotingMethodUseCase().onEach { result ->
            if (result is Resource.Success) {
                _state.update { it.copy(votingMethod = result.data ?: "QR Code") }
            }
        }.launchIn(viewModelScope)
    }
    fun checkShowcaseStatus() {
        getHasShownShowcaseUseCase().onEach { value ->
            val alreadyShown = value == "Y"
            _state.update { it.copy(hasShownShowcase = alreadyShown) }
        }.launchIn(viewModelScope)
    }

    fun markShowcaseAsShown() {
        viewModelScope.launch {
            updateHasShownShowUseCase()
            _state.update { it.copy(hasShownShowcase = true) }
        }
    }

    fun getUserInfo() {
        getUserInfoUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> _state.update {
                    it.copy(
                        userInfo = result.data,
                    )
                }

                is Resource.Error -> {}

                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun getSudahGantiKertas() {
        sudahGantiKertasUseCase().onEach { value ->
            _state.update { it.copy(sudahGantiKertas = value) }
        }.launchIn(viewModelScope)
    }

    fun setSudahGantiKertas() {
        viewModelScope.launch {
            gantiKertasUseCase()
            hideAlert()
        }
    }

    fun getTotalPemilihan() {
        getTotalPemilihanUseCase().onEach { result ->
            when(result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.update {
                        it.copy(jumlahPemilihan = result.data ?: 0)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun alertGantiKertas() {
        _state.update { it.copy(showAlertGantiKertas = true) }
    }

    fun confirmGantiKertas() {
        _state.update { it.copy(showConfirmGantiKertas = true) }
    }

    fun confirmBukaRekap() {
        _state.update { it.copy(confirmBukaRekap = true) }
    }

    fun alertBukaRekap() {
        _state.update { it.copy(showAlertTidakBisaBukaRekap = true) }
    }

    suspend fun bukaRekap(pinPanitia: String, pinPengembang: String): Boolean {
        _state.update { it.copy(confirmBukaRekap = false) }
        val correctPinPanitia = dataStoreDiv.getData("user_pin").first()
        val correctPinPengembang = "1212"

        return pinPanitia == correctPinPanitia && pinPengembang == correctPinPengembang
    }

    fun hideAlert() {
        _state.update { it.copy(showAlertGantiKertas = false, showConfirmGantiKertas = false, showAlertTidakBisaBukaRekap = false, confirmBukaRekap = false) }
    }
    private fun startConnectionObserver() {
        viewModelScope.launch {
            while (true) {
                // Hanya periksa koneksi secara intensif jika metodenya Face/Fingerprint (Mode Online)
                val isOnlineMode = _state.value.votingMethod != "QR Code"

                if (isOnlineMode) {
                    checkConnectionUseCase().collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.update { it.copy(isServerOnline = true, connectionMessage = "Online") }
                            }
                            is Resource.Error -> {
                                _state.update { it.copy(isServerOnline = false, connectionMessage = result.message ?: "Koneksi Bermasalah") }
                            }
                            else -> {}
                        }
                    }
                } else {
                    // Kalau mode QR Code (Offline), anggap selalu online agar banner merah tidak muncul mengganggu
                    _state.update { it.copy(isServerOnline = true) }
                }

                delay(5000) // Jeda 5 detik sebelum nge-ping lagi
            }
        }
    }
}
