package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetTotalPemilihanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val getTotalPemilihanUseCase: GetTotalPemilihanUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QRScannerState())
    val state: StateFlow<QRScannerState> = _state

    init {
        getTotalPemilihan()
    }

    fun getTotalPemilihan() {
        getTotalPemilihanUseCase().onEach { result ->
            when(result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.update { it.copy(totalPemilihan = result.data ?: 0) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onPermissionGranted() {
        _state.value = _state.value.copy(
            hasPermission = true,
            errorMessage = null
        )
    }

    fun onPermissionDenied() {
        _state.value = _state.value.copy(
            hasPermission = false,
            errorMessage = "Permission kamera diperlukan untuk scan QR code"
        )
    }

    fun onCameraReady() {
        _state.value = _state.value.copy(
            isCameraReady = true,
            isScanning = true
        )
    }

    fun onQRCodeScanned(qrContent: String) {
        // Prevent multiple scans
        if (_state.value.scannedNik != null) return

        val trimmedContent = qrContent.trim()

        // Validasi apakah QR code berisi NIK (16 digit angka)
        if (isValidNik(trimmedContent)) {
            Log.d(Constant.LOG_TAG, "Valid NIK detected, navigating...")
            _state.value = _state.value.copy(
                scannedNik = trimmedContent,
                isScanning = false,
                errorMessage = null
            )
        } else {
            Log.d(Constant.LOG_TAG, "Invalid NIK format: '$trimmedContent'")
            viewModelScope.launch {
                _state.value = _state.value.copy(
                    errorMessage = "QR Code tidak berisi NIK yang valid (harus 16 digit)",
                    isScanning = false
                )
                // Reset scanning setelah 2 detik untuk coba lagi
                delay(2000)
                resetScanning()
            }
        }
    }

    fun resetScanning() {
        _state.value = _state.value.copy(
            isScanning = true,
            errorMessage = null,
            isLoading = false,
            scannedNik = null
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
        if (_state.value.hasPermission && _state.value.isCameraReady) {
            resetScanning()
        }
    }

    private fun isValidNik(nik: String): Boolean {
        return nik.matches(Regex("^\\d{16}$"))
    }
}