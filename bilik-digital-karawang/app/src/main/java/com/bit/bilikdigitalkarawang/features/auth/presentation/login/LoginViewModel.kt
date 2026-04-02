package com.bit.bilikdigitalkarawang.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun updateForm(field: String, value: String) {
        _state.update { currentState ->
            val currentFormData = currentState.formData

            val updatedFormData = when(field) {
                "username" -> currentFormData.copy(username = value)
                "password" -> currentFormData.copy(password = value)
                else -> currentFormData
            }

            currentState.copy(formData = updatedFormData)
        }
    }

    fun submit() {
        viewModelScope.launch {
            _state.update { it.copy(status = CommonStatus.Loading, statusMsg = "Sedang memproses permintaan...", showAlert = false) }

            loginUseCase(formDatax = _state.value.formData).onEach { result ->
                when (result) {
                    is Resource.Success -> _state.update {
                        it.copy(
                            status = CommonStatus.Success,
                            statusMsg = "Login berhasil. Selamat datang!",
                            showAlert = true
                        )
                    }

                    is Resource.Error -> _state.update {
                        it.copy(
                            status = CommonStatus.Error,
                            statusMsg = result.message ?: "Login gagal. Silakan periksa koneksi atau data Anda.",
                            showAlert = true
                        )
                    }

                    is Resource.Loading -> _state.update {
                        it.copy(
                            status = CommonStatus.Loading,
                            statusMsg = "Menghubungkan ke server...",
                            showAlert = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }


    fun hideAlert() {
        _state.update { it.copy(showAlert = false) }
    }

}