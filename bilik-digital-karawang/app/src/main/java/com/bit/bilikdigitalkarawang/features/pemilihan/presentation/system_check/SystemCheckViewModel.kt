package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.system_check

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetKandidatUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.GetPemilihUseCase
import com.bit.bilikdigitalkarawang.features.setting.domain.GetExportLocationUseCase
import com.bit.bilikdigitalkarawang.helpers.BluetoothConnectionManager
import com.bit.bilikdigitalkarawang.helpers.UsbPrinterManager
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SystemCheckViewModel @Inject constructor(
    private val getKandidatUseCase: GetKandidatUseCase,
    private val getPemilihUseCase: GetPemilihUseCase,
    private val getExportLocationUseCase: GetExportLocationUseCase,
    @ApplicationContext private val context: Context,
    private val bluetoothConnectionManager: BluetoothConnectionManager,
    private val dataStoreDiv: DataStoreDiv,
    private val usbPrinterManager: UsbPrinterManager
) : ViewModel() {

    private val _state = MutableStateFlow(SystemCheckState())
    val state: StateFlow<SystemCheckState> = _state

    fun startSystemCheck() {
        checkKandidatData()
        checkPemilihData()
        checkBackupPath()
        checkSdCard()
        checkPrinter()
    }

    private fun checkKandidatData() {
        _state.update { it.copy(kandidatCheck = CommonStatus.Loading) }

        try {
            getKandidatUseCase().onEach { result ->
                when(result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(kandidatCheck = CommonStatus.Loading) }
                    }
                    is Resource.Success -> {
                        val dataList = result.data ?: emptyList()
                        if (dataList.isNotEmpty()) {
                            _state.update {
                                it.copy(
                                    kandidatCheck = CommonStatus.Success,
                                    kandidatCheckMsg = "Data kandidat tersedia (${dataList.size} kandidat)"
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    kandidatCheck = CommonStatus.Error,
                                    kandidatCheckMsg = "Data kandidat tidak ditemukan"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                kandidatCheck = CommonStatus.Error,
                                kandidatCheckMsg = result.message ?: "Gagal mengambil data kandidat"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    kandidatCheck = CommonStatus.Error,
                    kandidatCheckMsg = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    private fun checkPemilihData() {
        _state.update { it.copy(pemilihCheck = CommonStatus.Loading) }

        try {
            getPemilihUseCase().onEach { result ->
                when(result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(pemilihCheck = CommonStatus.Loading) }
                    }
                    is Resource.Success -> {
                        val dataList = result.data ?: emptyList()
                        if (dataList.isNotEmpty()) {
                            _state.update {
                                it.copy(
                                    pemilihCheck = CommonStatus.Success,
                                    pemilihCheckMsg = "Data pemilih tersedia (${dataList.size} pemilih)"
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    pemilihCheck = CommonStatus.Error,
                                    pemilihCheckMsg = "Data pemilih tidak ditemukan"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                pemilihCheck = CommonStatus.Error,
                                pemilihCheckMsg = result.message ?: "Gagal mengambil data pemilih"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    kandidatCheck = CommonStatus.Error,
                    kandidatCheckMsg = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    private fun checkSdCard() {
        _state.update { it.copy(sdCardCheck = CommonStatus.Loading) }

        viewModelScope.launch {
            try {
                delay(500)

                val sdCardPath = getRemovableSdCardPath()

                if (sdCardPath != null) {
                    val availableSpace = getSdCardAvailableSpace(sdCardPath)
                    _state.update {
                        it.copy(
                            sdCardCheck = CommonStatus.Success,
                            sdCardCheckMsg = "SD Card terdeteksi ($availableSpace tersedia)"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            sdCardCheck = CommonStatus.Error,
                            sdCardCheckMsg = "SD Card tidak terdeteksi"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        sdCardCheck = CommonStatus.Error,
                        sdCardCheckMsg = e.message ?: "Terjadi kesalahan saat cek SD Card"
                    )
                }
            }
        }
    }

    private fun getRemovableSdCardPath(): String? {
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)

        for (i in 1 until externalDirs.size) {
            val file = externalDirs[i]
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                // Dapatkan root SD Card path
                val path = file.absolutePath

                // Split by "/" dan ambil sampai storage ID
                // Contoh: /storage/1234-5678/Android/data/... -> /storage/1234-5678
                val parts = path.split("/")
                if (parts.size >= 3) {
                    return "/${parts[1]}/${parts[2]}" // /storage/XXXX-XXXX
                }
            }
        }

        return null
    }

    private fun getSdCardAvailableSpace(path: String): String {
        return try {
            val stat = StatFs(path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            android.text.format.Formatter.formatFileSize(context, availableBytes)
        } catch (e: Exception) {
            "Unknown"
        }
    }


    private fun checkBackupPath() {
        _state.update { it.copy(backupPathCheck = CommonStatus.Loading) }

        try {
            getExportLocationUseCase().onEach { result ->
                when(result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(backupPathCheck = CommonStatus.Loading) }
                    }
                    is Resource.Success -> {
                        val backupPath = result.data ?: ""
                        Log.d("BackupCheck", "📁 Backup path dari DataStore: $backupPath")

                        if (backupPath.isNotEmpty()) {

                            val sdCardPath = getRemovableSdCardPath()
                            Log.d("BackupCheck", "💾 SD Card path detected: $sdCardPath")

                            if (sdCardPath != null) {
                                val existsInSdCard = checkBackupPathInSdCard(sdCardPath, backupPath)
                                Log.d("BackupCheck", "✅ Exists in SD Card: $existsInSdCard")

                                if (existsInSdCard) {
                                    val displayPath = extractDisplayPath(backupPath)

                                    _state.update {
                                        it.copy(
                                            backupPathCheck = CommonStatus.Success,
                                            backupPathCheckMsg = "Backup path valid di SD Card: $displayPath "
                                        )
                                    }
                                } else {
                                    _state.update {
                                        it.copy(
                                            backupPathCheck = CommonStatus.Error,
                                            backupPathCheckMsg = "Backup path tidak ditemukan di SD Card"
                                        )
                                    }
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        backupPathCheck = CommonStatus.Error,
                                        backupPathCheckMsg = "SD Card tidak terdeteksi"
                                    )
                                }
                            }

                        } else {
                            _state.update {
                                it.copy(
                                    backupPathCheck = CommonStatus.Error,
                                    backupPathCheckMsg = "Path backup belum dikonfigurasi"
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                backupPathCheck = CommonStatus.Error,
                                backupPathCheckMsg = result.message ?: "Gagal mengambil data backupPath"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    backupPathCheck = CommonStatus.Error,
                    backupPathCheckMsg = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    private fun extractDisplayPath(path: String): String {
        return try {
            if (path.startsWith("content://")) {
                // Decode URI
                val decodedPath = Uri.decode(path)

                // Extract dari format: content://...documents/document/3AE1-13DC:restore_folder/alddy.json
                // Kita ambil bagian setelah storage ID (setelah ":")
                val regex = """[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}:(.+)""".toRegex()
                val match = regex.find(decodedPath)

                if (match != null) {
                    match.groupValues[1] // restore_folder/alddy.json
                } else {
                    // Fallback: ambil nama file saja dari DocumentFile
                    val uri = Uri.parse(path)
                    val documentFile = DocumentFile.fromSingleUri(context, uri)
                    documentFile?.name ?: "Unknown"
                }
            } else {
                // Untuk file path biasa, extract setelah storage ID
                // Dari: /storage/3AE1-13DC/restore_folder/alddy.json
                // Ke: restore_folder/alddy.json
                val parts = path.split("/")
                if (parts.size >= 4 && parts[1] == "storage") {
                    // Skip /storage/XXXX-XXXX/ dan ambil sisanya
                    parts.drop(3).joinToString("/")
                } else {
                    path
                }
            }
        } catch (e: Exception) {
            Log.e("BackupCheck", "Error extracting display path: ${e.message}")
            path
        }
    }

    private fun checkBackupPathInSdCard(sdCardPath: String, backupPath: String): Boolean {
        if (backupPath.isBlank()) return false

        Log.d("BackupCheck", "🔍 Checking backup path: $backupPath")
        Log.d("BackupCheck", "💾 Against SD Card: $sdCardPath")

        // Jika menggunakan Content URI (SAF)
        if (backupPath.startsWith("content://")) {
            return checkContentUriInSdCard(sdCardPath, backupPath)
        }

        // Jika menggunakan file path biasa
        val file = File(backupPath)
        val isInsideSdCard = backupPath.startsWith(sdCardPath)
        val exists = file.exists() && file.isDirectory

        Log.d("BackupCheck", "📂 Is inside SD Card: $isInsideSdCard, Exists: $exists")

        return isInsideSdCard && exists
    }

    private fun checkContentUriInSdCard(sdCardPath: String, uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)

            Log.d("BackupCheck", "🔍 Checking URI: $uriString")

            // Ekstrak storage ID dari URI
            val storageIdFromUri = extractStorageIdFromUri(uriString)
            val storageIdFromPath = extractStorageIdFromPath(sdCardPath)

            Log.d("BackupCheck", "🔑 Storage ID from URI: $storageIdFromUri")
            Log.d("BackupCheck", "🔑 Storage ID from Path: $storageIdFromPath")

            // Cek storage ID match
            val isSameStorage = storageIdFromUri != null &&
                    storageIdFromPath != null &&
                    storageIdFromUri == storageIdFromPath

            Log.d("BackupCheck", "✅ Same storage: $isSameStorage")

            // Untuk backup file, cukup cek apakah storage ID match
            // Karena file mungkin belum dibuat
            isSameStorage

        } catch (e: Exception) {
            Log.e("BackupCheck", "❌ Error checking URI: ${e.message}", e)
            false
        }
    }

    private fun extractStorageIdFromUri(uriString: String): String? {
        return try {
            // Decode URL encoding dulu
            val decodedUri = Uri.decode(uriString)
            Log.d("BackupCheck", "📝 Decoded URI: $decodedUri")

            // Cari pattern storage ID (format: XXXX-XXXX atau xxxx-xxxx)
            // Contoh dari: content://...documents/document/3AE1-13DC:alddy.json
            val regex = """([0-9A-Fa-f]{4}-[0-9A-Fa-f]{4})""".toRegex()
            val match = regex.find(decodedUri)?.value?.uppercase()

            Log.d("BackupCheck", "🔍 Found storage ID: $match")
            match
        } catch (e: Exception) {
            Log.e("BackupCheck", "❌ Error extracting storage ID from URI: ${e.message}")
            null
        }
    }

    private fun extractStorageIdFromPath(path: String): String? {
        // Dari /storage/3AE1-13DC/... ambil 3AE1-13DC
        val parts = path.split("/")
        val storageId = if (parts.size >= 3 && parts[1] == "storage") {
            parts[2].uppercase() // Normalize ke uppercase
        } else {
            null
        }

        Log.d("BackupCheck", "🔍 Extracted storage ID from path: $storageId")
        return storageId
    }


    private fun checkPrinter() {
        _state.update { it.copy(printerCheck = CommonStatus.Loading) }

        viewModelScope.launch {
            try {
                val mekanisme = dataStoreDiv.getData("mekanisme_print").first() ?: "bt"

                Log.d("ASASDA", mekanisme)

                val result = if (mekanisme == "cbl") {
                    checkUsbPrinter()
                } else {
                    connectPrinterWithRetry(maxRetries = 2)
                }

                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            printerCheck = CommonStatus.Success,
                            printerCheckMsg = "Printer terhubung"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            printerCheck = CommonStatus.Error,
                            printerCheckMsg = result.exceptionOrNull()?.message ?: "Gagal terhubung ke printer"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        printerCheck = CommonStatus.Error,
                        printerCheckMsg = e.message ?: "Terjadi kesalahan saat cek printer"
                    )
                }
            }
        }
    }

    private suspend fun checkUsbPrinter(): Result<Unit> {
        val connectResult = usbPrinterManager.connectIfNeeded()
        if (connectResult.isFailure) return connectResult

        return usbPrinterManager.testPrinter()
    }
    private suspend fun connectPrinterWithRetry(maxRetries: Int = 2): Result<Unit> {
        repeat(maxRetries) { attempt ->

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

    fun retryCheck() {
        _state.value = SystemCheckState()
        startSystemCheck()
    }
}
