package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.common.PrintMode
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CekNikValidUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.ExportPemilihanToJsonUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetDetailPemilihUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetKandidatUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetUserInfoUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.InsertPemilihanUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.PostVoteUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.PrintHasilVotingUseCase
import com.bit.bilikdigitalkarawang.helpers.BluetoothConnectionManager
import com.bit.bilikdigitalkarawang.helpers.UsbPrinterManager
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.compareTo
import kotlin.coroutines.resume


@HiltViewModel
class PemilihanViewModel @Inject constructor(
    private val getKandidatUseCase: GetKandidatUseCase,
    private val insertPemilihanUseCase: InsertPemilihanUseCase,
    private val postVoteUseCase: PostVoteUseCase,
    private val printHasilVotingUseCase: PrintHasilVotingUseCase,
    private val cekNikValidUseCase: CekNikValidUseCase,
    private val getDetailPemilihUseCase: GetDetailPemilihUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val exportPemilihanToJsonUseCase: ExportPemilihanToJsonUseCase,
    private val bluetoothConnectionManager: BluetoothConnectionManager,
    private val dataStoreDiv: DataStoreDiv,
    private val usbPrinterManager: UsbPrinterManager
): ViewModel() {

    private val _state = MutableStateFlow(PemilihanState())
    val state: StateFlow<PemilihanState> = _state

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

    fun setNik(nik: String) {
        _state.update { it.copy(nik = nik) }
        checkingNik()
    }

    fun checkingNik() {
        cekNikValidUseCase(_state.value.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isCheckingNik = true) }
                }

                is Resource.Success -> {
                    if (result.data == true) {
                        _state.update {
                            it.copy(
                                checkingNikStatus = true,
                                isCheckingNik = false
                            )
                        }
                        getKandidatList()
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            checkingNikStatus = false,
                            checkingNikStatusMsg = result.message ?: "Terjadi kesalahan",
                            isCheckingNik = false
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

    private fun getKandidatList() {
        getKandidatUseCase().onEach { result ->
            when (result) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        kandidatList = result.data ?: emptyList()
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleKandidatSelection(kandidat: Kandidat) {
        _state.update { currentState ->
            val currentSelection = currentState.kandidatTerpilih.toMutableList()

            // Jika kandidat sudah dipilih, hapus; kalau belum, tambah
            if (currentSelection.any { it.noUrut == kandidat.noUrut }) {
                currentSelection.removeAll { it.noUrut == kandidat.noUrut }
            } else {
                currentSelection.add(kandidat)
            }

            currentState.copy(
                kandidatTerpilih = currentSelection
            )
        }
    }

    private var isVoteProcessing = false // Flag untuk prevent multiple clicks

    fun vote() {
        // 🔒 PREVENT MULTIPLE CLICKS
        if (isVoteProcessing) {
            Log.d(Constant.LOG_TAG, "Vote sedang diproses, abaikan klik")
            return
        }

        isVoteProcessing = true

        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        voteStatus = CommonStatus.Loading,
                        voteStatusMsg = "Mohon tunggu...",
                        showConfirmation = false
                    )
                }

                // 1️⃣ CEK DAN KONEKSI PRINTER (dengan retry)
                val printerCheck = connectPrinterWithRetry(maxRetries = 1)
                if (printerCheck.isFailure) {
                    _state.update {
                        it.copy(
                            voteStatus = CommonStatus.Error,
                            voteStatusMsg = "Printer tidak terhubung. ${printerCheck.exceptionOrNull()?.message}",
                            showConfirmation = false
                        )
                    }
                    return@launch
                }

                // 2️⃣ INSERT PEMILIHAN
                val selectedCandidates = _state.value.kandidatTerpilih
                val noUrutList = selectedCandidates.map { it.noUrut }
                val namaKandidatList = selectedCandidates.map { it.namaCalon }
                val nik = _state.value.nik

                _state.update {
                    it.copy(
                        voteStatus = CommonStatus.Loading,
                        voteStatusMsg = "Menyimpan suara...",
                    )
                }

                // Gunakan suspending function langsung tanpa collect jika memungkinkan
                val insertResult = insertPemilihanSuspend(nik, noUrutList, namaKandidatList)

                if (insertResult.isFailure) {
                    _state.update {
                        it.copy(
                            voteStatus = CommonStatus.Error,
                            voteStatusMsg = insertResult.exceptionOrNull()?.message ?: "Gagal menyimpan suara",
                        )
                    }
                    return@launch
                }

                _state.update {
                    it.copy(
                        voteStatus = CommonStatus.Success,
                        voteStatusMsg = "Suara berhasil disimpan.",
                    )
                }

                // 3️⃣ AUTO EXPORT di background (fire and forget)
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

                // 4️⃣ CETAK dengan delay kecil untuk stabilitas
                delay(300) // Beri jeda sebelum print
                printWithRetry()

            } catch (e: Exception) {
                Log.e(Constant.LOG_TAG, "Vote error: ${e.message}", e)
                _state.update {
                    it.copy(
                        voteStatus = CommonStatus.Error,
                        voteStatusMsg = e.message ?: "Terjadi kesalahan",
                    )
                }
            } finally {
                isVoteProcessing = false // ✅ Unlock setelah selesai
            }
        }
    }

    // Helper function untuk koneksi printer dengan retry
    private suspend fun connectPrinterWithRetry(maxRetries: Int = 2): Result<Unit> {
        val mekanisme = dataStoreDiv.getData("mekanisme_print").first()
        Log.d(Constant.LOG_TAG, "Mekanisme print: $mekanisme")

        if (mekanisme == "cbl") {
            // USB tidak perlu retry seperti Bluetooth
            val result = usbPrinterManager.connectIfNeeded()
            if (result.isFailure) {
                Log.d(Constant.LOG_TAG, "USB gagal: ${result.exceptionOrNull()?.message}")
            }
            return result
        }

        // Bluetooth dengan retry (existing logic)
        repeat(maxRetries) { attempt ->
            Log.d(Constant.LOG_TAG, "Mencoba koneksi Bluetooth (attempt ${attempt + 1})")

            val result = bluetoothConnectionManager.connectIfNeeded()
            if (result.isSuccess) {
                delay(200)
                val testResult = bluetoothConnectionManager.testPrinter()
                if (testResult.isSuccess) {
                    Log.d(Constant.LOG_TAG, "Printer berhasil terkoneksi dan ditest")
                    return Result.success(Unit)
                }
            }

            if (attempt < maxRetries - 1) {
                delay(500)
                bluetoothConnectionManager.close()
                delay(300)
            }
        }

        return Result.failure(Exception("Gagal terhubung ke printer"))
    }

    // Helper function untuk insert dengan suspending
    private suspend fun insertPemilihanSuspend(
        nik: String,
        noUrutList: List<String>,
        namaKandidatList: List<String>
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        insertPemilihanUseCase(nik, noUrutList, namaKandidatList)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    is Resource.Error -> {
                        if (continuation.isActive) {
                            continuation.resume(
                                Result.failure(Exception(result.message ?: "Insert gagal"))
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Ignore loading state
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Print dengan retry mechanism
    private suspend fun printWithRetry(maxRetries: Int = 2) {
        val idStatus = when {
            _state.value.kandidatTerpilih.isEmpty() -> 3
            _state.value.kandidatTerpilih.size > 1 -> 2
            else -> 1
        }

        val noUrut = if (idStatus == 1) _state.value.kandidatTerpilih.first().noUrut else null
        val namaKandidat = if (idStatus == 1) _state.value.kandidatTerpilih.first().namaCalon else null

        _state.update {
            it.copy(
                printStatus = CommonStatus.Loading,
                printStatusMsg = "Mencetak resi..."
            )
        }

        for (attempt in 0 until maxRetries) {
            Log.d(Constant.LOG_TAG, "Mencoba print (attempt ${attempt + 1})")

            // Pastikan koneksi masih OK sebelum print
            if (attempt > 0) {
                bluetoothConnectionManager.close()
                delay(300)
                val reconnect = bluetoothConnectionManager.connectIfNeeded()
                if (reconnect.isFailure) {
                    if (attempt == maxRetries - 1) {
                        _state.update {
                            it.copy(
                                printStatus = CommonStatus.Error,
                                printStatusMsg = "Koneksi printer terputus"
                            )
                        }
                        return
                    }
                    delay(500)
                    continue
                }
                delay(200)
            }

            val result = printHasilVotingUseCase(PrintMode.NORMAL, idStatus, noUrut, namaKandidat)

            if (result.isSuccess) {
                Log.d(Constant.LOG_TAG, "Print berhasil ✅")
                _state.update {
                    it.copy(
                        printStatus = CommonStatus.Success,
                        printStatusMsg = "Resi berhasil dicetak."
                    )
                }
                return
            } else {
                Log.e(Constant.LOG_TAG, "Print gagal (attempt ${attempt + 1}): ${result.exceptionOrNull()?.message}")

                if (attempt == maxRetries - 1) {
                    // Retry terakhir gagal
                    _state.update {
                        it.copy(
                            printStatus = CommonStatus.Error,
                            printStatusMsg = "Gagal mencetak: ${result.exceptionOrNull()?.message}"
                        )
                    }
                } else {
                    delay(500) // Delay sebelum retry
                }
            }
        }
    }


    fun showConfirm(value: Boolean) {
        _state.update { it.copy(showConfirmation = value) }
    }

    fun resetSuccessVote() {
        _state.update { it.copy(voteStatus = CommonStatus.Idle, voteStatusMsg = "") }
    }

    fun resetKandidat() {
        _state.update { it.copy(kandidatTerpilih = emptyList()) }
    }
}