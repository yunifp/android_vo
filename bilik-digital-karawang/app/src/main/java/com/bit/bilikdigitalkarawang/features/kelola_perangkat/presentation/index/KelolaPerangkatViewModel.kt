package com.bit.bilikdigitalkarawang.features.kelola_perangkat.presentation.index

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.ConnectionStatus
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.BluetoothDeviceInfo
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.UsbPrinterInfo
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.repository.KelolaPerangkatRepository
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.usecase.GetSavedPrinterUseCase
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.PrintHasilVotingUseCase
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class KelolaPerangkatViewModel @Inject constructor(
    private val kelolaPerangkatRepository: KelolaPerangkatRepository,
    private val getSavedPrinterUseCase: GetSavedPrinterUseCase,
    private val printHasilVotingUseCase: PrintHasilVotingUseCase,
    private val dataStoreDiv: DataStoreDiv
) : ViewModel() {

    private val _state = MutableStateFlow(KelolaPerangkatState())
    val state: StateFlow<KelolaPerangkatState> = _state.asStateFlow()

    init {
        getSavedPrinter()
        observeDiscoveredDevices()
    }

    private fun getSavedPrinter() {
        viewModelScope.launch {
            val result = getSavedPrinterUseCase()
            result.onSuccess { printerInfo ->
                _state.update { it.copy(savedPrinterInfo = printerInfo) }
            }.onFailure {
                _state.update { it.copy(savedPrinterInfo = null) }
            }
        }
    }

    private fun observeDiscoveredDevices() {
        viewModelScope.launch {
            combine(
                kelolaPerangkatRepository.discoveredDevices,
                kelolaPerangkatRepository.getSelectedPrinterMac(),
                kelolaPerangkatRepository.getSelectedPrinterName()
            ) { discovered, selectedMac, selectedName ->
                _state.update {
                    it.copy(
                        discoveredDevices = discovered,
                        selectedPrinter = if (selectedMac != null && selectedName != null) {
                            BluetoothDeviceInfo(selectedName, selectedMac, isPrinter = true)
                        } else null
                    )
                }
            }.collect()
        }
    }

    // ✅ Dipanggil dari UI saat permission sudah diberikan
    fun refreshBluetoothState() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        _state.update { it.copy(isBluetoothEnabled = adapter?.isEnabled == true) }

        checkPermissions()
        loadPairedDevices()
    }

    private fun checkPermissions() {
        val missingPermissions = kelolaPerangkatRepository.checkPermissions()
        if (missingPermissions.isNotEmpty()) {
            _state.update {
                it.copy(errorMessage = "Permissions diperlukan: ${missingPermissions.joinToString(", ")}")
            }
        }
    }

    private fun loadPairedDevices() {
        try {
            val pairedDevices = kelolaPerangkatRepository.getPairedDevices()
            _state.update { it.copy(pairedDevices = pairedDevices) }
        } catch (e: Exception) {
            _state.update {
                it.copy(errorMessage = "Error memuat perangkat: ${e.message}")
            }
        }
    }

    fun startScan() {
        if (!kelolaPerangkatRepository.isBluetoothEnabled()) {
            _state.update { it.copy(errorMessage = "Bluetooth tidak aktif") }
            return
        }

        val missingPermissions = kelolaPerangkatRepository.checkPermissions()
        if (missingPermissions.isNotEmpty()) {
            _state.update {
                it.copy(errorMessage = "Permissions tidak lengkap: ${missingPermissions.joinToString(", ")}")
            }
            return
        }

        _state.update { it.copy(isScanning = true, errorMessage = null) }

        try {
            val success = kelolaPerangkatRepository.startDiscovery()
            if (!success) {
                _state.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "Gagal memulai pencarian perangkat."
                    )
                }
            } else {
                viewModelScope.launch {
                    delay(12000)
                    if (_state.value.isScanning) stopScan()
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(isScanning = false, errorMessage = "Error: ${e.message}")
            }
        }
    }

    fun stopScan() {
        kelolaPerangkatRepository.stopDiscovery()
        _state.update { it.copy(isScanning = false) }
    }

    fun selectPrinter(device: BluetoothDeviceInfo) {
        _state.update {
            it.copy(
                isConnecting = true,
                errorMessage = null,
                connectionStatus = ConnectionStatus.CONNECTING
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connected = kelolaPerangkatRepository.connectToPrinter(device.macAddress)

                withContext(Dispatchers.Main) {
                    if (connected) {
                        kelolaPerangkatRepository.saveSelectedPrinter(device)
                        _state.update {
                            it.copy(
                                selectedPrinter = device,
                                connectionStatus = ConnectionStatus.CONNECTED,
                                isConnecting = false,
                                errorMessage = "Berhasil terhubung ke ${device.name}"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                connectionStatus = ConnectionStatus.ERROR,
                                isConnecting = false,
                                errorMessage = "Gagal terhubung ke printer ${device.name}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.ERROR,
                            isConnecting = false,
                            errorMessage = "Error koneksi: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun scanUsbPrinters(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val printers = usbManager.deviceList.values.filter { device ->
            device.deviceClass == UsbConstants.USB_CLASS_PRINTER ||
                    (0 until device.interfaceCount).any { i ->
                        device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_PRINTER
                    }
        }.map { UsbPrinterInfo(it.deviceName, it.vendorId, it.productId) }

        _state.update { it.copy(usbPrinterList = printers) }

        // Request permission untuk semua printer yang ditemukan
        printers.forEach { printerInfo ->
            val device = usbManager.deviceList.values.firstOrNull {
                it.vendorId == printerInfo.vendorId && it.productId == printerInfo.productId
            } ?: return@forEach

            if (!usbManager.hasPermission(device)) {
                val permissionIntent = PendingIntent.getBroadcast(
                    context, 0,
                    Intent("${context.packageName}.USB_PERMISSION"),
                    PendingIntent.FLAG_IMMUTABLE
                )
                usbManager.requestPermission(device, permissionIntent)
            }
        }
    }

    fun selectUsbPrinter(context: Context, printer: UsbPrinterInfo) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == printer.vendorId && it.productId == printer.productId
        } ?: run {
            _state.update { it.copy(errorMessage = "Device USB tidak ditemukan") }
            return
        }

        if (!usbManager.hasPermission(device)) {
            _state.update { it.copy(errorMessage = "Permission USB belum diberikan") }
            return
        }

        viewModelScope.launch {
            saveSelectedUsbPrinter(printer)
            _state.update {
                it.copy(
                    savedUsbPrinterInfo = printer,
                    errorMessage = "Printer USB ${printer.deviceName} tersimpan"
                )
            }
        }
    }

    private suspend fun saveSelectedUsbPrinter(printer: UsbPrinterInfo) {
        val dataMap = mapOf(
            "selected_usb_printer_name" to printer.deviceName,
            "selected_usb_vendor_id" to printer.vendorId.toString(),
            "selected_usb_product_id" to printer.productId.toString()
        )
        dataStoreDiv.bulkSaveData(dataMap)
    }

    fun loadSavedUsbPrinter() {
        viewModelScope.launch {
            val name = dataStoreDiv.getData("selected_usb_printer_name").first()
            val vendorId = dataStoreDiv.getData("selected_usb_vendor_id").first()?.toIntOrNull()
            val productId = dataStoreDiv.getData("selected_usb_product_id").first()?.toIntOrNull()

            if (name != null && vendorId != null && productId != null) {
                _state.update {
                    it.copy(savedUsbPrinterInfo = UsbPrinterInfo(name, vendorId, productId))
                }
            }
        }
    }

    // Tambah fungsi
    fun loadMekanismePrint() {
        viewModelScope.launch {
            val mekanisme = dataStoreDiv.getData("mekanisme_print").first() ?: "bt"
            _state.update { it.copy(mekanismePrint = mekanisme) }
        }
    }

    fun setMekanismePrint(mekanisme: String) {
        viewModelScope.launch {
            dataStoreDiv.saveData("mekanisme_print", mekanisme)
            _state.update { it.copy(mekanismePrint = mekanisme) }
        }
    }


    fun print() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isPrinting = true) }
                val bytes = PrinterHelper.generateTestPrintBytes()
                val success = kelolaPerangkatRepository.printBytes(bytes)
                _state.update {
                    it.copy(
                        errorMessage = if (success) "Print berhasil!" else "Print gagal!"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Error print: ${e.message}")
                }
            } finally {
                _state.update { it.copy(isPrinting = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun hideAlert() {
        _state.update { it.copy(connectionStatus = null) }
    }

    override fun onCleared() {
        super.onCleared()
        kelolaPerangkatRepository.stopDiscovery()
        kelolaPerangkatRepository.disconnectPrinter()
    }
}
