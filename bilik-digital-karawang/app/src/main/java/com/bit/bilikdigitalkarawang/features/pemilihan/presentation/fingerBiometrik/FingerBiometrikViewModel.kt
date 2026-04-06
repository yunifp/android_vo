package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.fingerBiometrik

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CekNikValidUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.FingerprintTemplateData
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.RetrofitClient
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.UserData
import com.zkteco.android.biometric.module.fingerprintreader.ZKFingerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

sealed class FingerScanState {
    object Idle : FingerScanState()
    object Verifying : FingerScanState()
    data class Success(val user: UserData) : FingerScanState()
    data class Error(val message: String) : FingerScanState()
}

@HiltViewModel
class FingerBiometrikViewModel @Inject constructor(
    private val cekNikValidUseCase: CekNikValidUseCase
) : ViewModel() {
    var scanState by mutableStateOf<FingerScanState>(FingerScanState.Idle)
        private set

    var isDataLoaded by mutableStateOf(false)
        private set

    var serverTemplates by mutableStateOf<List<FingerprintTemplateData>>(emptyList())
        private set

    var syncError by mutableStateOf<String?>(null)
        private set

    fun resetScan() {
        scanState = FingerScanState.Idle
    }

    fun loadFingerprintData() {
        if (isDataLoaded) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.instance
                val response = api.getAllFingerprints()
                withContext(Dispatchers.Main) {
                    if (response.success && response.data != null) {
                        serverTemplates = response.data
                        isDataLoaded = true
                        syncError = null
                    } else {
                        syncError = "Gagal mengunduh data sidik jari dari server."
                        scanState = FingerScanState.Error(syncError!!)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    syncError = "Tidak dapat terhubung ke server database."
                    scanState = FingerScanState.Error(syncError!!)
                }
            }
        }
    }

    // 🌟 PERBAIKAN: Parameter bitmap telah dihapus, kembali menggunakan capturedTemplate saja
    fun processFingerprint(capturedTemplate: ByteArray) {
        if (scanState is FingerScanState.Verifying || scanState is FingerScanState.Success || scanState is FingerScanState.Error) return

        scanState = FingerScanState.Verifying

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val foundUser = findMatchingUser(capturedTemplate, serverTemplates)

                withContext(Dispatchers.Main) {
                    if (foundUser != null) {
                        validateNikInDpt(foundUser)
                    } else {
                        scanState = FingerScanState.Error("Sidik jari tidak dikenali atau tidak terdaftar dalam sistem.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    scanState = FingerScanState.Error("Terjadi kesalahan saat memproses data biometrik.")
                }
            }
        }
    }

    private fun validateNikInDpt(detectedUser: UserData) {
        cekNikValidUseCase(detectedUser.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    if (result.data == true) {
                        scanState = FingerScanState.Success(detectedUser)
                    } else {
                        scanState = FingerScanState.Error("Sidik jari dikenali, namun NIK (${detectedUser.nik}) tidak terdaftar dalam Data Pemilih.")
                    }
                }
                is Resource.Error -> {
                    scanState = FingerScanState.Error(result.message ?: "Terjadi kesalahan saat memvalidasi NIK di Data Pemilih.")
                }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun findMatchingUser(
        capturedTemplate: ByteArray,
        templates: List<FingerprintTemplateData>
    ): UserData? = coroutineScope {
        var foundUser: UserData? = null
        val matchFound = AtomicBoolean(false)
        val cores = Runtime.getRuntime().availableProcessors()
        val chunkSize = maxOf(1, templates.size / cores)

        val jobs = templates.chunked(chunkSize).map { chunk ->
            async(Dispatchers.Default) {
                for (dbData in chunk) {
                    if (matchFound.get()) break
                    try {
                        val dbBytes = Base64.decode(dbData.template_data, Base64.NO_WRAP)
                        val score = ZKFingerService.verify(capturedTemplate, dbBytes)

                        if (score >= 70) {
                            if (matchFound.compareAndSet(false, true)) {
                                foundUser = dbData.user
                            }
                            break
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
        }

        jobs.awaitAll()
        return@coroutineScope foundUser
    }
}