package com.bit.bilikdigitalkarawang.features.auth.presentation.konfirmasi_pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KonfirmasiPinViewModel @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) : ViewModel() {

    private val _state = MutableStateFlow(KonfirmasiPinState())
    val state: StateFlow<KonfirmasiPinState> = _state.asStateFlow()

    fun onPinChanged(pin: String) {
        if (pin.length <= 6) {
            _state.value = _state.value.copy(
                pin = pin,
                errorMessage = ""
            )

            // Auto verify ketika PIN sudah 6 digit
            if (pin.length == 6) {
                verifyPin(pin)
            }
        }
    }

    private fun verifyPin(inputPin: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val savedPin = dataStoreDiv.getData("user_pin").first()

                if (inputPin == savedPin) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isVerified = true,
                        errorMessage = ""
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "PIN salah",
                        pin = "" // Reset PIN input
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}",
                    pin = ""
                )
            }
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(errorMessage = "")
    }
}