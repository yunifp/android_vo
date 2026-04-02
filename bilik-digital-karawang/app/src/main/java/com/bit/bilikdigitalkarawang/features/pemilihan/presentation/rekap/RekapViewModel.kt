package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.ExportPemilihanToJsonUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetJumlahPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraAbstainLakiUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraAbstainPerempuanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraPerKandidatUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraSahLakiUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraSahPerempuanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraTidakSahLakiUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetSuaraTidakSahPerempuanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetTotalPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RekapViewModel @Inject constructor(
    private val getSuaraPerKandidatUseCase: GetSuaraPerKandidatUseCase,
    private val getJumlahPemilihUseCase: GetJumlahPemilihUseCase,
    private val getJumlahPemilihanUseCase: GetTotalPemilihanUseCase,
    private val getSuaraSahLakiUseCase: GetSuaraSahLakiUseCase,
    private val getSuaraSahPerempuanUseCase: GetSuaraSahPerempuanUseCase,
    private val getSuaraTidakSahLakiUseCase: GetSuaraTidakSahLakiUseCase,
    private val getSuaraTidakSahPerempuanUseCase: GetSuaraTidakSahPerempuanUseCase,
    private val getSuaraAbstainLakiUseCase: GetSuaraAbstainLakiUseCase,
    private val getSuaraAbstainPerempuanUseCase: GetSuaraAbstainPerempuanUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val exportPemilihanToJsonUseCase: ExportPemilihanToJsonUseCase

) : ViewModel() {

    private val _state = MutableStateFlow(RekapState())
    val state: StateFlow<RekapState> = _state

    init {
        getJumlahPemilih()
        getJumlahPemilihan()
        getSuaraPerKandidat()
        getJumlahSahLaki()
        getJumlahSahPerempuan()
        getJumlahTidakSahLaki()
        getJumlahTidakSahPerempuan()
        getJumlahAbstainLaki()
        getJumlahAbstainPerempuan()
        getUserInfo()
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

    fun getJumlahSahLaki() {
        getSuaraSahLakiUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahSahLaki = result.data ?: 0
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

    fun getJumlahSahPerempuan() {
        getSuaraSahPerempuanUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahSahPerempuan = result.data ?: 0
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

    fun getJumlahTidakSahLaki() {
        getSuaraTidakSahLakiUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahTidakSahLaki = result.data ?: 0
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

    fun getJumlahTidakSahPerempuan() {
        getSuaraTidakSahPerempuanUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahTidakSahPerempuan = result.data ?: 0
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

    fun getJumlahAbstainLaki() {
        getSuaraAbstainLakiUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahAbstainLaki = result.data ?: 0
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

    fun getJumlahAbstainPerempuan() {
        getSuaraAbstainPerempuanUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        jumlahAbstainPerempuan = result.data ?: 0
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

    fun exportJson() {
        viewModelScope.launch {
            try {
                exportPemilihanToJsonUseCase().collect { exportResult ->
                    when (exportResult) {
                        is Resource.Success -> {
                            Log.d("AutoExport", "Export berhasil: ${exportResult.data}")
                        }
                        is Resource.Error -> {
                            Log.e("AutoExport", "Export gagal: ${exportResult.message}")
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoExport", "Export exception: ${e.message}")
            }
        }
    }
}
