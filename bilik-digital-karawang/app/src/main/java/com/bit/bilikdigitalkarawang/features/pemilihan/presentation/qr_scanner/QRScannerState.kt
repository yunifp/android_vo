package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner

data class QRScannerState(
    val isScanning: Boolean = false,
    val scannedNik: String? = null,
    val scannedIdDpt: String? = null,
    val errorMessage: String? = null,
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val isCameraReady: Boolean = false,
    val totalPemilihan: Int = 0
)