package com.bit.bilikdigitalkarawang.features.auth.presentation.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val dataStoreDiv: DataStoreDiv
) : ViewModel() {

    private val _state = MutableStateFlow(PinState())
    val state: StateFlow<PinState> = _state

    fun onPinChanged(pin: String) {
        _state.update { it.copy(
            pin = pin,
            errorMessage = "",
            successMessage = "",
            isPinMatched = false
        ) }
    }

    fun onConfirmPinChanged(confirmPin: String) {
        _state.update { currentState ->
            val isMatched = currentState.pin == confirmPin

            currentState.copy(
                confirmPin = confirmPin,
                isPinMatched = isMatched,
                errorMessage = if (confirmPin.isNotEmpty() && !isMatched) "PIN tidak cocok" else "",
                successMessage = if (isMatched) "PIN cocok" else ""
            )
        }
    }

    fun savePinIfMatched() {
        if (_state.value.isPinMatched) {
            viewModelScope.launch {
                dataStoreDiv.bulkSaveData(mapOf(
                    "has_pin" to "Y",
                    "user_pin" to _state.value.pin,
                ))
                _state.update {
                    it.copy(
                        isPinMatched = true,
                        successMessage = "PIN berhasil disimpan",
                        errorMessage = ""
                    )
                }
            }
        } else {
            _state.update {
                it.copy(
                    isPinMatched = false,
                    errorMessage = "PIN tidak cocok",
                    successMessage = ""
                )
            }
        }
    }


    fun markPinCreated() {
        _state.update {
            it.copy(
                isPinCreated = true,
                errorMessage = "",
                successMessage = ""
            )
        }
    }

    fun resetPinCreation() {
        _state.update { currentState ->
            currentState.copy(
                isPinCreated = false,
                confirmPin = "",
                errorMessage = "",
                successMessage = ""
            )
        }
    }

}
