package com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model

data class BluetoothDeviceInfo(
    val name: String,
    val macAddress: String,
    val isConnected: Boolean = false,
    val isPrinter: Boolean = false
)