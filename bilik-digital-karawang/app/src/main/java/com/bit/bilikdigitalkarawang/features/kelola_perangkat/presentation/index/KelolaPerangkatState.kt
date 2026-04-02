package com.bit.bilikdigitalkarawang.features.kelola_perangkat.presentation.index

import com.bit.bilikdigitalkarawang.common.ConnectionStatus
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.BluetoothDeviceInfo
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.SavedPrinterInfo
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.UsbPrinterInfo

data class KelolaPerangkatState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val discoveredDevices: List<BluetoothDeviceInfo> = emptyList(),
    val pairedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val selectedPrinter: BluetoothDeviceInfo? = null,
    val connectionStatus: ConnectionStatus? = ConnectionStatus.DISCONNECTED,
    val errorMessage: String? = null,
    val isConnecting: Boolean = false,
    val savedPrinterInfo: SavedPrinterInfo? = null,
    val isPrinting: Boolean = false,

    val mekanismePrint: String = "bt", // "bt" atau "cbl"
    val usbPrinterList: List<UsbPrinterInfo> = emptyList(),
    val savedUsbPrinterInfo: UsbPrinterInfo? = null,
)